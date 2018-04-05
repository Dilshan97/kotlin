/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.diagnostics

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtStubbedPsiUtil
import org.jetbrains.kotlin.psi.doNotAnalyze
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.util.ExtensionProvider

interface DiagnosticSuppressor {
    fun isSuppressed(diagnostic: Diagnostic): Boolean

    companion object {
        val EP_NAME: ExtensionPointName<DiagnosticSuppressor> =
            ExtensionPointName.create<DiagnosticSuppressor>("org.jetbrains.kotlin.diagnosticSuppressor")
    }
}

abstract class KotlinSuppressCache {
    private val diagnosticSuppressors = ExtensionProvider.create(DiagnosticSuppressor.EP_NAME)

    // The cache is weak: we're OK with losing it
    private val suppressors = ContainerUtil.createConcurrentWeakValueMap<KtAnnotated, Suppressor>()

    val filter: (Diagnostic) -> Boolean = { diagnostic: Diagnostic -> !isSuppressed(diagnostic) }

    fun isSuppressed(psiElement: PsiElement, suppressionKey: String, severity: Severity): Boolean =
        isSuppressed(StringSuppressRequest(psiElement, severity, suppressionKey.toLowerCase()))

    fun isSuppressed(diagnostic: Diagnostic): Boolean = isSuppressed(DiagnosticSuppressRequest(diagnostic))

    private fun isSuppressed(request: SuppressRequest): Boolean {
        // If diagnostics are reported in a synthetic file generated by KtPsiFactory (dummy.kt),
        // there's no point to present such diagnostics to the user, because the user didn't write this code
        val element = request.element
        if (!element.isValid) return true

        val file = element.containingFile
        if (file is KtFile) {
            if (file.doNotAnalyze != null) return true
        }

        if (request is DiagnosticSuppressRequest) {
            for (suppressor in diagnosticSuppressors.get()) {
                if (suppressor.isSuppressed(request.diagnostic)) return true
            }
        }

        val annotated = KtStubbedPsiUtil.getPsiOrStubParent(element, KtAnnotated::class.java, false) ?: return false

        return isSuppressedByAnnotated(request.suppressKey, request.severity, annotated, 0)
    }


    /*
       The cache is optimized for the case where no warnings are suppressed (most frequent one)

       trait Root {
         suppress("X")
         trait A {
           trait B {
             suppress("Y")
             trait C {
               fun foo() = warning
             }
           }
         }
       }

       Nothing is suppressed at foo, so we look above. While looking above we went up to the root (once) and propagated
       all the suppressors down, so now we have:

          foo  - suppress(Y) from C
          C    - suppress(Y) from C
          B    - suppress(X) from A
          A    - suppress(X) from A
          Root - suppress() from Root

       Next time we look up anything under foo, we try the Y-suppressor and then immediately the X-suppressor, then to the empty
       suppressor at the root. All the intermediate empty nodes are skipped, because every suppressor remembers its definition point.

       This way we need no more lookups than the number of suppress() annotations from here to the root.
     */
    protected fun isSuppressedByAnnotated(suppressionKey: String, severity: Severity, annotated: KtAnnotated, debugDepth: Int): Boolean {
        val suppressor = getOrCreateSuppressor(annotated)
        if (suppressor.isSuppressed(suppressionKey, severity)) return true

        val annotatedAbove = KtStubbedPsiUtil.getPsiOrStubParent(suppressor.annotatedElement, KtAnnotated::class.java, true) ?: return false

        val suppressed = isSuppressedByAnnotated(suppressionKey, severity, annotatedAbove, debugDepth + 1)
        val suppressorAbove = suppressors[annotatedAbove]
        if (suppressorAbove != null && suppressorAbove.dominates(suppressor)) {
            suppressors.put(annotated, suppressorAbove)
        }

        return suppressed
    }

    private fun getOrCreateSuppressor(annotated: KtAnnotated): Suppressor {
        var suppressor: Suppressor? = suppressors[annotated]
        if (suppressor == null) {
            val strings = getSuppressingStrings(annotated)
            suppressor = when {
                strings.isEmpty() -> EmptySuppressor(annotated)
                strings.size == 1 -> SingularSuppressor(annotated, strings.iterator().next())
                else -> MultiSuppressor(annotated, strings)
            }
            suppressors.put(annotated, suppressor)
        }
        return suppressor
    }

    abstract fun getSuppressionAnnotations(annotated: KtAnnotated): List<AnnotationDescriptor>

    private fun getSuppressingStrings(annotated: KtAnnotated): Set<String> {
        val builder = ImmutableSet.builder<String>()
        for (annotationDescriptor in getSuppressionAnnotations(annotated)) {
            processAnnotation(builder, annotationDescriptor)
        }

        return builder.build()
    }

    private fun processAnnotation(builder: ImmutableSet.Builder<String>, annotationDescriptor: AnnotationDescriptor) {
        if (annotationDescriptor.fqName != KotlinBuiltIns.FQ_NAMES.suppress) return

        // We only add strings and skip other values to facilitate recovery in presence of erroneous code
        for (arrayValue in annotationDescriptor.allValueArguments.values) {
            if ((arrayValue is ArrayValue)) {
                for (value in arrayValue.value) {
                    if (value is StringValue) {
                        builder.add(value.value.toLowerCase())
                    }
                }
            }
        }
    }

    companion object {
        fun getDiagnosticSuppressKey(diagnostic: Diagnostic): String {
            return diagnostic.factory.name.toLowerCase()
        }

        fun isSuppressedByStrings(key: String, strings: Set<String>, severity: Severity): Boolean {
            if (strings.contains("warnings") && severity == Severity.WARNING) return true

            return strings.contains(key)
        }
    }

    private abstract class Suppressor protected constructor(val annotatedElement: KtAnnotated) {
        abstract fun isSuppressed(diagnostic: Diagnostic): Boolean
        abstract fun isSuppressed(suppressionKey: String, severity: Severity): Boolean

        // true is \forall x. other.isSuppressed(x) -> this.isSuppressed(x)
        abstract fun dominates(other: Suppressor): Boolean
    }

    private class EmptySuppressor(annotated: KtAnnotated) : Suppressor(annotated) {
        override fun isSuppressed(diagnostic: Diagnostic): Boolean = false
        override fun isSuppressed(suppressionKey: String, severity: Severity): Boolean = false
        override fun dominates(other: Suppressor): Boolean = other is EmptySuppressor
    }

    private class SingularSuppressor(annotated: KtAnnotated, private val string: String) : Suppressor(annotated) {
        override fun isSuppressed(diagnostic: Diagnostic): Boolean {
            return isSuppressed(getDiagnosticSuppressKey(diagnostic), diagnostic.severity)
        }

        override fun isSuppressed(suppressionKey: String, severity: Severity): Boolean {
            return isSuppressedByStrings(suppressionKey, ImmutableSet.of(string), severity)
        }

        override fun dominates(other: Suppressor): Boolean {
            return other is EmptySuppressor || (other is SingularSuppressor && other.string == string)
        }
    }

    private class MultiSuppressor(annotated: KtAnnotated, private val strings: Set<String>) : Suppressor(annotated) {
        override fun isSuppressed(diagnostic: Diagnostic): Boolean {
            return isSuppressed(getDiagnosticSuppressKey(diagnostic), diagnostic.severity)
        }

        override fun isSuppressed(suppressionKey: String, severity: Severity): Boolean {
            return isSuppressedByStrings(suppressionKey, strings, severity)
        }

        override fun dominates(other: Suppressor): Boolean {
            // it's too costly to check set inclusion
            return other is EmptySuppressor
        }
    }

    private interface SuppressRequest {
        val element: PsiElement
        val severity: Severity
        val suppressKey: String
    }

    private class StringSuppressRequest(
        override val element: PsiElement,
        override val severity: Severity,
        override val suppressKey: String
    ) : SuppressRequest

    private class DiagnosticSuppressRequest(val diagnostic: Diagnostic) : SuppressRequest {
        override val element: PsiElement get() = diagnostic.psiElement
        override val severity: Severity get() = diagnostic.severity
        override val suppressKey: String get() = getDiagnosticSuppressKey(diagnostic)
    }
}

class BindingContextSuppressCache(val context: BindingContext) : KotlinSuppressCache() {
    override fun getSuppressionAnnotations(annotated: KtAnnotated): List<AnnotationDescriptor> {
        val descriptor = context.get(BindingContext.DECLARATION_TO_DESCRIPTOR, annotated)

        return if (descriptor != null) {
            descriptor.annotations.toList()
        } else {
            annotated.annotationEntries.mapNotNull { context.get(BindingContext.ANNOTATION, it) }
        }
    }
}

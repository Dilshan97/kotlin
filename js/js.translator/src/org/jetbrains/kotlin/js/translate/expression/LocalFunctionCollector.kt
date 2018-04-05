/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.expression

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext

class LocalFunctionCollector(val bindingContext: BindingContext) : KtVisitorVoid() {
    val functions: MutableSet<FunctionDescriptor> = mutableSetOf<FunctionDescriptor>()

    override fun visitExpression(expression: KtExpression) {
        if (expression is KtDeclarationWithBody) {
            functions += BindingUtils.getFunctionDescriptor(bindingContext, expression)
        }
        else {
            expression.acceptChildren(this, null)
        }
    }

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        // skip
    }
}
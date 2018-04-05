/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.caches.project

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.util.CachedValueProvider
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.caches.resolve.KotlinCacheService
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

fun Module.findImplementingModules(modelsProvider: IdeModifiableModelsProvider): List<Module> =
    modelsProvider.modules.filter { name in it.findImplementedModuleNames(modelsProvider) }

val Module.implementingModules: List<Module>
    get() = cached(CachedValueProvider {
        CachedValueProvider.Result(
            findImplementingModules(IdeModifiableModelsProviderImpl(project)),
            ProjectRootModificationTracker.getInstance(project)
        )
    })

private fun Module.getModuleInfo(baseModuleSourceInfo: ModuleSourceInfo): ModuleSourceInfo? =
    when (baseModuleSourceInfo) {
        is ModuleProductionSourceInfo -> productionSourceInfo()
        is ModuleTestSourceInfo -> testSourceInfo()
        else -> null
    }

private fun Module.findImplementingModuleInfos(moduleSourceInfo: ModuleSourceInfo): List<ModuleSourceInfo> {
    val modelsProvider = IdeModifiableModelsProviderImpl(project)
    val implementingModules = findImplementingModules(modelsProvider)
    return implementingModules.mapNotNull { it.getModuleInfo(moduleSourceInfo) }
}

val ModuleDescriptor.implementingDescriptors: List<ModuleDescriptor>
    get() {
        val moduleSourceInfo = getCapability(ModuleInfo.Capability) as? ModuleSourceInfo ?: return emptyList()
        val module = moduleSourceInfo.module
        return module.cached(CachedValueProvider {
            val implementingModuleInfos = module.findImplementingModuleInfos(moduleSourceInfo)
            val implementingModuleDescriptors = implementingModuleInfos.mapNotNull {
                KotlinCacheService.getInstance(module.project).getResolutionFacadeByModuleInfo(it, it.platform)?.moduleDescriptor
            }
            CachedValueProvider.Result(
                implementingModuleDescriptors,
                *(implementingModuleInfos.map { it.createModificationTracker() } +
                        ProjectRootModificationTracker.getInstance(module.project)).toTypedArray()
            )
        })
    }

val ModuleDescriptor.implementedDescriptors: List<ModuleDescriptor>
    get() {
        val moduleSourceInfo = getCapability(ModuleInfo.Capability) as? ModuleSourceInfo ?: return emptyList()

        return moduleSourceInfo.expectedBy.mapNotNull {
            KotlinCacheService.getInstance(moduleSourceInfo.module.project)
                .getResolutionFacadeByModuleInfo(it, it.platform)?.moduleDescriptor
        }
    }
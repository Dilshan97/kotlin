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

package org.jetbrains.kotlin.metadata.deserialization

import org.jetbrains.kotlin.metadata.ProtoBuf

class TypeTable(typeTable: ProtoBuf.TypeTable) {
    val types: List<ProtoBuf.Type> = run {
        val originalTypes = typeTable.typeList
        if (typeTable.hasFirstNullable()) {
            val firstNullable = typeTable.firstNullable
            typeTable.typeList.mapIndexed { i, type ->
                if (i >= firstNullable) {
                    type.toBuilder().setNullable(true).build()
                }
                else type
            }
        }
        else originalTypes
    }

    operator fun get(index: Int): ProtoBuf.Type = types[index]
}

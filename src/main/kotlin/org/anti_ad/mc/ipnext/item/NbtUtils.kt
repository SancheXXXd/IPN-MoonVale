/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2019-2020 jsnimda <7615255+jsnimda@users.noreply.github.com>
 *   Copyright (c) 2021-2022 Plamen K. Kosseff <p.kosseff@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anti_ad.mc.ipnext.item

import com.mojang.brigadier.StringReader
import net.minecraft.commands.arguments.NbtPathArgument
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CollectionTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NumericTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.TagParser
import net.minecraft.nbt.NbtUtils as VanillaNbtUtils
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item

import org.anti_ad.mc.ipnext.Log
import org.anti_ad.mc.common.extensions.AsComparable
import org.anti_ad.mc.common.extensions.asComparable
import org.anti_ad.mc.common.extensions.compareTo
import org.anti_ad.mc.common.extensions.tryCatch
import org.anti_ad.mc.common.extensions.tryOrPrint
import org.anti_ad.mc.common.extensions.trySwallow
import org.anti_ad.mc.common.extensions.unlessIt
import org.anti_ad.mc.ipnext.ingame.`(asString)`
import org.anti_ad.mc.ipnext.ingame.`(getByIdentifier)`
import org.anti_ad.mc.ipnext.ingame.`(type)`
import org.anti_ad.mc.ipnext.item.ComponentUtils.toFilteredNbtOrNull
import java.util.*

// ============
// vanillamapping code depends on mappings
// ============

object NbtUtils {
    // ============
    // Vanilla Item
    // ============
    fun getItemFromId(id: Identifier): Item? {
        return BuiltInRegistries.ITEM.`(getByIdentifier)`(id)
    }

    fun getTagFromId(id: Identifier): List<Item>? {
        return BuiltInRegistries.ITEM.tags.iterator().asSequence().firstOrNull {
            it.key().location == id
        }?.map { item ->
            item.value()
        }
    }

    // ============
    // nbt
    // ============
    fun compareNbt(a: CompoundTag?,
                   b: CompoundTag?): Int {
        val b1 = a == null
        val b2 = b == null
        if (b1 != b2)
            return if (b1) -1 else 1 // no nbt = first
        if (a == null || b == null) return 0
        val keys1: List<String> = a.keySet().sorted()
        val keys2: List<String> = b.keySet().sorted()
        val pairs1 = keys1.map { (it to a.get(it)).asComparable(::compareStringTag) }
        val pairs2 = keys2.map { (it to b.get(it)).asComparable(::compareStringTag) }
        return pairs1.compareTo(pairs2)
    }

    private fun compareStringTag(p1: Pair<String, Tag?>,
                                 p2: Pair<String, Tag?>): Int {
        val (key1, tag1) = p1
        val (key2, tag2) = p2
        val result = key1.compareTo(key2)
        if (result != 0) return result
        if (tag1 == null || tag2 == null) return 0 // actually they should be non null
        return tag1.compareTo(tag2)
    }

    fun Tag.compareTo(other: Tag): Int {
        val w1 = WrappedTag(this)
        val w2 = WrappedTag(other)
        return when {
            w1.isNumber -> if (w2.isNumber) w1.asDouble.compareTo(w2.asDouble) else null
            w1.isCompound -> if (w2.isCompound) compareNbt(w1.asCompound,
                                                           w2.asCompound) else null
            w1.isList -> if (w2.isList) w1.asListComparable.compareTo(w2.asListComparable) else null

            w1.isString -> if (w2.isString) w1.asString.removeSurrounding("\"").compareTo(w2.asString.removeSurrounding("\"")) else null
            else -> null
        } ?: w1.asString.compareTo(w2.asString)
    }

    fun parseNbt(nbt: String): CompoundTag? {
        // TagParser
        return tryCatch { TagParser.parseCompoundFully(nbt) }
    }

    fun parseNbtOrEmpty(nbt: String): Tag {
        return tryCatch {
            TagParser.create(NbtOps.INSTANCE).parseFully(StringReader(nbt))
        } ?: CompoundTag()
    }

    // ============
    // match nbt
    // ============
    fun matchNbtNoExtra(a: Tag?,
                        b: Tag?): Boolean { // b superset of a (a <= b)
        if (a == null) return true
        if (b == null) return false

        val wrappedA = WrappedTag(a)
        val wrappedB = WrappedTag(b)

        if (!wrappedA.sameType(wrappedB)) return false
        return when {
            wrappedA.isNumber -> wrappedA.asNumber == wrappedB.asNumber
            wrappedA.isString -> wrappedA.asString == wrappedB.asString
            wrappedA.isList -> {
                a as ListTag
                b as ListTag
                a.size == b.size && a.containsAll(b)
            }
            else -> {
                a as CompoundTag
                b as CompoundTag
                return a.unlessIt { isEmpty } == b.unlessIt { isEmpty }
            }
        }
    }

    fun matchNbt(a: Tag?,
                 b: Tag?): Boolean { // b superset of a (a <= b)
        if (a == null) return true
        if (b == null) return false

        val wrappedA = WrappedTag(a)
        val wrappedB = WrappedTag(b)

        if (!wrappedA.sameType(wrappedB)) return false
        return when {
            wrappedA.isNumber -> wrappedA.asNumber == wrappedB.asNumber
            wrappedA.isString -> wrappedA.asString == wrappedB.asString
            wrappedA.isList -> {
                a as ListTag
                b as ListTag
                b.containsAll(a)
            }
            else -> {
                a as CompoundTag
                b as CompoundTag
                if (a.isEmpty && b.isEmpty) return true
                if (b.isEmpty) return false

                innerMatchNbt(a, b)
            }
        }
    }

    class NbtPath(val value: NbtPathArgument.NbtPath) { // wrapper class to avoid direct imports to vanilla code
        companion object {
            fun of(string: String): NbtPath? {
                return getNbtPath(string)?.let { NbtPath(it) }
            }
        }

        fun getTags(itemType: ItemType, componentType: DataComponentType<*>?): List<WrappedTag> {
            val tag = itemType.tag
            tag ?: return listOf()
            val nbt = componentType?.toFilteredNbtOrNull(tag.get(componentType))
            nbt ?: return listOf()
            return getTagsForPath(value, nbt).map { WrappedTag(it) }
        }
    }

    class WrappedTag(val value: Tag) {
        val isString: Boolean
            get() = value.`(type)` == 8
        val isNumber: Boolean
            get() = value.`(type)` in 1..6
        val isCompound: Boolean
            get() = value.`(type)` == 10
        val isList: Boolean
            get() = value.`(type)` in listOf(7,
                                             9,
                                             11,
                                             12)
        val asString: String
            get() = value.`(asString)`
        val asNumber: Number // todo what if number is long > double precision range
            get() = (value as? NumericTag)?.asDouble()?.get() ?: 0
        val asDouble: Double
            get() = (value as? NumericTag)?.asDouble()?.get() ?: 0.0
        val asCompound: CompoundTag
            get() = value as? CompoundTag ?: CompoundTag()
        val asList: List<WrappedTag>
            get() = (value as? CollectionTag)?.map { WrappedTag(it) } ?: listOf()
        val asListUnwrapped: List<Tag>
            get() = (value as? CollectionTag)?.toList() ?: listOf()
        val asListComparable: List<AsComparable<Tag>>
            get() = asListUnwrapped.map { it.asComparable { a, b -> a.compareTo(b) } }

        fun sameType(other: WrappedTag): Boolean {
            return isList && other.isList ||
                   isString && other.isString ||
                   isCompound && other.isCompound ||
                   isNumber && other.isNumber
        }

        fun copy(): WrappedTag {
            return WrappedTag(value.copy())
        }

    }

    // ============
    // private
    // ============
    private fun innerMatchNbt(a: CompoundTag?,
                              b: CompoundTag?): Boolean { // b superset of a (a <= b)

        return VanillaNbtUtils.compareNbt(a,
                                          b,
                                          true) // criteria, testTarget, allowExtra (for list)
    }

    private fun getNbtPath(path: String): NbtPathArgument.NbtPath? {
        // NbtPathArgumentType().parse(StringReader(path))
        return tryOrPrint(Log::warn) { NbtPathArgument().parse(StringReader(path)) }
    }

    private fun getTagsForPath(nbtPath: NbtPathArgument.NbtPath,
                               target: Tag): List<Tag> {
        return trySwallow(listOf()) { nbtPath.get(target) }
    }

    fun Tag?.nullIfEmpty(): Tag? {
        return if (this == null) {
            null
        }
        else if (this is CompoundTag && this.isEmpty || this is ListTag && this.isEmpty()) {
            null
        } else {
            this
        }
    }
}

/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2024 Plamen K. Kosseff <p.kosseff@gmail.com>
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

import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.tags.EnchantmentTags
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.component.MapPostProcessing
import net.minecraft.world.item.component.TypedEntityData
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.saveddata.maps.MapId

import org.anti_ad.mc.common.vanilla.Vanilla

import org.anti_ad.mc.ipnext.Log
import org.anti_ad.mc.ipnext.ingame.`(itemType)`
import org.anti_ad.mc.ipnext.item.NbtUtils.compareTo
import java.util.*
import java.util.function.*
import kotlin.Comparator

@Suppress("UNCHECKED_CAST")
object ComponentUtils {

    private val registry: MutableMap<String, Comparator<Any?>> = mutableMapOf()

    private val ItemEnchantments.score: Double
        get() {
            return -1 * this.entrySet().fold(0.0) { acc, (enchantment, level) ->
                acc + if (enchantment.`is`(EnchantmentTags.CURSE)) -0.001 else level.toDouble() / enchantment.value().maxLevel
            }
        }

    private val PotionContents.score: Double
        get() {
            return if (this.hasEffects()) {
                this.allEffects.toList().fold(0.0) {acc, statusEffectInstance ->
                    val effectType = statusEffectInstance.effect.value()
                    @Suppress("SENSELESS_NULL_IN_WHEN")
                    var res: Double = when(effectType.category) {
                        MobEffectCategory.HARMFUL       -> {
                            -5.0
                        }
                        MobEffectCategory.NEUTRAL, null -> {
                            0.0
                        }
                        MobEffectCategory.BENEFICIAL -> {
                            5.0
                        }
                    }
                    res += statusEffectInstance.duration / 20.0 / 1000.0
                    res += statusEffectInstance.amplifier * 5.0
                    res += if (statusEffectInstance.isInfiniteDuration) 5.0 else 0.0
                    effectType.category
                    acc + res * -1
                }
            } else 0.0
        }

    private val DEFAULT_COMPARATOR: Comparator<Any?> = nullsLast { _: Any, _: Any ->
        0
    }

    private val typedEntityDataComparator: Comparator<TypedEntityData<*>> = Comparator { a: TypedEntityData<*>, b: TypedEntityData<*> ->
        if (a.type().javaClass === b.type().javaClass) {
            a.copyTagWithoutId().compareTo(b.copyTagWithoutId())
        } else {
            0
        }
    }.apply {
        registry[DataComponents.BLOCK_ENTITY_DATA.toString()] = this as Comparator<Any?>
        registry[DataComponents.ENTITY_DATA.toString()] = this as Comparator<Any?>
    }

    private val nbtComparator: Comparator<CustomData?> = nullsLast { a: CustomData, b: CustomData ->
        a.copyTag().compareTo(b.copyTag())
    }.apply {
        registry[DataComponents.CUSTOM_DATA.toString()] = this as Comparator<Any?>
        registry[DataComponents.BUCKET_ENTITY_DATA.toString()] = this as Comparator<Any?>
    }

    private val textComparator: Comparator<Component?> = nullsLast( compareBy<Component> { it.contents.toString() }
                                                                   .thenBy { it.siblings?.size ?: 0 }
                                                                   .thenComparing { a, b ->
                                                                       a.siblings.forEachIndexed { i, text ->
                                                                           getTextComparator().compare(text, b.siblings[i]).let {
                                                                               if (it != 0) return@thenComparing it
                                                                           }
                                                                       }
                                                                       0
                                                                   }
                                                             ).apply {
        registry[DataComponents.CUSTOM_NAME.toString()] = this as Comparator<Any?>
        registry[DataComponents.ITEM_NAME.toString()] = this as Comparator<Any?>
    }
    private fun getTextComparator(): Comparator<Component?> = textComparator

    private val dyedComponentComparator = nullsLast( compareBy<DyedItemColor> { it.rgb }).apply {
        registry[DataComponents.DYED_COLOR.toString()] = this as Comparator<Any?>
    }

    private val mapIdComparator = nullsLast( compareBy<MapId> {
        it.id
    }).apply {
        registry[DataComponents.MAP_ID.toString()] = this as Comparator<Any?>
    }

    private val mapMapPostProcessingComponentComparator = nullsLast( compareBy<MapPostProcessing> { it.id() })
        .apply {
            registry[DataComponents.MAP_POST_PROCESSING.toString()] = this as Comparator<Any?>
        }

    val itemTypeComparator = nullsLast ( compareBy<ItemType> { itemType -> itemType.itemId }
                                             .thenBy { itemType -> itemType.vanillaStack.count }
                                             .thenComparator { itemType1, itemType2 -> compareComponents(itemType1, itemType2) }
                                       )


    private val chargedProjectilesComponentComparator = nullsLast(
            compareBy<ChargedProjectiles> { it.items.size.inv() }.thenComparator { o1, o2 ->

                val l1: List<ItemType> = o1.items.map { it.create().`(itemType)` }.sortedWith(itemTypeComparator)
                val l2: List<ItemType> = o2.items.map { it.create().`(itemType)` }.sortedWith(itemTypeComparator)
                l1.forEachIndexed { index, itemType ->
                    val v = itemTypeComparator.compare(itemType, l2[index])
                    if (v != 0) return@thenComparator v
                }
                0
            })
        .apply {
            registry[DataComponents.CHARGED_PROJECTILES.toString()] = this as Comparator<Any?>
        }

    private val itemEnchantmentsComponentComparator = nullsLast (compareBy<ItemEnchantments> { it.score }).apply {
        registry[DataComponents.ENCHANTMENTS.toString()] = this as Comparator<Any?>
    }

    private val potionEffectsComponentComparator = nullsLast (
            compareBy<PotionContents> { it.score }).apply {
        registry[DataComponents.POTION_CONTENTS.toString()] = this as Comparator<Any?>
    }



    fun DataComponentType<*>?.comparatorFor(): Comparator<Any?> {
        registry[this.toString()]?.let {
            return it
        }
        if (this is Comparable<*>) {
            return nullsLast ( compareBy<Comparable<*>> { it } ) as Comparator<Any?>
        }
        return DEFAULT_COMPARATOR
    }

    fun compareComponents(item: ItemType, item1: ItemType): Int {
        val tag = item.tag
        val tag1 = item1.tag
/*
        val b1 = tag == null
        val b2 = tag1 == null
        if (b1 != b2)
            return if (b1) -1 else 1 // no nbt = first
        if (tag == null || tag1 == null) return 0
*/
        val list = tag.keySet().toList().sortedBy { it.toString() }
        val list1 = tag1.keySet().toList().sortedBy { it.toString() }
        val matching = list.filter {
            it in list1
        }
        matching.forEach {
            val c1 = tag[it]
            val c2 = tag1[it]
            Log.trace("c1: ${c1?.javaClass}, c2: ${c2?.javaClass}")
            if (c1 is Comparable<*>) {
                val cmp = (c1 as Comparable<Any?>).compareTo(c2)
                if (cmp != 0) return  cmp
            }
            val comparator = it?.comparatorFor()
            comparator?.compare(c1, c2)?.let { cmp ->
                if (cmp != 0) return cmp
            }
        }
        return tag.size().compareTo(tag1.size())
    }

    fun DataComponentType<*>.toFilteredNbtOrNull(value: Any?): Tag? {
        if (value == null) return null
        val codecAny: Codec<Any> = this.codecOrThrow() as Codec<Any>
        if (value is Tag) return value.copy()
        val opt = codecAny.encodeStart(Vanilla.world().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                                       value).resultOrPartial()
        return if (opt.isPresent) {
            when (this) {
                DataComponents.ENCHANTMENTS -> {
                    (opt.get() as CompoundTag).sanitizeEnchantments()
                }

                DataComponents.DAMAGE       -> {
                    null
                }

                DataComponents.CUSTOM_NAME  -> {
                    null
                }

                else                            -> {
                    opt.get()
                }
            }
        } else null
    }

    fun DataComponentType<*>.toFullNbtOrNull(value: Any?): Tag? {
        if (value == null) return null
        val codecAny: Codec<Any> = this.codecOrThrow() as Codec<Any>
        if (value is Tag) return value.copy()
        val opt = codecAny.encodeStart(Vanilla.world().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                                       value).resultOrPartial()
        return if (opt.isPresent) {
            opt.get()
        } else null
    }


    private fun CompoundTag.sanitizeEnchantments(): Tag {
        //val levels = this["levels"] as CompoundTag
        val newLevels = ListTag()
        this.keySet().forEach { it ->
            Log.trace("found level $it")
            newLevels.add(StringTag.valueOf(it))
        }

        return newLevels
    }

    fun DataComponentPatch.`(withRemovedIf)`(predicate: Predicate<DataComponentType<*>>): DataComponentPatch = this.forget(predicate)

}

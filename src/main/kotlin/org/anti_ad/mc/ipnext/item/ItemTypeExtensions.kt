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

@file:Suppress("ObjectPropertyName")

package org.anti_ad.mc.ipnext.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Items
import net.minecraft.world.item.MobBucketItem
import net.minecraft.world.item.SolidBucketItem
import net.minecraft.world.item.component.Consumable
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect
import net.minecraft.world.item.consume_effects.ConsumeEffect
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.equipment.Equippable
import net.minecraft.world.level.block.ShulkerBoxBlock

import org.anti_ad.mc.common.extensions.ifTrue
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.common.vanilla.alias.glue.I18n
import org.anti_ad.mc.ipnext.Log
import org.anti_ad.mc.ipnext.compat.integrations.Integrations
import org.anti_ad.mc.ipnext.config.CreativeMenuSortOrder
import org.anti_ad.mc.ipnext.config.SortSettings
import org.anti_ad.mc.ipnext.ingame.*
import org.anti_ad.mc.ipnext.mixin.*
import org.anti_ad.mc.ipnext.mixinhelpers.IMixinItemGroup
import java.util.*
import net.minecraft.world.item.ItemStack as VanillaItemStack

// ============
// vanillamapping code depends on mappings
// ============

fun ItemType.fullItemInfoAsJson(): String {
    var res = "{" + "\n\t\"id\" : \"" + this.itemId + "\",\n"
    tag.keySet().forEach {
        res += "\t{\n\t\t\"$it\" : ${tag.get(it)}\n},"
    }
    return "$res\n}"
}

fun ItemType.toNamespacedString(): String { // like ItemType.toString() but with namespace
    return itemId + (tag ?: "")
}

val ItemType.Companion.EMPTY
    get() = ItemType(Items.AIR, PatchedDataComponentMap(Items.AIR.components()),
                     DataComponentPatch.EMPTY,
                     { false })

fun ItemType.isEmpty(): Boolean {
    return item == Items.AIR
    //air has components now so removed the check for it
}

inline val ItemType.isShulker: Boolean
    get() = item is BlockItem && item.block is ShulkerBoxBlock

val ItemType.isEmptyShulker: Boolean
    get() {
        val iss = isShulker
        return iss && (tag?.get(DataComponents.CONTAINER).isEmpty())
    }

private fun ItemContainerContents?.isEmpty(): Boolean {
    return this == null || this === ItemContainerContents.EMPTY || this == ItemContainerContents.EMPTY
}

inline val ItemType.maxCount: Int
    get() {
        val carpetEmptyShulkersStackSize = Integrations.carpetEmptyShulkersStackSize
        return if (carpetEmptyShulkersStackSize > 1 && isEmptyShulker) {
            carpetEmptyShulkersStackSize
        } else {
            vanillaStack.maxStackSize
        }
    }

inline val ItemType.searchItemStack: VanillaItemStack
    get() {
        return VanillaItemStack(this.item, 1)
    }

inline val ItemType.vanillaStack: VanillaItemStack
    get() {
        val tag = this@vanillaStack.tag.copy()
        tag.applyPatch(this@vanillaStack.changes)
        val stack = VanillaItemStack(BuiltInRegistries.ITEM.wrapAsHolder(item), 1, tag)
        return stack
    }

fun ItemType.vanillaStackWithCount(count: Int): VanillaItemStack =
    VanillaItemStack(BuiltInRegistries.ITEM.wrapAsHolder(this.item),
                     count,
                     this@vanillaStackWithCount.tag as PatchedDataComponentMap) // nbt was tag

inline val ItemType.identifier: Identifier
    get() = BuiltInRegistries.ITEM.getKey(item)

inline val ItemType.namespace: String
    get() = identifier.namespace

inline val ItemType.itemClass
    get() = item.javaClass

//region ItemType String Relative

inline val ItemType.hasCustomName: Boolean
    get() = this.vanillaStack.components[DataComponents.CUSTOM_NAME] != null
inline val ItemType.customName: String
    get() = if (hasCustomName) displayName else ""
inline val ItemType.displayName: String
    get() = vanillaStack.displayName.string
inline val ItemType.translatedName: String
    get() = I18n.translate(translationKey)
inline val ItemType.itemId: String
    get() = identifier.toString()
inline val ItemType.translationKey: String
    get() = item.descriptionId
inline val ItemType.isStackable: Boolean
    get() = vanillaStack.isStackable

inline val ItemType.customOrTranslatedName: String
    get() = if (hasCustomName) displayName else I18n.translate(translationKey)

//endregion

//region ItemType Number Relative

inline val CreativeModeTab.`(priorityIndex)`: Int
    get() {
        if (this is IMixinItemGroup) {
            var index = ipnPriorityIndex
            if (index == -1) {
                val content = this.displayName.contents
                if (content is TranslatableContents) {
                    index = ItemTypeExtensionsObject.translationKeysPriorityList.indexOf(content.key)
                    ipnPriorityIndex = if (index == -1) {
                        Int.MAX_VALUE
                    } else {
                        index
                    }
                }
            }
            return index
        }
        return Int.MAX_VALUE
    }

private class ItemGroupComparator: Comparator<CreativeModeTab> {

    override fun compare(g1: CreativeModeTab?,
                         g2: CreativeModeTab?): Int {

        val defaultCompare = {
            if (g1 == null && g2 == null) {
                0
            } else {
                if (g1 == null) {
                    -1
                } else {
                    if (g2 == null) {
                        1
                    } else {
                        if (g1.row() == g2.row()) {
                            g1.column().compareTo(g2.column())
                        } else {
                            g1.row().compareTo(g2.row())
                        }
                    }
                }
            }
        }

        val priorityCompare = {
            if (g1 == null && g2 == null) {
                0
            } else {
                if (g1 == null) {
                    -1
                } else {
                    if (g2 == null) {
                        1
                    } else {
                        g1.`(priorityIndex)`.compareTo(g2.`(priorityIndex)`)
                    }
                }
            }
        }

        return if (SortSettings.CREATIVE_SORT_ORDER_TYPE.value == CreativeMenuSortOrder.SEARCH_TAB)
            defaultCompare()
        else
            priorityCompare()
    }
}

private fun initGroupIndex() {
    val player = Vanilla.playerNullable()
    val features = player?.connection?.enabledFeatures()
    val lookup = player?.level()?.registryAccess()
    val opTabEnabled = Vanilla.mc().options.operatorItemsTab().get() && player?.canUseGameMasterBlocks() ?: false
    if (features != null && lookup != null) {
        CreativeModeTabs.tryRebuildTabContents(features, opTabEnabled, lookup).ifTrue {
            val sm = player.connection.searchTrees()
            val list = CreativeModeTabs.searchTab().displayItems.toList()
            sm.updateCreativeTooltips(lookup, list)
            sm.updateCreativeTags(list)
        }
    }
}

val ItemType.searchTabIndex: Int
    get() {
        initGroupIndex()
        val index = CreativeModeTabs.searchTab().searchTabDisplayItems.indexOfFirst { itemStack ->
            @Suppress("SENSELESS_COMPARISON")
            itemStack != null && itemStack.`(itemType)`.item == this.item
        }
        if (index == -1) {
            Log.warn("Item ${this.itemId} not present in the Creative Inventory Search Tab!")
        }
        return index
    }

val ItemType.groupIndex: Int
    get() {
        val stack = this.searchItemStack
        initGroupIndex()
        var result = Int.MAX_VALUE
        val grps = CreativeModeTabs.allTabs().filter {
            val grType = it.type ?: CreativeModeTab.Type.INVENTORY
            grType === CreativeModeTab.Type.CATEGORY
        }.sortedWith(ItemGroupComparator())

        run earlyStop@ {
            grps.forEachIndexed { index, itemGroup ->
                if (itemGroup.contains(stack)) {
                    result = index
                    return@earlyStop
                }
            }
        }
        if (result in 0..(grps.size-1)) {
            val grp = grps[result]
            Log.trace("${this.identifier} - > $result -> ${grp?.displayName}")
        }
        return result
    }

inline val ItemType.`(foodComponent)`: FoodProperties
    get() = item.components().get(DataComponents.FOOD) ?: error("this shouldn't happen")

inline val ItemType.`(consumableComponent)`: Consumable
    get() = item.components().get(DataComponents.CONSUMABLE) ?: error("this shouldn't happen")

inline val ItemType.`(isFood)`: Boolean
    get() = item.components().get(DataComponents.FOOD) != null


inline val Consumable.`(consumeEffects)`: List<ConsumeEffect>
    get() = mutableListOf<ConsumeEffect>().also { ls ->
        this.onConsumeEffects.forEach {
            ls.add(it)
        }
    }

inline val Consumable.`(isHarmful)`: Boolean
    get() {
        this.`(consumeEffects)`.forEach {
            if (it.type === ConsumeEffect.Type.APPLY_EFFECTS) {
                val effects = it as ApplyStatusEffectsConsumeEffect
                effects.effects.forEach {
                    if (it.effect.value()?.category === MobEffectCategory.HARMFUL) {
                        return true
                    }
                }
            }
        }
        return false
    }

inline val FoodProperties.`(saturationModifier)`
    get() = this.saturation

val COMPONENTS_CHANGES_CODEC: Codec<VanillaItemStack> = Codec.lazyInitialized {
    RecordCodecBuilder.create { instance ->
        instance.group(DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter {
            stack -> @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") stack?.componentsPatch
        }).apply(instance) { changes: DataComponentPatch? ->
            VanillaItemStack(BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getKey(Items.AIR)).get(), 0, changes ?: DataComponentPatch.EMPTY)
        }
    }
}

fun VanillaItemStack.`(componentsToCodecList)`(): MutableList<Codec<*>> {
    //PairCodec<ComponentType<*>, Optional<*>>
    val res = mutableListOf<Codec<*>>()
    if (!this.componentsPatch.isEmpty) {
        val split = this.componentsPatch.split()
        split.added().forEach { res.add(it.type().codecOrThrow()) }
        split.removed().forEach { res.add(it.codecOrThrow()) }
    }
    return res
}


inline val ItemType.`(componentsToNbt)`: Tag?
    get() = if (!this.isEmpty()) {
        COMPONENTS_CHANGES_CODEC.encodeStart(Vanilla.world().registryAccess().createSerializationContext(NbtOps.INSTANCE), this.vanillaStack).getOrThrow();
    } else {
        null
    }

inline val ItemType.rawId: Int
    get() = BuiltInRegistries.ITEM.`(getRawId)`(item)
inline val ItemType.damage: Int
    get() = vanillaStack.damageValue

inline val ItemType.`(enchantmentsScore)`: Double
    get() = EnchantmentHelper.getEnchantmentsForCrafting(vanillaStack).entrySet().fold(0.0) { acc, (enchantment, level) ->
        acc + if (enchantment.`is`(EnchantmentTags.CURSE)) -0.001 else level.toDouble() / enchantment.value().maxLevel
    } // cursed enchantments +0 scores

inline val ItemType.`(enchantments)`: MutableMap<ResourceKey<Enchantment>, Int>
    get() = EnchantmentHelper.getEnchantmentsForCrafting(vanillaStack).entrySet().associateTo (mutableMapOf()) {
        it.key.unwrapKey().get() to it.intValue
    }

inline val ItemType.isDamageable: Boolean
    get() = vanillaStack.isDamageableItem
inline val ItemType.maxDamage: Int
    get() = vanillaStack.maxDamage
inline val ItemType.durability: Int
    get() = if (isDamageableFn())  maxDamage - damage else -1

inline val ItemType.isBucket: Boolean
    get() = item is BucketItem || item is SolidBucketItem

inline val ItemType.isFullBucket: Boolean
    get() = item is MobBucketItem || item is SolidBucketItem || (item is IMixinBucketItem && !(item.fluid as IMixinFluid).callIsEmpty())

inline val ItemType.isEmptyBucket: Boolean
    get() {
        return item !is MobBucketItem && item is IMixinBucketItem && (item.fluid as IMixinFluid).callIsEmpty()
    }

fun ItemType.isEmptyComparedTo(other: ItemType): Boolean {
    val otherItem = other.item
    return  if (item is SolidBucketItem && otherItem is IMixinBucketItem && (otherItem.fluid as IMixinFluid).callIsEmpty()) {
        true
    } else if (otherItem == Items.BUCKET && item is IMixinBucketItem && !(item.fluid as IMixinFluid).callIsEmpty()) {
        true
    } else item is MobBucketItem && otherItem is IMixinBucketItem && otherItem !is MobBucketItem
    //item is MilkBucketItem || item is IMixinEntityBucketItem || (item is IMixinBucketItem && !(item.fluid as IMixinFluid).callIsEmpty())
}

fun ItemType.isFullComparedTo(other: ItemType): Boolean {
    val otherItem = other.item
    return if (item == Items.BUCKET && otherItem is SolidBucketItem) {
        true
    } else item !is MobBucketItem && otherItem is MobBucketItem

}

inline val ItemType.isHoneyBottle: Boolean
    get() = item == Items.HONEY_BOTTLE

inline val ItemType.isStew: Boolean
    get() = `(isFood)`
            && (vanillaStack.components[DataComponents.USE_REMAINDER]?.convertInto?.item == Items.BOWL && vanillaStack.components[DataComponents.USE_REMAINDER]?.convertInto?.count == 1)


//region ItemType Potion Relative

inline val ItemType.hasPotionName: Boolean
    get() = tag?.has(DataComponents.POTION_CONTENTS) ?: false

inline val ItemType.potionName: String
    get() = if (hasPotionName) tag.get(DataComponents.POTION_CONTENTS)?.potion?.get()?.value()?.name() ?: "" else ""

inline val ItemType.hasPotionEffects: Boolean
    get() = tag?.get(DataComponents.POTION_CONTENTS)?.hasEffects() ?: false

inline val ItemType.hasCustomPotionEffects: Boolean
    get() = tag?.get(DataComponents.POTION_CONTENTS)?.customEffects?.isNotEmpty() ?: false

inline val ItemType.potionEffects: List<MobEffectInstance>
    get() = tag?.get(DataComponents.POTION_CONTENTS)?.allEffects?.toList() ?: listOf()

inline val ItemType.comparablePotionEffects: List<PotionEffect>
    get() = potionEffects.map { it.`(asComparable)` }

@Suppress("ObjectPropertyName")
inline val MobEffectInstance.`(asComparable)`: PotionEffect
    get() = PotionEffect(BuiltInRegistries.MOB_EFFECT.getKey(this.effect.value()).toString(),
                         this.amplifier,
                         this.duration)

data class PotionEffect(val effect: String,
                        val amplifier: Int,
                        val duration: Int) : Comparable<PotionEffect> {

    override fun compareTo(other: PotionEffect): Int { // stronger first
        this.effect.compareTo(other.effect).let { if (it != 0) return it }
        other.amplifier.compareTo(this.amplifier).let { if (it != 0) return it }
        other.duration.compareTo(this.duration).let { if (it != 0) return it }
        return 0
    }
}

object ItemTypeExtensionsObject {

    val translationKeysPriorityList = mutableListOf<String>()

    fun priorityListChanged() {
        initGroupIndex()
        translationKeysPriorityList.clear()
        CreativeModeTabs.allTabs().forEach {
            (it as IMixinItemGroup).ipnPriorityIndex = -1;
        }
        SortSettings.CATEGORY_PRIORITY_LIST.value.split(",").forEach {
            translationKeysPriorityList.add(it.trim())
        }
    }

    private val local: ThreadLocal<Boolean> = ThreadLocal<Boolean>().apply { set(true) }

    fun defaultOrderListChanged() {
        if (local.get()) {
            initGroupIndex()
            local.set(false)
            SortSettings.CATEGORY_ORIGINAL_ORDER.value = makeDefaultList()
            local.set(true)
        }
    }

    fun makeDefaultList(): String {
        var newValue = ""
        CreativeModeTabs.allTabs().forEach {
            if (it.type == CreativeModeTab.Type.CATEGORY) {
                val content = it.displayName.contents
                if (content is TranslatableContents) {
                    newValue = if (newValue != "") "$newValue, ${content.key}" else content.key
                }
            }
        }
        return newValue
    }
}

inline val ItemType.`(equipmentSlot)`: EquipmentSlot?
    get() {
        val equipable: Equippable? = this.tag.get(DataComponents.EQUIPPABLE)
        return equipable?.slot()
    }

val ItemType.`(isBundle)`: Boolean
    get() {
        return tag.get(DataComponents.BUNDLE_CONTENTS) != null
    }

val ItemType.`(isBundleAndHasItems)`: Boolean
    get() {
        return tag.get(DataComponents.BUNDLE_CONTENTS)?.size()?.let { it: Int -> it > 0 } ?: false
    }

//endregion

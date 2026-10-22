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

@file:Suppress("ObjectPropertyName", "FunctionName")

package org.anti_ad.mc.ipnext.ingame

import com.mojang.blaze3d.platform.Window
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent

import net.minecraft.client.gui.components.EditBox

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.client.multiplayer.MultiPlayerGameMode

import net.minecraft.core.DefaultedRegistry
import net.minecraft.core.Holder

import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey

import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.StonecutterMenu
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.item.enchantment.Enchantment

import org.anti_ad.mc.alias.client.gui.screen.recipebook.`(searchField)`

import org.anti_ad.mc.common.math2d.Point
import org.anti_ad.mc.common.math2d.Rectangle
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.ipnext.item.EMPTY
import org.anti_ad.mc.ipnext.item.ItemStack
import org.anti_ad.mc.ipnext.item.ItemType
import org.anti_ad.mc.ipnext.item.MutableItemStack
import org.anti_ad.mc.ipnext.item.empty
import org.anti_ad.mc.ipnext.item.vanillaStack
import org.anti_ad.mc.ipnext.mixin.IMixinSlot
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.item.ItemStack as VanillaItemStack

// ============
// vanillamapping code depends on mappings
// ============

// use `()` to avoid potential mapping name collision

val VanillaItemStack.`(itemType)`: ItemType
    get() = ItemType(item,
                     components as PatchedDataComponentMap,
                     componentsPatch,
                     { this.isDamageableItem })
val VanillaItemStack.`(itemStack)`: ItemStack
    get() = if (isEmpty) ItemStack.EMPTY else ItemStack(`(itemType)`,
                                                        count)
val VanillaItemStack.`(mutableItemStack)`: MutableItemStack
    get() = if (isEmpty) MutableItemStack.empty() else MutableItemStack(`(itemType)`,
                                                                        count)

val AbstractContainerMenu.`(slots)`: List<Slot>
    get() = slots

val Slot.`(id)`: Int
    get() = index
val Slot.`(invSlot)`: Int
    get() = (this as IMixinSlot).invSlot
val Slot.`(itemStack)`: ItemStack
    get() = item.`(itemStack)`
val Slot.`(vanillaStack)`: VanillaItemStack
    get() = this.item

val Slot.`(mutableItemStack)`: MutableItemStack
    get() = item.`(mutableItemStack)`
val Slot.`(inventory)`: Container
    get() = container
val Slot.`(inventoryOrNull)`: Container?
    get() = container
val Slot.`(left)`: Int
    get() = x
val Slot.`(top)`: Int
    get() = y
val Slot.`(topLeft)`: Point
    get() = Point(`(left)`,
                  `(top)`)

fun Slot.`(canInsert)`(itemStack: ItemStack): Boolean {
    return mayPlace(itemStack.vanillaStack)
}

val Screen.`(focusedSlot)`: Slot?
    get() = (this as? AbstractContainerScreen<*>)?.`(rawFocusedSlot)`?.let {
        vPlayerSlotOf(it,
                      this)
    }

val AbstractContainerScreen<*>.`(rawFocusedSlot)`: Slot?
    get() = hoveredSlot
val AbstractContainerScreen<*>.`(containerBounds)`: Rectangle
    get() = Rectangle(leftPos,
                      topPos,
                      imageWidth,
                      imageHeight)

val AbstractContainerScreen<*>.`(container)`: AbstractContainerMenu
    get() = menu

var Inventory.`(selectedSlot)`: Int
    get() = selectedSlot
    set(value) {
        selectedSlot = value
    }

val CreativeModeInventoryScreen.`(isInventoryTab)`: Boolean // method_2469() == ItemGroup.INVENTORY.getIndex()
    get() = this.isInventoryOpen

val ItemEnchantments.`(enchantments)`: Set<Holder<Enchantment>>
    get() = this.keySet()

// ============
// Registry
// ============

fun <T: Any> DefaultedRegistry<T>.`(getRawId)`(value: T): Int {
    return getId(value)
}

fun <T: Any> DefaultedRegistry<T>.`(getByIdentifier)`(id: Identifier): T {
    return getValue(id)
}

fun <T: Any> Holder<T>.`(is)`(id: Identifier): Boolean = this.`is`(id)
fun <T: Any> Holder<T>.`(is)`(id: TagKey<T>): Boolean = this.`is`(id)
fun <T: Any> Holder<T>.`(is)`(id: ResourceKey<T>): Boolean = this.`is`(id)
val <T: Any> Holder<T>.`(key)`: ResourceKey<T>?
    get() = this.unwrapKey().getOrNull()
val <T: Any> Holder<T>.`(value)`: T?
    get() = this.value()
val ResourceKey<*>.`(registry)`: Identifier
    get() = this.registry()
val ResourceKey<*>.`(identigier)`: Identifier
    get() = this.identifier()

// ============
// nbt Tag
// ============

val Tag.asString: String
    get() = this.asString().getOrElse { this.toString() }


val Tag.`(type)`: Int
    get() = id.toInt()
val Tag.`(asString)`: String
    get() = this.asString

val CompoundTag.`(keys)`: Set<String>
    get() = keySet()

val Minecraft.`(window)`: Window
    get() = window

val Minecraft.`(options)`: Options
    get() = options

val Options.`(keyDrop)`: KeyMapping
    get() = keyDrop

val KeyMapping.`(isPressed)`: Boolean
    get() = isDown

val Window.`(scaledWidth)`: Int
    get() = guiScaledWidth

val Window.`(scaledHeight)`: Int
    get() = guiScaledHeight

fun MultiPlayerGameMode.`(clickSlot)`(i: Int, j: Int, k: Int, slotActionType: ContainerInput, playerEntity: Player) =
        this.handleContainerInput(i, j, k, slotActionType, playerEntity)

fun InventoryMenu.`(onSlotClick)`(slotIndex: Int, button: Int, actionType: ContainerInput, player: Player) =
        this.clicked(slotIndex, button, actionType, player)

fun InventoryMenu.`(sendContentUpdates)`() = broadcastChanges()

val StonecutterMenu.`(selectedRecipe)`: Int
    get() = selectedRecipeIndex

fun Minecraft.`(send)`(runnable: Runnable) = schedule(runnable)

// interpreted for creative inventory
// in-game safe
fun vFocusedSlot(): Slot? = Vanilla.screen()?.`(focusedSlot)`

fun vMainhandIndex() =
        Vanilla.playerInventory().`(selectedSlot)`

fun isInputFieldActive(scr: AbstractContainerScreen<*>): Boolean {
    return scr.children()?.any {
        (it is RecipeBookComponent<*> && (it.`(searchField)`?.isFocused == true))
        || (it is EditBox && it.isFocused)
    } == true
}

fun vCursorStack() = Vanilla.playerInventory().player.containerMenu.carried?.`(itemStack)` ?: ItemStack.EMPTY

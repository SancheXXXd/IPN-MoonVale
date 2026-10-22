/*
 * Inventory Profiles Next
 *
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

package org.anti_ad.mc.ipnext.event

import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.item.alchemy.PotionContents

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component

import org.anti_ad.mc.common.IInputHandler
import org.anti_ad.mc.common.extensions.transformOrNull
import org.anti_ad.mc.common.gui.NativeInputContextBase
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.common.vanilla.VanillaUtil
import org.anti_ad.mc.ipnext.Log
import org.anti_ad.mc.ipnext.config.EditProfiles
import org.anti_ad.mc.ipnext.config.Features
import org.anti_ad.mc.ipnext.config.GuiSettings
import org.anti_ad.mc.ipnext.config.Hotkeys
import org.anti_ad.mc.ipnext.event.autorefill.AutoRefillHandler
import org.anti_ad.mc.ipnext.ingame.`(asString)`
import org.anti_ad.mc.ipnext.ingame.`(enchantments)`
import org.anti_ad.mc.ipnext.ingame.`(identigier)`
import org.anti_ad.mc.ipnext.ingame.`(itemStack)`
import org.anti_ad.mc.ipnext.ingame.`(key)`
import org.anti_ad.mc.ipnext.ingame.`(selectedSlot)`
import org.anti_ad.mc.ipnext.ingame.`(slots)`
import org.anti_ad.mc.ipnext.ingame.`(vanillaStack)`
import org.anti_ad.mc.ipnext.ingame.vCursorStack
import org.anti_ad.mc.ipnext.inventory.ContainerClicker
import org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions
import org.anti_ad.mc.ipnext.item.ComponentUtils.toFilteredNbtOrNull
import org.anti_ad.mc.ipnext.item.ComponentUtils.toFullNbtOrNull
import org.anti_ad.mc.ipnext.item.ItemStack
import org.anti_ad.mc.ipnext.item.NbtUtils.compareTo
import org.anti_ad.mc.ipnext.item.customName
import org.anti_ad.mc.ipnext.item.identifier
import org.anti_ad.mc.ipnext.item.isEmpty
import org.anti_ad.mc.ipnext.item.itemId
import org.anti_ad.mc.ipnext.item.rule.file.RuleFileRegister
import org.anti_ad.mc.ipnext.parser.ProfilesLoader
import org.anti_ad.mc.ipnext.profiles.config.`(asIdentifier)`
import org.anti_ad.mc.ipnext.profiles.config.ProfileComponentData
import org.anti_ad.mc.ipnext.profiles.config.ProfileData
import org.anti_ad.mc.ipnext.profiles.config.ProfileItemData
import org.anti_ad.mc.ipnext.profiles.config.ProfileItemId
import org.anti_ad.mc.ipnext.profiles.config.ProfileSlot
import org.anti_ad.mc.ipnext.profiles.config.ProfileSlotId
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ProfileSwitchHandler: IInputHandler {

    val activeProfileName: String?
        get() {
            return if (targetProfile.valid) targetProfile.name else null
        }

    var activeProfileId = 0

    private var doApplyProfile: Boolean = false

    private val allSlots: List<Int> = (9..45).toList() + (5..8).toList()
    private val hotBarSlots: List<Int> = (0+36..8+36).toList()

    fun onTickInGame() {
        val screen = Vanilla.screen()
        if (VanillaUtil.inGame() && doApplyProfile && (screen == null || screen is InventoryScreen)) {
            doApplyProfile = false
            applyProfile()
        }
    }

    val quickProfileKeys = mapOf(Hotkeys.PROFILE_1 to EditProfiles.QUICK_SLOT_1_PROFILE,
                                 Hotkeys.PROFILE_2 to EditProfiles.QUICK_SLOT_2_PROFILE,
                                 Hotkeys.PROFILE_3 to EditProfiles.QUICK_SLOT_3_PROFILE,
                                 Hotkeys.PROFILE_4 to EditProfiles.QUICK_SLOT_4_PROFILE,
                                 Hotkeys.PROFILE_5 to EditProfiles.QUICK_SLOT_5_PROFILE,
                                 Hotkeys.PROFILE_6 to EditProfiles.QUICK_SLOT_6_PROFILE,
                                 Hotkeys.PROFILE_7 to EditProfiles.QUICK_SLOT_7_PROFILE,
                                 Hotkeys.PROFILE_8 to EditProfiles.QUICK_SLOT_8_PROFILE,
                                 Hotkeys.PROFILE_9 to EditProfiles.QUICK_SLOT_9_PROFILE,
                                 Hotkeys.PROFILE_10 to EditProfiles.QUICK_SLOT_10_PROFILE)



    private fun toProfileComponentDataOrNull(type: DataComponentType<*>, value: Any?): ProfileComponentData? {
        return type.toFilteredNbtOrNull(value).transformOrNull {
            ProfileComponentData(type.toString(), it)
        }
    }

    fun createProfileFromCurrentState(): ProfileData {
        val inventory = Vanilla.playerInventory()
        return ProfileData("Saved", ProfileSlotId.valueOf(inventory.`(selectedSlot)` + 36), mutableListOf<ProfileSlot>().apply {
            allSlots.forEach { slot->
                if (ProfileSlotId.valueOf(slot) != ProfileSlotId.NONE) {
                    val stack = Vanilla.playerContainer().`(slots)`[slot].`(itemStack)`
                    val vStack = Vanilla.playerContainer().`(slots)`[slot].`(vanillaStack)`
                    if (!stack.isEmpty()) {
                        add(ProfileSlot(ProfileSlotId.valueOf(slot), null, mutableListOf<ProfileItemData>().apply {
                            val customName = if (EditProfiles.INCLUDE_CUSTOM_NAME.booleanValue) stack.itemType.customName else ""
                            add(ProfileItemData(ProfileItemId.Id(stack.itemType.identifier), customName, vStack.componentsPatch.split().added().mapNotNull {
                                toProfileComponentDataOrNull(it.type(), it.value())
                            } ))
                        }))
                    }
                }
            }
        })
    }

    private fun applyProfile() {
        //Vanilla.inGameHud().setSubtitle(LiteralText(targetProfile.name))
        if (GuiSettings.ENABLE_PROFILES_ANNOUNCEMENT.booleanValue) {
            Vanilla.inGameHud().setOverlayMessage(Component.literal(targetProfile.name), false)
        }
        var sourceSlots = allSlots
        monitors.forEach {
            if (it.findAndSwap(sourceSlots)) {
                sourceSlots = sourceSlots - it.slot
            }
        }
        LockedSlotKeeper.init()
        if (targetProfile.active != ProfileSlotId.NONE) {
            val activate: Int = targetProfile.active.slotId;
            Vanilla.playerInventory().`(selectedSlot)` = activate - 36
        }
    }

    val EMPTY_PROFILE = ProfileData("", ProfileSlotId.NONE, emptyList(), false)

    private var targetProfile: ProfileData = EMPTY_PROFILE

    val monitors: MutableList<ProfileMonitor> = mutableListOf()

    fun applyCurrent(gui: Boolean = false) {
        doApplyProfile = targetProfile.valid
    }

    fun reloadActiveProfile() {
        val activeName = if (targetProfile.valid) activeProfileName else null
        activeProfileId = -1
        init(EMPTY_PROFILE)
        activeName?.let {
            switchToProfileName(it)
            doApplyProfile = false
        }
    }

    fun init(newProfile: ProfileData) {
        if (targetProfile == newProfile) {
            targetProfile = newProfile
            doApplyProfile = targetProfile.valid

        } else {
            monitors.clear()
            targetProfile = newProfile
            if (targetProfile.valid) {
                targetProfile.slots.forEach {
                    monitors.add(ProfileMonitor(it))
                }
                doApplyProfile = true
            }
        }
    }

    override fun onInput(context: NativeInputContextBase<*>,
                         lastKey: Int,
                         lastAction: Int): Boolean {

        if (!Features.ENABLE_PROFILES.booleanValue) return false

        if (!VanillaUtil.inGame()) return false

        val screen = Vanilla.screen()
        if (screen == null || screen is InventoryScreen) {

            if (Hotkeys.APPLY_PROFILE.isActivated()) {
                applyCurrent()
                return true
            }
            if (Hotkeys.NEXT_PROFILE.isActivated()) {
                nextProfile()
                return true
            }
            if (Hotkeys.PREV_PROFILE.isActivated()) {
                prevProfile()
                return true
            }

            quickProfileKeys.forEach { (key,  string) ->
                if (key.isActivated() && string.value != string.defaultValue) {
                    switchToProfileName(string.value)
                    return true
                }
            }

            if (Hotkeys.SAVE_AS_PROFILE.isActivated()) {
                val p = createProfileFromCurrentState()
                ProfilesLoader.savedProfiles.add(p)
                ProfilesLoader.save()
                VanillaUtil.chat(Component.translatable("ipnlegit.profiles.created_new_saved"))
                Log.trace("\n$p")
                return true
            }
        }

        return false
    }

    private fun switchToProfileName(name: String) {
        val index = byName(name)
        if (index != -1) {
            activeProfileId = index
            init(ProfilesLoader.profiles[index])
        }
    }

    fun prevProfile(gui: Boolean = false) {
        if (ProfilesLoader.profiles.isNotEmpty()) {
            init(ProfilesLoader.profiles[nextOrFirst()])
        }
    }

    fun nextProfile(gui: Boolean = false) {
        if (ProfilesLoader.profiles.isNotEmpty()) {
            init(ProfilesLoader.profiles[prevOrLast()])
        }
    }

    class ProfileMonitor(private val slotData: ProfileSlot) {

        val slot: Int get() = this.slotData.id.slotId
        private val sortOrder get() = this.slotData.order
        private val targetItems get() = this.slotData.items

        fun findAndSwap(sourceSlots: List<Int>): Boolean {
            val currentItem = Vanilla.playerContainer().`(slots)`[slot].`(itemStack)`
            Log.trace("found ${currentItem.itemType.itemId} in slot $slot")
            if (targetItems.isNotEmpty()) {
                val swapWith: Int? = targetItems.findIn(sourceSlots, ::bestMatch)
                if (swapWith != null) {
                    if (slot != swapWith) {
                        Log.trace("swapping $swapWith to $slot")
                        //clicks.add(Pair(slot, swapWith))
                        swapSlots(slot, swapWith)
                    }
                    return true
                }
            } else {
                val preferLocked = true
                val preferNonHotbar = true
                var targets = if (Features.ENABLE_LOCK_SLOTS.booleanValue && LockedSlotKeeper.isMultiPlayer) {
                    if (preferLocked && LockedSlotKeeper.emptyLockedSlots.isNotEmpty()) {
                        LockedSlotKeeper.emptyLockedSlots.toList()
                    } else {
                        LockedSlotKeeper.emptyNonLockedSlots.toList()
                    }
                } else {
                    allSlots.filter {
                        Vanilla.playerContainer().`(slots)`[it].`(itemStack)`.isEmpty()
                    }.toList()
                }

                if (preferNonHotbar) {
                    val noHotBar = targets - hotBarSlots
                    if (noHotBar.isNotEmpty()) {
                        targets = noHotBar
                    }
                }

                targets.forEach {
                    if (Vanilla.playerContainer().`(slots)`[it].`(itemStack)`.isEmpty()) {
                        swapSlots(slot, it)
                        return true
                    }
                }
            }
            return false
        }

        private fun swapSlots(to: Int, foundSlotId: Int) {
            AutoRefillHandler.profilesSwappedItems.add(to)
            GeneralInventoryActions.cleanCursor()
            if ((to - 36) in 0..8) { // use swap
                //handles hotbar
                ContainerClicker.swap(foundSlotId,
                                      to - 36)
            } else {
                //handles offhand and armor slots
                ContainerClicker.leftClick(foundSlotId)
                ContainerClicker.leftClick(to)
                if (!vCursorStack().isEmpty()) {
                    ContainerClicker.leftClick(foundSlotId) // put back
                }
            }
        }

        private fun bestMatch(to: ProfileItemData, from: List<Int>): Int? {
            val rule = RuleFileRegister.getCustomRuleOrEmpty(this.sortOrder ?: "auto_refill_best")

            val sorted = from.filter {
                val itemStack = Vanilla.playerContainer().`(slots)`[it].`(itemStack)`
                !itemStack.isEmpty() && to.match(itemStack)
            }.sortedWith { i, j ->
                val a = Vanilla.playerContainer().`(slots)`[i].`(itemStack)`.itemType;
                val b = Vanilla.playerContainer().`(slots)`[j].`(itemStack)`.itemType;
                rule.compare(a, b)
            }

            Log.trace("$to :: ${sorted.joinToString { Vanilla.playerContainer().`(slots)`[it].`(itemStack)`.itemType.itemId }}")
            return sorted.lastOrNull()
        }
    }

    private fun byName(name: String): Int {
        ProfilesLoader.profiles.forEachIndexed { index, profileData ->
            if (profileData.name == name) {
                return index
            }
        }
        return  -1
    }


    private fun nextOrFirst(): Int {
        val next  = activeProfileId + 1

        activeProfileId = if (next < ProfilesLoader.profiles.size) {
            next
        } else {
            0
        }
        return  activeProfileId

    }

    private fun prevOrLast(): Int {
        val next  = activeProfileId - 1
        activeProfileId = if (next >= 0) {
            next
        } else {
            ProfilesLoader.profiles.size - 1
        }
        return activeProfileId
    }
}

private fun ProfileItemData.match(stack: ItemStack): Boolean {
    val stackCustomName = stack.itemType.customName

    val customName = !customName.isNotBlank() || customName == stackCustomName

    return itemId.test(stack.itemType) && customName && this.components.match(stack)
}

//val itemPredicateArgumentType by lazy {
//    val access = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup())
//    ItemPredicateArgumentType.itemPredicate(access)
//}

private fun List<ProfileComponentData>?.match(stack: ItemStack): Boolean {
    if (this.isNullOrEmpty()) return true

    //    val changes = stack.itemType.changes
    val tag = stack.itemType.tag


    return this.all { ct ->
        val type = ct.id.type ?: return@all false

        val condNBT = ct.componentNbt
        val component = tag.get(type)
        val itemNBT = type.toFullNbtOrNull(component) ?: CompoundTag()

        if (condNBT == null) {
            Log.trace("existence check")

            component != null
        } else if (component is ItemEnchantments && condNBT is ListTag) {
            val enchant = component.`(enchantments)`.mapNotNullTo(mutableSetOf()) { it.`(key)`?.`(identigier)` }

            val cond = condNBT.mapTo(mutableSetOf()) { it.`(asString)`.`(asIdentifier)` }

            Log.trace("enchant check: $cond $enchant")
            enchant.containsAll(cond)
        } else if (component is PotionContents && (condNBT is StringTag || condNBT is ListTag)) {
            val potion = component.potion.getOrNull()?.`(key)`?.`(identigier)` ?: return@all false

            val cond = if (condNBT is ListTag) {
                condNBT.map { it.`(asString)`.`(asIdentifier)` }
            } else {
                listOf(condNBT.`(asString)`.`(asIdentifier)`)
            }

            Log.trace("potion check: $cond $potion")
            cond.contains(potion)
        } else {

            Log.trace("partial check: $condNBT < $itemNBT") // should use item predicates
            condNBT.compareTo(itemNBT) == 0
        }
    }
}

private fun Iterable<ProfileItemData>.findIn(from: List<Int>, action: (ProfileItemData, List<Int>) -> Int?): Int? {
    for (element in this) {
        val stack = action(element, from)
        if (stack != null) return stack
    }
    return null
}

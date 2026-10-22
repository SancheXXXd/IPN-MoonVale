/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2022 Plamen K. Kosseff <p.kosseff@gmail.com>
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

package org.anti_ad.mc.ipnext.access

import net.minecraft.world.inventory.Slot
import org.anti_ad.mc.common.extensions.ifTrue
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.ipn.api.access.IContainerClicker
import org.anti_ad.mc.ipnext.config.ModSettings
import org.anti_ad.mc.ipnext.event.autorefill.AutoRefillHandler
import org.anti_ad.mc.ipnext.event.LockedSlotKeeper
import org.anti_ad.mc.ipnext.ingame.vCursorStack

import org.anti_ad.mc.ipnext.inventory.ContainerClicker
import org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions
import org.anti_ad.mc.ipnext.item.isEmpty


object IPNSpi {

    private val actions = mutableListOf<() -> Unit>()

    fun onTickInGame() {
        val localActions = mutableListOf<() -> Unit>()
        synchronized(actions) {
            localActions.addAll(actions)
            actions.clear()
        }
        localActions.isNotEmpty().ifTrue {
            localActions.forEach {
                it()
            }
            LockedSlotKeeper.init()
        }
        localActions.clear()
    }

    fun addTickAction(action: () -> Unit) {
        synchronized(actions) {
            actions.add(action)
        }
    }
}


class PContainerClicker: IContainerClicker {

    override fun leftClick(slotId: Int) {
        IPNSpi.addTickAction {
            ContainerClicker.leftClick(if (slotId < 9) slotId + 36 else slotId)
        }
    }

    override fun rightClick(slotId: Int) {
        IPNSpi.addTickAction {
            ContainerClicker.rightClick(if (slotId < 9) slotId + 36 else slotId)
        }
    }

    override fun shiftClick(slotId: Int) {
        IPNSpi.addTickAction {
            ContainerClicker.shiftClick(if (slotId < 9) slotId + 36 else slotId)
        }
    }

    override fun qClick(slotId: Int) {
        IPNSpi.addTickAction {
            ContainerClicker.qClick(if (slotId < 9) slotId + 36 else slotId)
        }
    }

    override fun click(slotId: Int,
                       button: Int) {
        IPNSpi.addTickAction {
            ContainerClicker.click(if (slotId < 9) slotId + 36 else slotId, button)
        }
    }

    override fun swap(slotId: Int,
                      hotbarSlotId: Int) {
        IPNSpi.addTickAction {
            swapSlots(hotbarSlotId, slotId)
        }
    }

    override fun executeQClicks(clicks: Map<Int, Int>) {
        IPNSpi.addTickAction {
            ContainerClicker.executeQClicks(translateMapValueToSlot(clicks), interval)
        }
    }

    override fun executeSwapClicks(clicks: Map<Int, Int>) {
        IPNSpi.addTickAction {
            ContainerClicker.executeSwapClicks(translateMap(clicks), interval)
        }
    }

    override fun executeClicks(clicks: Map<Int, Int>) {
        IPNSpi.addTickAction {
            ContainerClicker.executeClicks(clicks.toList(), interval)
        }
    }

}

private val interval: Int
get() {
    return if (ModSettings.ADD_INTERVAL_BETWEEN_CLICKS.booleanValue)
        ModSettings.INTERVAL_BETWEEN_CLICKS_MS.integerValue
    else 0
}

private fun translateMap(source: Map<Int, Int>): Map<Int, Int> {
    val res = mutableMapOf<Int, Int>()
    source.forEach { (k, v) ->
        res[if (k < 9) k + 36 else k] = v
    }
    return res
}

private fun translateMapValueToSlot(source: Map<Int, Int>): Map<Int, Slot> {
    val res = mutableMapOf<Int, Slot>()
    source.forEach { (k, v) ->
        res[if (k < 9) k + 36 else k] = Vanilla.container().getSlot(v)
    }
    return res
}

private fun swapSlots(to: Int, foundSlotId: Int) {
    val swappedTo = if (to <= 8) to + 36 else to
    AutoRefillHandler.profilesSwappedItems.add(swappedTo)
    GeneralInventoryActions.cleanCursor()
    if (to in 0..8) { // use swap
        //handles hotbar
        ContainerClicker.swap(foundSlotId, to)
    } else {
        //handles offhand and armor slots

        ContainerClicker.leftClick(foundSlotId)
        ContainerClicker.leftClick(to)
        if (!vCursorStack().isEmpty()) {
            ContainerClicker.leftClick(foundSlotId) // put back
        }
    }
}

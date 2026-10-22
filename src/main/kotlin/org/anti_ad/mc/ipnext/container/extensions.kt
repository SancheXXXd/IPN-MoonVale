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

package org.anti_ad.mc.ipnext.container

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.world.inventory.*
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.ipnext.inventory.*

val versionSpecificContainerTypes =
        setOf(InventoryMenu::class.java                              /**/ to playerOnly,
              CreativeModeInventoryScreen.ItemPickerMenu::class.java /**/ to setOf(ContainerType.PURE_BACKPACK,
                                                                                   ContainerType.CREATIVE),
              EnchantmentMenu::class.java                            /**/ to setOf(ContainerType.PURE_BACKPACK),
              AnvilMenu::class.java                                  /**/ to setOf(ContainerType.PURE_BACKPACK,
                                                                                   ContainerType.ANVIL),
              BeaconMenu::class.java                                 /**/ to nonStorage,
              BlastFurnaceMenu::class.java                           /**/ to simple,
              CartographyTableMenu::class.java                       /**/ to simple,
              ItemCombinerMenu::class.java                           /**/ to simple,
              FurnaceMenu::class.java                                /**/ to simple,
              GrindstoneMenu::class.java                             /**/ to simple,
              LecternMenu::class.java                                /**/ to nonStorage,
              LoomMenu::class.java                                   /**/ to nonStorage,
              StonecutterMenu::class.java                            /**/ to setOf(ContainerType.PURE_BACKPACK,
                                                                                   ContainerType.STONECUTTER),
              SmithingMenu::class.java                               /**/ to simple,
              SmokerMenu::class.java                                 /**/ to simple,
              MerchantMenu::class.java                               /**/ to setOf(ContainerType.TRADER),
              CraftingMenu::class.java                               /**/ to setOf(ContainerType.CRAFTING),
              CrafterMenu::class.java                                /**/ to simple,
              HopperMenu::class.java                                 /**/ to setOf(ContainerType.NO_SORTING_STORAGE,
                                                                                   ContainerType.PURE_BACKPACK), //setOf(ContainerType.NO_SORTING_STORAGE),
              BrewingStandMenu::class.java                           /**/ to nonStorage,
              AbstractFurnaceMenu::class.java                        /**/ to simple,
              ChestMenu::class.java                                  /**/ to setOf(ContainerType.SORTABLE_STORAGE,
                                                                                   ContainerType.RECTANGULAR,
                                                                                   ContainerType.WIDTH_9),
              ShulkerBoxMenu::class.java                             /**/ to setOf(ContainerType.SORTABLE_STORAGE,
                                                                                   ContainerType.RECTANGULAR,
                                                                                   ContainerType.WIDTH_9),
              HorseInventoryMenu::class.java                         /**/ to setOf(ContainerType.SORTABLE_STORAGE,
                                                                                   ContainerType.RECTANGULAR,
                                                                                   ContainerType.HEIGHT_3,
                                                                                   ContainerType.HORSE_STORAGE),
              DispenserMenu::class.java                              /**/ to setOf(ContainerType.SORTABLE_STORAGE,
                                                                                   ContainerType.RECTANGULAR,
                                                                                   ContainerType.HEIGHT_3)).toTypedArray()

var selectPostAction: () -> Unit = {}
var selectPreAction: () -> Unit = {}

fun StonecutterMenu.selectRecipe(id: Int) {
    clickMenuButton(Vanilla.player(), id)
    Vanilla.interactionManager().handleInventoryButtonClick(containerId, id)
}

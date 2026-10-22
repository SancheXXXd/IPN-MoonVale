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

package org.anti_ad.mc.ipnext.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipnext.event.ClientEventHandler;
import org.anti_ad.mc.ipnext.event.CuttersDispatcher;
import org.anti_ad.mc.ipnext.event.LockedSlotKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionManager {

    @Inject(at = @At("HEAD"), method = "handleContainerInput(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V", cancellable = true)
    public void clickSlotPre(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (slotNum == 1 && (CuttersDispatcher.INSTANCE.isAnyRefillTick() || CuttersDispatcher.INSTANCE.isAnyStillCrafting())) {
            ci.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "handleContainerInput(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V")
    public void clickSlot(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (slotNum == 0) {
            ClientEventHandler.INSTANCE.onCrafted();
        } else if (slotNum == 1 && CuttersDispatcher.INSTANCE.isAnyOldScreen()) {
            CuttersDispatcher.INSTANCE.onCrafted();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemAdd(Lnet/minecraft/world/item/ItemStack;I)V")
    public void clickCreativeStack(ItemStack clicked, int slot, CallbackInfo ci) {
        LockedSlotKeeper.INSTANCE.addIgnoredHotbarSlotId(slot);
    }

    @Inject(at = @At("HEAD"), method = "handlePickItemFromBlock")
    public void pickFromInventory(BlockPos pos, boolean includeData, CallbackInfo ci) {
        LockedSlotKeeper.INSTANCE.ignoredSelectedHotbarSlot(-1);
    }

    @Inject(at = @At("HEAD"), method = "handlePickItemFromEntity")
    public void pickFromInventory(Entity entity, boolean includeData, CallbackInfo ci) {
        LockedSlotKeeper.INSTANCE.ignoredSelectedHotbarSlot(-1);
    }
}

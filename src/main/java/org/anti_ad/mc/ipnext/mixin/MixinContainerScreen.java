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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.anti_ad.mc.common.gui.NativeContext;
import org.anti_ad.mc.common.math2d.Rectangle;
import org.anti_ad.mc.ipnext.Log;
import org.anti_ad.mc.ipnext.config.LockedSlotsSettings;
import org.anti_ad.mc.ipnext.event.LockSlotsHandler;
import org.anti_ad.mc.ipnext.gui.inject.ContainerScreenEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinAbstractContainerScreen
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    protected MixinContainerScreen(Component title) {
        super(title);
    }

    protected MixinContainerScreen(Minecraft minecraft, Font font, Component title) {
        super(minecraft, font, title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            shift = At.Shift.AFTER), method = "extractContents")
    public void onBackgroundRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        IMixinContainerScreen screen = (IMixinContainerScreen) this;
        graphics.pose().pushMatrix();
        var topLeft = new Rectangle(screen.getContainerX(), screen.getContainerY(), screen.getContainerHeight(), screen.getContainerWidth()).getTopLeft();
        graphics.pose().translate(-topLeft.getX(), -topLeft.getY());
        ContainerScreenEventHandler.INSTANCE.onBackgroundRender(new NativeContext(graphics), mouseX,
                                                                mouseY, a);
        graphics.pose().popMatrix();
    }



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            shift = At.Shift.AFTER), method = "extractContents")
    public void onForegroundRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        IMixinContainerScreen screen = (IMixinContainerScreen) this;
        var context = new NativeContext(graphics);
        context.setOverlay(true);
        graphics.pose().pushMatrix();
        var topLeft = new Rectangle(screen.getContainerX(), screen.getContainerY(), screen.getContainerHeight(), screen.getContainerWidth()).getTopLeft();
        graphics.pose().translate(-topLeft.getX(), -topLeft.getY());
        ContainerScreenEventHandler.INSTANCE.onForegroundRender(context, mouseX, mouseY, a);
        graphics.pose().popMatrix();
    }

    @Inject(at = @At(value = "HEAD"),
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
            cancellable = true)
    public void onMouseClick(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        Log.INSTANCE.trace("onMouseClick for " + slotId);
        LockSlotsHandler.INSTANCE.setLastMouseClickSlot(slot);
        if (slot  != null  && LockedSlotsSettings.INSTANCE.getLOCK_SLOTS_DISABLE_USER_INTERACTION().getValue()) {
            if (LockSlotsHandler.INSTANCE.isMappedSlotLocked(slot) ||
                (containerInput  == ContainerInput.SWAP && LockSlotsHandler.INSTANCE.isSlotLocked(buttonNum))) {
                Log.INSTANCE.trace("cancel for " + slotId);
                ci.cancel();
            }
        }
    }

    @Inject(at = @At(value = "TAIL"),
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V")
    public void afterOnMouseClick(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        LockSlotsHandler.INSTANCE.setLastMouseClickSlot(null);
    }
}

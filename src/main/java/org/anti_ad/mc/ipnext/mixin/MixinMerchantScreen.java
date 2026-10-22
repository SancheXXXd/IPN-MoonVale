/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2023 Plamen K. Kosseff <p.kosseff@gmail.com>
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

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.trading.MerchantOffer;
import org.anti_ad.mc.common.gui.NativeContext;
import org.anti_ad.mc.ipnext.event.villagers.VillagerTradeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public class MixinMerchantScreen {


    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "extractContents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;extractAndDecorateCostA(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;II)V", ordinal = 0))
    void render(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float a, CallbackInfo ci,
                @Local(ordinal = 2) int xo,
                @Local(ordinal = 3) int yo,
                @Local(ordinal = 4) int offerY,
                @Local(ordinal = 5) int sellItem1X,
                @Local(ordinal = 6) int currentOfferIndex,
                @Local MerchantOffer offer) {
        VillagerTradeManager.INSTANCE.drawingButton((MerchantScreen)((Object)this), new NativeContext(drawContext), mouseX, mouseY,
                                                    offer,
                                                    xo,
                                                    yo,
                                                    offerY,
                                                    sellItem1X,
                                                    currentOfferIndex);
    }


}

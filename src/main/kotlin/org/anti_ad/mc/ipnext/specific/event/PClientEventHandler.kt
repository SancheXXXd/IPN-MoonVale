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

package org.anti_ad.mc.ipnext.specific.event

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

import org.anti_ad.mc.common.moreinfo.SemVer

import org.anti_ad.mc.common.vanilla.alias.glue.I18n
import java.net.URI

interface PClientEventHandler {

    fun createHoverEventText(text: String): HoverEvent =   HoverEvent.ShowText(Component.literal(text))

    fun createChatMessage(new: SemVer): MutableComponent = Component.literal("")
        .append(Component.literal("Inventory Profiles Next (MoonVale):")
                    .apply {
                        style = Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.AQUA)
                    }
               )
        .append(Component.literal(I18n.translate("ipnlegit.update.version"))
                    .apply {
                        style = Style.EMPTY
                    })
        .append(Component.literal("'$new'")
                    .apply {
                        style = Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.DARK_GREEN)
                    })
        .append(Component.literal(I18n.translate("ipnlegit.update.available"))
                    .apply {
                        style = Style.EMPTY
                    })
        .append(I18n.translate("ipnlegit.update.get"))
        .append(Component.literal("\"Modrinth\"")
                    .apply {
                        style = Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.DARK_GREEN)
                            .withUnderlined(true)
                            .withClickEvent(ClickEvent.OpenUrl(URI("https://modrinth.com/mod/inventory-profiles-next")))
                            .withHoverEvent(createHoverEventText("https://modrinth.com/mod/inventory-profiles-next"))
                    })
        .append(Component.literal(I18n.translate("ipnlegit.update.or"))
                    .apply { style = Style.EMPTY })
        .append(Component.literal("\"CurseForge\"")
                    .apply {
                        style = Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.DARK_RED)
                            .withUnderlined(true)
                            .withClickEvent(ClickEvent.OpenUrl(URI("https://www.curseforge.com/minecraft/mc-mods/inventory-profiles-next")))
                            .withHoverEvent(createHoverEventText("https://www.curseforge.com/minecraft/mc-mods/inventory-profiles-next"))
                    })
}

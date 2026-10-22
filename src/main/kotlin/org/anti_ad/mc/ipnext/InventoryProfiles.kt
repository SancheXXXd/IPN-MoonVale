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

package org.anti_ad.mc.ipnext

import org.anti_ad.mc.ipn.api.access.IContainerClicker
import org.anti_ad.mc.ipn.api.access.IPN
import org.anti_ad.mc.ipnext.access.PContainerClicker
import org.anti_ad.mc.ipnext.config.ModSettings
import org.anti_ad.mc.ipnext.event.ClientInitHandler
import org.anti_ad.mc.ipnext.compat.integrations.Integrations
import org.anti_ad.mc.ipnext.config.ConfigScreenSettings
import org.anti_ad.mc.ipnext.event.LockSlotsHandler
import org.anti_ad.mc.ipnext.event.SlotHighlightHandler
import org.anti_ad.mc.ipnext.gui.inject.InsertWidgetHandler
import org.anti_ad.mc.ipnext.input.InputHandler
import org.anti_ad.mc.ipnext.specific.initInfoManager
import java.net.URI
import java.net.URL

val versionCheckUrl: URL = URI("https://ipn.anti-ad.org/ipn/ipnVersionCheckV3").toURL()

@Suppress("unused")
fun init() {

    val ipnimpl = object : IPN() {

        override val containerClicker: IContainerClicker = PContainerClicker()

        override val lockedSlots: List<Int>
            get() = LockSlotsHandler.lockedInvSlots.toList()

        init {
            _IPN = this
        }
    }


    specificInit()

    ClientInitHandler.register {

        try {
            SlotHighlightHandler.hashCode()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        Integrations.init()

        initInfoManager()

        // Keybind register

        InputHandler.onClientInit()
        InsertWidgetHandler.onClientInit()
        ConfigScreenSettings.initMainConfig()
        //CustomDataFileLoader.load()
        if (ModSettings.FIRST_RUN.booleanValue) {
            ModSettings.FIRST_RUN.value = false
            ConfigScreenSettings.saveLoadManager.save()
        }
        //var s: Sounds = Sounds.REFILL_STEP_NOTIFY
    }

}

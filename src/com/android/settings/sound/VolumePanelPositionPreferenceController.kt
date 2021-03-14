/*
 * Copyright (C) 2022 AOSP-Krypton Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.sound

import android.content.Context
import android.os.UserHandle.USER_CURRENT
import android.provider.Settings

import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.krypton.settings.Utils

class VolumePanelPositionPreferenceController(
    context: Context,
    preferenceKey: String,
) : TogglePreferenceController(context, preferenceKey) {

    private val defaultValue = Utils.getBoolSysUIResource(context, CONFIG_NAME)

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun isChecked() = Settings.System.getIntForUser(
        mContext.contentResolver,
        Settings.System.VOLUME_PANEL_ON_LEFT,
        if (defaultValue) 1 else 0,
        USER_CURRENT
    ) == 1

    override fun setChecked(isChecked: Boolean) = Settings.System.putIntForUser(
        mContext.contentResolver,
        Settings.System.VOLUME_PANEL_ON_LEFT,
        if (isChecked) 1 else 0,
        USER_CURRENT
    )

    override fun getSliceHighlightMenuRes() = R.string.menu_key_sound

    companion object {
        private const val CONFIG_NAME = "config_audioPanelOnLeftSide"
    }
}

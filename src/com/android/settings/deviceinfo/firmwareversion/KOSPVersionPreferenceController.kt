/*
 * Copyright (C) 2019 The Android Open Source Project
 *               2021 AOSP-Krypton Project
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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemProperties
import android.util.Log

import androidx.preference.Preference

import com.android.settings.core.BasePreferenceController
import com.android.settings.R

public class KOSPVersionPreferenceController(
    private val context: Context,
    preferenceKey: String?
): BasePreferenceController(context, preferenceKey) {
    private val GITHUB_URI = Uri.parse("https://github.com/AOSP-Krypton")

    override public fun getAvailabilityStatus() = AVAILABLE

    override public fun getSummary() = SystemProperties.get(KRYPTON_VERSION_PROP,
        context.getString(R.string.device_info_not_available))

    override public fun handlePreferenceTreeClick(preference: Preference): Boolean {
        val key: String? = preference.getKey()
        if (key == null || !key.contentEquals(getPreferenceKey())) {
            return false
        }

        val intent = Intent(Intent.ACTION_VIEW, GITHUB_URI)
        if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "queryIntentActivities returned empty")
            return false
        }

        context.startActivity(intent)
        return true
    }

    companion object {
        private const val TAG = "KOSPVersionPreferenceController"
        private const val KRYPTON_VERSION_PROP = "ro.krypton.build.version"
    }
}

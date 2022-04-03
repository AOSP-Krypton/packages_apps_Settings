/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2021-2022 AOSP-Krypton Project
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

import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class KOSPVersionPreferenceController(
    context: Context,
    preferenceKey: String?
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence = SystemProperties.get(KRYPTON_VERSION_PROP,
        mContext.getString(R.string.device_info_not_available))

    override fun handlePreferenceTreeClick(preference: Preference) : Boolean {
        if (preference.key != preferenceKey) return false
        val resolvedActivities = mContext.packageManager.queryIntentActivities(CONTENT_INTENT, 0)
        if (resolvedActivities.isEmpty()) {
            Log.w(TAG, "No activity found to launch content intent!")
        } else {
            mContext.startActivity(CONTENT_INTENT)
        }
        return true
    }

    companion object {
        private const val TAG = "KOSPVersionPreferenceController"
        private const val KRYPTON_VERSION_PROP = "ro.krypton.build.version"

        private val CONTENT_INTENT = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/AOSP-Krypton"),
        )
    }
}

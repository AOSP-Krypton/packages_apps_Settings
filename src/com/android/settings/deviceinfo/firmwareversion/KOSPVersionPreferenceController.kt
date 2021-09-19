/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2021-2023 AOSP-Krypton Project
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
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.net.Uri
import android.os.SystemProperties
import android.widget.Toast

import androidx.preference.Preference

import com.android.settings.R
import com.android.settings.core.BasePreferenceController

private const val TAG = "KOSPVersionPreferenceController"
private const val KOSP_VERSION_PROP = "ro.kosp.build.version"

private val CONTENT_INTENT = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("https://github.com/AOSP-Krypton"),
)

class KOSPVersionPreferenceController(
    context: Context,
    preferenceKey: String?
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence = SystemProperties.get(
        KOSP_VERSION_PROP,
        mContext.getString(R.string.device_info_not_available)
    )

    override fun handlePreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key != preferenceKey) return false
        val resolvedActivities = mContext.packageManager.queryIntentActivities(
            CONTENT_INTENT,
            ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
        )
        if (resolvedActivities.isEmpty()) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.no_application_found_to_open_link),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            mContext.startActivity(CONTENT_INTENT)
        }
        return true
    }
}

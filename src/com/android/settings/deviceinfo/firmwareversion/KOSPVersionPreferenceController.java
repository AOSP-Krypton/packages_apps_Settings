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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

public class KOSPVersionPreferenceController extends BasePreferenceController {
    private static final String TAG = "KOSPVersionPreferenceController";
    private static final String KOSP_VERSION = "ro.krypton.build.version";
    private static final Uri GITHUB_URI = Uri.parse("https://github.com/AOSP-Krypton");

    private final String mKOSPVersion;

    public KOSPVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mKOSPVersion = SystemProperties.get(KOSP_VERSION,
            context.getString(R.string.device_info_not_available));
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return mKOSPVersion;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) {
            return false;
        } else if (!key.equals(getPreferenceKey())) {
            return false;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, GITHUB_URI);
        if (mContext.getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "queryIntentActivities() returns empty");
            return false;
        }

        mContext.startActivity(intent);
        return true;
    }
}

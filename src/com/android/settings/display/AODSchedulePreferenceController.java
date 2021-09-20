/*
 * Copyright (C) 2017 The Android Open Source Project
 *               2021 AOSP-Krypton Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

public class AODSchedulePreferenceController extends BasePreferenceController {

    static final int MODE_DISABLED = 0;
    static final int MODE_NIGHT = 1;
    static final int MODE_TIME = 2;
    static final int MODE_MIXED_SUNSET = 3;
    static final int MODE_MIXED_SUNRISE = 4;

    private static final String PROP_AWARE_AVAILABLE = "ro.vendor.aware_available";

    private AmbientDisplayConfiguration mConfig;

    public AODSchedulePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    @AvailabilityStatus
    public int getAvailabilityStatus() {
        return isAODAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final int mode = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        switch (mode) {
            default:
            case MODE_DISABLED:
                return mContext.getString(R.string.disabled);
            case MODE_NIGHT:
                return mContext.getString(R.string.night_display_auto_mode_twilight);
            case MODE_TIME:
                return mContext.getString(R.string.night_display_auto_mode_custom);
            case MODE_MIXED_SUNSET:
                return mContext.getString(R.string.always_on_display_schedule_mixed_sunset);
            case MODE_MIXED_SUNRISE:
                return mContext.getString(R.string.always_on_display_schedule_mixed_sunrise);
        }
    }

    public AODSchedulePreferenceController setConfig(AmbientDisplayConfiguration config) {
        mConfig = config;
        return this;
    }

    private AmbientDisplayConfiguration getConfig() {
        if (mConfig == null) {
            mConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mConfig;
    }

    private boolean isAODAvailable() {
        return getConfig().alwaysOnAvailableForUser(UserHandle.myUserId()) &&
            !SystemProperties.getBoolean(PROP_AWARE_AVAILABLE, false);
    }
}

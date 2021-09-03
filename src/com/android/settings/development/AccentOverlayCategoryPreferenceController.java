/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.development;

import static android.provider.Settings.Secure.ACCENT_DARK;
import static android.provider.Settings.Secure.ACCENT_LIGHT;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

/**
 * Preference controller to allow users to choose an overlay from a list of accent overlays.
 */
public class AccentOverlayCategoryPreferenceController extends OverlayCategoryPreferenceController {
    private static final String TAG = "AccentOverlayCategoryPC";
    private static final boolean DEBUG = false;

    public AccentOverlayCategoryPreferenceController(Context context, String category) {
        super(context, category);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver contentResolver = mContext.getContentResolver();
        logD("resetting rgb accents");
        Settings.Secure.putString(contentResolver, ACCENT_LIGHT, "-1");
        Settings.Secure.putString(contentResolver, ACCENT_DARK, "-1");
        return super.onPreferenceChange(preference, newValue);
    }

    @Override
    public void updateState(Preference preference) {
        /**
         * First check if both light and dark rgb accent is reset / empty.
         * If either of them is not reset / empty, ListPreference value should not be set.
         */
        boolean isReset = false;
        final ContentResolver contentResolver = mContext.getContentResolver();
        String lightAccent = Settings.Secure.getString(contentResolver, ACCENT_LIGHT);
        logD("lightAccent = " + lightAccent);
        if (lightAccent == null || lightAccent.equals("-1")) {
            logD("lightAccent is empty or = -1");
            String darkAccent = Settings.Secure.getString(contentResolver, ACCENT_DARK);
            logD("darkAccent = " + darkAccent);
            if (darkAccent == null || darkAccent.equals("-1")) {
                logD("darkAccent is empty or = -1");
                isReset = true;
            }
        }
        super.updateState(preference);
        if (!isReset) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setValue(null);
        }
    }

    private static void logD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}

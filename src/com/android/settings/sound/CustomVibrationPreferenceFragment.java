/*
 * Copyright (C) 2020 Yet Another AOSP Project
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

package com.android.settings.sound;

import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.krypton.settings.KryptonDashboardFragment;
import com.krypton.settings.preference.CustomSeekBarPreference;

import java.util.List;

/**
 * Settings for custom ringtone vibration pattern
 */
public class CustomVibrationPreferenceFragment extends KryptonDashboardFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "CustomVibrationPreferenceFragment";

    private static final String KEY_CUSTOM_VIB1 = "custom_vibration_pattern1";
    private static final String KEY_CUSTOM_VIB2 = "custom_vibration_pattern2";
    private static final String KEY_CUSTOM_VIB3 = "custom_vibration_pattern3";

    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build();

    private static final int[] SEVEN_ELEMENTS_VIBRATION_AMPLITUDE = {
        0, // No delay before starting
        255, // Vibrate full amplitude
        0, // No amplitude while waiting
        255,
        0,
        255,
        0,
    };

    private Vibrator mVibrator;
    private CustomSeekBarPreference mCustomVib1;
    private CustomSeekBarPreference mCustomVib2;
    private CustomSeekBarPreference mCustomVib3;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mVibrator = getContext().getSystemService(Vibrator.class);

        mCustomVib1 = findPreference(KEY_CUSTOM_VIB1);
        mCustomVib2 = findPreference(KEY_CUSTOM_VIB2);
        mCustomVib3 = findPreference(KEY_CUSTOM_VIB3);
        updateCustomVibPreferences();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.custom_vibration_pattern;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCustomVib1) {
            updateCustomVib(0, newValue);
            return true;
        } else if (preference == mCustomVib2) {
            updateCustomVib(1, newValue);
            return true;
        } else if (preference == mCustomVib3) {
            updateCustomVib(2, newValue);
            return true;
        }
        return false;
    }

    private void updateCustomVibPreferences() {
        final String value = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN);
        if (value != null && !value.isEmpty()) {
            try {
                final String[] customPattern = value.split(",", 3);
                mCustomVib1.setValue(Integer.parseInt(customPattern[0]));
                mCustomVib2.setValue(Integer.parseInt(customPattern[1]));
                mCustomVib3.setValue(Integer.parseInt(customPattern[2]));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Corrupt custom vibration pattern setting value, fallback to default");
                fallbackToDefault();
            }
        } else { // set default
            fallbackToDefault();
        }
        mCustomVib1.setOnPreferenceChangeListener(this);
        mCustomVib2.setOnPreferenceChangeListener(this);
        mCustomVib3.setOnPreferenceChangeListener(this);
    }

    private void fallbackToDefault() {
        mCustomVib1.setValue(0);
        mCustomVib2.setValue(800);
        mCustomVib3.setValue(800);
        Settings.System.putString(getContext().getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN, "0,800,800");
    }

    private void updateCustomVib(int index, Object value) {
        final String[] customPattern = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN).split(",", 3);
        customPattern[index] = String.valueOf(value);
        Settings.System.putString(getContext().getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN, String.join(",",
                    customPattern[0], customPattern[1], customPattern[2]));
        previewPattern(customPattern);
    }

    private void previewPattern(String[] pattern) {
        if (mVibrator == null || !mVibrator.hasVibrator()) return;
        long[] customVibPattern = {
            0, // No delay before starting
            Long.parseLong(pattern[0]), // How long to vibrate
            400, // Delay
            Long.parseLong(pattern[1]), // How long to vibrate
            400, // Delay
            Long.parseLong(pattern[2]), // How long to vibrate
            400, // How long to wait before vibrating again
        };
        mVibrator.vibrate(VibrationEffect.createWaveform(customVibPattern,
                SEVEN_ELEMENTS_VIBRATION_AMPLITUDE, -1), VIBRATION_ATTRIBUTES);
    }
}

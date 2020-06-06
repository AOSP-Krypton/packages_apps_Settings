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

package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.krypton.settings.preference.CustomSeekBarPreference;

/**
 * This class allows choosing a vibration pattern while ringing
 */
public class VibrationPatternPreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_VIB_PATTERN = "vibration_pattern";
    private static final String KEY_CUSTOM_VIB_CATEGORY = "custom_vibration_pattern";
    private static final String KEY_CUSTOM_VIB1 = "custom_vibration_pattern1";
    private static final String KEY_CUSTOM_VIB2 = "custom_vibration_pattern2";
    private static final String KEY_CUSTOM_VIB3 = "custom_vibration_pattern3";

    private ListPreference mVibPattern;
    private PreferenceCategory mCustomVibCategory;
    private CustomSeekBarPreference mCustomVib1, mCustomVib2, mCustomVib3;

    private static class VibrationEffectProxy {
        public VibrationEffect createWaveform(long[] timings, int[] amplitudes, int repeat) {
            return VibrationEffect.createWaveform(timings, amplitudes, repeat);
        }
    }

    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build();

    private static final long[] SIMPLE_VIBRATION_PATTERN = {
        0, // No delay before starting
        800, // How long to vibrate
        800, // How long to wait before vibrating again
    };

    private static final long[] DZZZ_DA_VIBRATION_PATTERN = {
        0, // No delay before starting
        500, // How long to vibrate
        200, // Delay
        20, // How long to vibrate
        720, // How long to wait before vibrating again
    };

    private static final long[] MM_MM_MM_VIBRATION_PATTERN = {
        0, // No delay before starting
        300, // How long to vibrate
        400, // Delay
        300, // How long to vibrate
        400, // Delay
        300, // How long to vibrate
        1400, // How long to wait before vibrating again
    };

    private static final long[] DA_DA_DZZZ_VIBRATION_PATTERN = {
        0, // No delay before starting
        30, // How long to vibrate
        80, // Delay
        30, // How long to vibrate
        80, // Delay
        50,  // How long to vibrate
        180, // Delay
        600,  // How long to vibrate
        1050, // How long to wait before vibrating again
    };

    private static final long[] DA_DZZZ_DA_VIBRATION_PATTERN = {
        0, // No delay before starting
        80, // How long to vibrate
        200, // Delay
        600, // How long to vibrate
        150, // Delay
        20,  // How long to vibrate
        1050, // How long to wait before vibrating again
    };

    private static final int[] NINE_ELEMENTS_VIBRATION_AMPLITUDE = {
        0, // No delay before starting
        255, // Vibrate full amplitude
        0, // No amplitude while waiting
        255,
        0,
        255,
        0,
        255,
        0,
    };

    private static final int[] SEVEN_ELEMENTS_VIBRATION_AMPLITUDE = {
        0, // No delay before starting
        255, // Vibrate full amplitude
        0, // No amplitude while waiting
        255,
        0,
        255,
        0,
    };

    private static final int[] FIVE_ELEMENTS_VIBRATION_AMPLITUDE = {
        0, // No delay before starting
        255, // Vibrate full amplitude
        0, // No amplitude while waiting
        255,
        0,
    };

    private static final int[] SIMPLE_VIBRATION_AMPLITUDE = {
        0, // No delay before starting
        255, // Vibrate full amplitude
        0, // No amplitude while waiting
    };

    public VibrationPatternPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_VIB_PATTERN;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        mVibPattern = screen.findPreference(KEY_VIB_PATTERN);
        final int vibPattern = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.RINGTONE_VIBRATION_PATTERN, 0);
        mVibPattern.setValueIndex(vibPattern);
        mVibPattern.setOnPreferenceChangeListener(this);

        mCustomVibCategory = screen.findPreference(KEY_CUSTOM_VIB_CATEGORY);
        mCustomVib1 = screen.findPreference(KEY_CUSTOM_VIB1);
        mCustomVib2 = screen.findPreference(KEY_CUSTOM_VIB2);
        mCustomVib3 = screen.findPreference(KEY_CUSTOM_VIB3);
        mCustomVib1.setOnPreferenceChangeListener(this);
        mCustomVib2.setOnPreferenceChangeListener(this);
        mCustomVib3.setOnPreferenceChangeListener(this);
        updateCustomVibVisibility(vibPattern == 5);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVibPattern) {
            int vibPattern = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.RINGTONE_VIBRATION_PATTERN, vibPattern);
            updateCustomVibVisibility(vibPattern == 5);
            return true;
        } else if (preference == mCustomVib1) {
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

    private void updateCustomVibVisibility(boolean show) {
        mCustomVibCategory.setVisible(show);
        mCustomVib1.setVisible(show);
        mCustomVib2.setVisible(show);
        mCustomVib3.setVisible(show);
        if (show) updateCustomVibPreferences();
    }

    private void updateCustomVibPreferences() {
        final String value = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN);
        if (value != null) {
            final String[] customPattern = value.split(",", 3);
            mCustomVib1.setValue(Integer.parseInt(customPattern[0]));
            mCustomVib2.setValue(Integer.parseInt(customPattern[1]));
            mCustomVib3.setValue(Integer.parseInt(customPattern[2]));
        } else { // set default
            mCustomVib1.setValue(0);
            mCustomVib2.setValue(800);
            mCustomVib3.setValue(800);
            Settings.System.putString(mContext.getContentResolver(),
                    Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN, "0,800,800");
        }
    }

    private void updateCustomVib(int index, Object value) {
        final String[] customPattern = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN).split(",", 3);
        customPattern[index] = String.valueOf((Integer) value);
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.CUSTOM_RINGTONE_VIBRATION_PATTERN, String.join(
                ",", customPattern[0], customPattern[1], customPattern[2]));
    }
}

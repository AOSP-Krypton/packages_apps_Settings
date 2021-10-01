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
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * This class allows choosing a vibration pattern while ringing
 */
public class VibrationPatternPreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_VIB_PATTERN = "vibration_pattern";
    private static final String KEY_CUSTOM_VIB_PREFERENCE = "custom_vibration_pattern";

    private ListPreference mVibPattern;
    private Preference mCustomVibPreference;

    private static VibrationEffect createWaveform(long[] timings, int[] amplitudes) {
        return VibrationEffect.createWaveform(timings, amplitudes, -1);
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
        return Utils.isVoiceCapable(mContext);
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

        mCustomVibPreference = screen.findPreference(KEY_CUSTOM_VIB_PREFERENCE);
        mCustomVibPreference.setVisible(vibPattern == 5);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final boolean rampingRinger = Settings.Global.getInt(
            mContext.getContentResolver(),
            Settings.Global.APPLY_RAMPING_RINGER, 0) == 1;
        final boolean alwaysVibrate = Settings.System.getInt(
            mContext.getContentResolver(),
            Settings.System.VIBRATE_WHEN_RINGING, 0) == 1;
        preference.setEnabled(rampingRinger || alwaysVibrate);
        mCustomVibPreference.setEnabled(rampingRinger || alwaysVibrate);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVibPattern) {
            int vibPattern = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.RINGTONE_VIBRATION_PATTERN, vibPattern);
            mCustomVibPreference.setVisible(vibPattern == 5);
            return true;
        }
        return false;
    }

    private void previewPattern() {
        final Vibrator vibrator = mContext.getSystemService(Vibrator.class);
        VibrationEffect effect;
        final int vibPattern = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.RINGTONE_VIBRATION_PATTERN, 0);
        switch (vibPattern) {
            case 1:
                effect = createWaveform(DZZZ_DA_VIBRATION_PATTERN, FIVE_ELEMENTS_VIBRATION_AMPLITUDE);
                break;
            case 2:
                effect = createWaveform(MM_MM_MM_VIBRATION_PATTERN, SEVEN_ELEMENTS_VIBRATION_AMPLITUDE);
                break;
            case 3:
                effect = createWaveform(DA_DA_DZZZ_VIBRATION_PATTERN, NINE_ELEMENTS_VIBRATION_AMPLITUDE);
                break;
            case 4:
                effect = createWaveform(DA_DZZZ_DA_VIBRATION_PATTERN, SEVEN_ELEMENTS_VIBRATION_AMPLITUDE);
                break;
            default:
                effect = createWaveform(SIMPLE_VIBRATION_PATTERN, SIMPLE_VIBRATION_AMPLITUDE);
        }
        vibrator.vibrate(effect, VIBRATION_ATTRIBUTES);
    }
}

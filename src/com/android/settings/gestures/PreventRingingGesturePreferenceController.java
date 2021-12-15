/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.gestures;

import static android.provider.Settings.Secure.KOSP_VOLUME_HUSH_MUTE;
import static android.provider.Settings.Secure.KOSP_VOLUME_HUSH_NORMAL;
import static android.provider.Settings.Secure.KOSP_VOLUME_HUSH_OFF;
import static android.provider.Settings.Secure.KOSP_VOLUME_HUSH_VIBRATE;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.MainSwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;

public class PreventRingingGesturePreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener, LifecycleObserver,
        OnResume, OnPause, PreferenceControllerMixin {

    private static final String KEY_MASTER = "gesture_prevent_ringing_switch";
    private static final String KEY_VIBRATE = "prevent_ringing_option_vibrate";
    private static final String KEY_MUTE = "prevent_ringing_option_mute";
    private static final String KEY_NORMAL = "prevent_ringing_option_normal";

    private static final String KEY_CYCLE = "prevent_ringing_option_cycle";

    private final String PREF_KEY_VIDEO = "gesture_prevent_ringing_video";
    private final String KEY = "gesture_prevent_ringing_category";
    private final Context mContext;

    private PreferenceCategory mPreferenceCategory;
    private MainSwitchPreference mMasterSwitch;
    private SwitchPreference mNormalPref;
    private SwitchPreference mVibratePref;
    private SwitchPreference mMutePref;

    private SettingObserver mSettingObserver;

    public PreventRingingGesturePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreferenceCategory = screen.findPreference(getPreferenceKey());
        mMasterSwitch = screen.findPreference(KEY_MASTER);
        mNormalPref = makeSwitchPreference(KEY_NORMAL, R.string.prevent_ringing_option_normal);
        mVibratePref = makeSwitchPreference(KEY_VIBRATE, R.string.prevent_ringing_option_vibrate);
        mMutePref = makeSwitchPreference(KEY_MUTE, R.string.prevent_ringing_option_mute);

        if (mPreferenceCategory != null) {
            mSettingObserver = new SettingObserver(mPreferenceCategory);
        }
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_volumeHushGestureEnabled);
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean isAdd = (Boolean) newValue;
        final String preventRingingSetting = keyToSetting(preference.getKey());
        String settingsValue = Settings.Secure.getString(
                mContext.getContentResolver(), Settings.Secure.VOLUME_HUSH_GESTURE);
        if (settingsValue == null || settingsValue.isEmpty()) settingsValue = KOSP_VOLUME_HUSH_OFF;
        final ArrayList<String> currentValue = new ArrayList<>(Arrays.asList(settingsValue.split(",", 3)));

        if (isAdd) {
            if (currentValue.get(0).equals(KOSP_VOLUME_HUSH_OFF))
                currentValue.clear();
            if (!currentValue.contains(preventRingingSetting))
                currentValue.add(preventRingingSetting);
        } else {
            if (currentValue.size() == 1 ||
                    preventRingingSetting.equals(KOSP_VOLUME_HUSH_OFF)) {
                currentValue.clear();
                currentValue.add(KOSP_VOLUME_HUSH_OFF);
                if (mMasterSwitch != null) mMasterSwitch.setChecked(false);
            } else {
                currentValue.remove(preventRingingSetting);
            }
        }

        return Settings.Secure.putString(mContext.getContentResolver(),
                Settings.Secure.VOLUME_HUSH_GESTURE, TextUtils.join(",", currentValue));
    }

    @Override
    public void updateState(Preference preference) {
        final String preventRingingSetting = Settings.Secure.getString(
                mContext.getContentResolver(), Settings.Secure.VOLUME_HUSH_GESTURE);

        final boolean enabled = preventRingingSetting != null && !preventRingingSetting.isEmpty() &&
                !preventRingingSetting.equals(KOSP_VOLUME_HUSH_OFF);
        if (mVibratePref != null) mVibratePref.setEnabled(enabled);
        if (mMutePref != null) mMutePref.setEnabled(enabled);
        if (mNormalPref != null) mNormalPref.setEnabled(enabled);

        final boolean isVibrate = enabled && preventRingingSetting.contains(KOSP_VOLUME_HUSH_VIBRATE);
        final boolean isMute = enabled && preventRingingSetting.contains(KOSP_VOLUME_HUSH_MUTE);
        final boolean isNormal = enabled && preventRingingSetting.contains(KOSP_VOLUME_HUSH_NORMAL);
        if (mVibratePref != null && mVibratePref.isChecked() != isVibrate)
            mVibratePref.setChecked(isVibrate);
        if (mMutePref != null && mMutePref.isChecked() != isMute)
            mMutePref.setChecked(isMute);
        if (mNormalPref != null && mNormalPref.isChecked() != isNormal)
            mNormalPref.setChecked(isNormal);
    }

    @Override
    public void onResume() {
        if (mSettingObserver != null) {
            mSettingObserver.register();
            updateState(mPreferenceCategory);
        }
    }

    @Override
    public void onPause() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister();
        }
    }

    private String keyToSetting(String key) {
        switch (key) {
            case KEY_MUTE:
                return KOSP_VOLUME_HUSH_MUTE;
            case KEY_VIBRATE:
                return KOSP_VOLUME_HUSH_VIBRATE;
            case KEY_NORMAL:
                return KOSP_VOLUME_HUSH_NORMAL;
            default:
                return KOSP_VOLUME_HUSH_OFF;
        }
    }

    private SwitchPreference makeSwitchPreference(String key, int titleId) {
        SwitchPreference pref = new SwitchPreference(mPreferenceCategory.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnPreferenceChangeListener(this);
        mPreferenceCategory.addPreference(pref);
        return pref;
    }

    private class SettingObserver extends ContentObserver {
        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(new Handler());
            mPreference = preference;
        }

        void register() {
            mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.VOLUME_HUSH_GESTURE),
                false, this);
        }

        void unregister() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.getLastPathSegment().equals(Settings.Secure.VOLUME_HUSH_GESTURE)) {
                updateState(mPreference);
            }
        }
    }
}

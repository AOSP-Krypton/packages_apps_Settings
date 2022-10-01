/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;

import android.app.settings.SettingsEnums;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.kosp.settings.gestures.NavBarInversePreferenceController;

import java.util.List;

/**
 * A fragment that includes settings for 2- and 3-button navigation modes.
 */
@SearchIndexable(forTarget = SearchIndexable.MOBILE)
public class ButtonNavigationSettingsFragment extends DashboardFragment {

    private static final String TAG = "ButtonNavigationSettingsFragment";

    private static final String NAV_BAR_INVERSE_PREF_KEY = "nav_bar_inverse";

    public static final String BUTTON_NAVIGATION_SETTINGS =
            "com.android.settings.BUTTON_NAVIGATION_SETTINGS";


    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_BUTTON_NAV_DLG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.button_navigation_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected final List<AbstractPreferenceController> createPreferenceControllers(final Context context) {
       return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static final List<AbstractPreferenceController> buildPreferenceControllers(
        final Context context,
        final Lifecycle lifecycle,
        final Fragment host
    ) {
        return List.of(new NavBarInversePreferenceController(context, NAV_BAR_INVERSE_PREF_KEY, lifecycle, host));
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.button_navigation_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return SystemNavigationPreferenceController.isOverlayPackageAvailable(context,
                            NAV_BAR_MODE_2BUTTON_OVERLAY)
                            || SystemNavigationPreferenceController.isOverlayPackageAvailable(
                            context,
                            NAV_BAR_MODE_3BUTTON_OVERLAY);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(final Context context) {
                    return buildPreferenceControllers(context, null /* lifecycle */, null /* host */);
                }
            };
}

/*
 * Waiting For Moranis
 * Copyright (C) 2020-2025
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.adrienbricchi.waitingformoranis.ui.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import org.adrienbricchi.waitingformoranis.databinding.AddCalendarTutoBinding;

import java.util.Optional;

import static android.R.string.ok;
import static android.app.Activity.RESULT_OK;
import static org.adrienbricchi.waitingformoranis.R.string.settings_google_calendar_title;
import static org.adrienbricchi.waitingformoranis.R.style.AppTheme;


public class GoogleCalendarOnboardingDialogFragment extends DialogFragment {

    static final String TAG = GoogleCalendarOnboardingDialogFragment.class.getSimpleName();
    static final String SHARED_PREFERENCES_SEEN_KEY = "google_calendar_onboarding_seen";
    static final int REQUEST_CODE = 30115;


    // <editor-fold desc="LifeCycle">


    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create the AlertDialog object and return it
        AddCalendarTutoBinding binding = AddCalendarTutoBinding.inflate(LayoutInflater.from(getContext()), null, false);

        // Use the Builder class for convenient dialog construction
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(getActivity(), AppTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(themeWrapper);
        builder.setTitle(settings_google_calendar_title);
        builder.setView(binding.getRoot());
        builder.setPositiveButton(ok, (dialog, which) -> dismiss());

        return builder.create();
    }


    @Override
    public void dismiss() {

        Optional.ofNullable(getActivity())
                .map(Activity::getApplicationContext)
                .map(PreferenceManager::getDefaultSharedPreferences)
                .ifPresent(m -> m.edit()
                                 .putBoolean(SHARED_PREFERENCES_SEEN_KEY, true)
                                 .apply());

        Optional.ofNullable(getTargetFragment())
                .ifPresent(t -> t.onActivityResult(REQUEST_CODE, RESULT_OK, null));

        super.dismiss();
    }


    // </editor-fold desc="LifeCycle">

}

/*
 * Waiting For Moranis
 * Copyright (C) 2020-2024
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.adrienbricchi.waitingformoranis.ui.preferences;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.*;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.adrienbricchi.waitingformoranis.R.drawable.ic_google_calendar_24dp_w40dp;
import static org.adrienbricchi.waitingformoranis.R.string.*;
import static org.adrienbricchi.waitingformoranis.R.xml.preferences;


@Keep
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();


    // <editor-fold desc="LifeCycle">


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(preferences, rootKey);

        if (getActivity() == null) { return; }

        buildGoogleCalendarPref();

        Optional.ofNullable((EditTextPreference) findPreference(getString(key_tmdb_key)))
                .ifPresent(p -> {

                    p.setOnPreferenceClickListener(preference -> {
                        TmdbService.init(getActivity())
                                   .flatMap(TmdbService::getPrivateApiKey)
                                   .ifPresent(p::setText);
                        return false;
                    });

                    p.setOnPreferenceChangeListener((preference, newValue) -> {
                        TmdbService.init(getActivity())
                                   .ifPresent(t -> t.setPrivateApiKey(String.valueOf(newValue)));
                        return false;
                    });
                });

        Optional.ofNullable((Preference) findPreference(getString(key_google_calendar_no_permission)))
                .ifPresent(p -> p.setOnPreferenceClickListener(preference -> {
                    if (getActivity() == null) { return false; }

                    if (PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                                         .getBoolean(GoogleCalendarOnboardingDialogFragment.SHARED_PREFERENCES_SEEN_KEY, false)) {
                        CalendarService.init(getActivity())
                                       .ifPresent(CalendarService::askPermissions);
                    } else {
                        showGoogleCalendarOnboarding();
                    }
                    return false;
                }));
    }


    @Override
    public void onResume() {
        super.onResume();
        buildGoogleCalendarPref();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CalendarService.PERMISSION_REQUEST_CODE) {
            buildGoogleCalendarPref();
        } else if (requestCode == GoogleCalendarOnboardingDialogFragment.REQUEST_CODE) {
            CalendarService.init(getActivity())
                           .ifPresent(CalendarService::askPermissions);
        }
    }


    // </editor-fold desc="LifeCycle">


    private void buildGoogleCalendarPref() {
        if (getActivity() == null) { return; }

        boolean hasPermissions = CalendarService.init(getActivity())
                                                .map(CalendarService::hasPermissions)
                                                .orElse(false);
        if (!hasPermissions) {
            return;
        }

        // Remove previous ones

        Optional.ofNullable((PreferenceCategory) findPreference(getString(key_external_services)))
                .ifPresent(c -> {
                    Optional.ofNullable((Preference) findPreference(getString(key_google_calendar_no_permission)))
                            .ifPresent(c::removePreference);
                    Optional.ofNullable((Preference) findPreference(getString(key_google_calendar)))
                            .ifPresent(c::removePreference);
                });

        // Populate new one

        ListPreference listPref = new ListPreference(getActivity());
        populateGoogleCalendarList(listPref);
        listPref.setKey(getString(key_google_calendar));
        listPref.setIcon(ic_google_calendar_24dp_w40dp);
        listPref.setTitle(settings_google_calendar_title);
        listPref.setDialogTitle(settings_google_calendar_title);
        listPref.setOnPreferenceChangeListener((preference, newValue) -> {
            long newId = Long.parseLong(newValue.toString());
            CalendarService.init(getActivity())
                           .ifPresent(c -> c.setCalendarId(newId));
            populateGoogleCalendarList(listPref);
            return true;
        });

        Optional.ofNullable((PreferenceCategory) findPreference(getString(key_external_services)))
                .ifPresent(c -> c.addPreference(listPref));
    }


    private void populateGoogleCalendarList(@NonNull ListPreference preference) {

        Map<Long, String> calendars = CalendarService.init(getActivity())
                                                     .map(CalendarService::getCalendarIds)
                                                     .orElse(null);
        if (calendars == null) { return; }

        calendars.put(-1L, getString(disabled));

        // Display the currently selected calendar

        Long calendar = CalendarService.init(getActivity())
                                       .map(CalendarService::getCurrentCalendarId)
                                       .orElse(-1L);

        preference.setSummary(calendars.get(calendar));

        // Build list

        preference.setEntryValues(calendars.keySet()
                                           .stream()
                                           .map(Object::toString)
                                           .collect(toList())
                                           .toArray(new String[]{}));

        preference.setEntries(calendars.values()
                                       .toArray(new String[]{}));
    }


    private void showGoogleCalendarOnboarding() {
        GoogleCalendarOnboardingDialogFragment fragment = new GoogleCalendarOnboardingDialogFragment();
        fragment.setTargetFragment(this, GoogleCalendarOnboardingDialogFragment.REQUEST_CODE);
        fragment.show(getParentFragmentManager(), GoogleCalendarOnboardingDialogFragment.TAG);
    }

}

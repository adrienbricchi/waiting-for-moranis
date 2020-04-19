/*
 * Waiting For Moranis
 * Copyright (C) 2020
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
package org.adrienbricchi.waitingformoranis.components.preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;

import java.util.Map;
import java.util.Optional;

import static android.content.Intent.ACTION_VIEW;
import static java.util.stream.Collectors.toList;


@SuppressWarnings("unused")
public class SettingsFragment extends PreferenceFragmentCompat {

    private final String GITHUB_URL = "https://github.com/adrienbricchi/waiting-for-moranis";


    // <editor-fold desc="LifeCycle">


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (getActivity() == null) { return; }

        buildGoogleCalendarPref();

        Optional.ofNullable((Preference) findPreference(getString(R.string.key_github)))
                .ifPresent(p -> p.setOnPreferenceClickListener(preference -> {
                    Intent i = new Intent(ACTION_VIEW);
                    i.setData(Uri.parse(GITHUB_URL));
                    startActivity(i);
                    return true;
                }));

        Optional.ofNullable((Preference) findPreference(getString(R.string.key_google_calendar_no_permission)))
                .ifPresent(p -> p.setOnPreferenceClickListener(preference -> {
                    CalendarService.askPermissions(getActivity());
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
        }
    }


    // </editor-fold desc="LifeCycle">


    private void buildGoogleCalendarPref() {
        if (getActivity() == null) { return; }

        boolean hasPermissions = CalendarService.hasPermissions(getActivity());
        if (!hasPermissions) {
            return;
        }

        // Remove previous ones

        Optional.ofNullable((PreferenceCategory) findPreference(getString(R.string.key_external_services)))
                .ifPresent(c -> {
                    Optional.ofNullable((Preference) findPreference(getString(R.string.key_google_calendar_no_permission)))
                            .ifPresent(c::removePreference);
                    Optional.ofNullable((Preference) findPreference(getString(R.string.key_google_calendar)))
                            .ifPresent(c::removePreference);
                });

        // Populate new one

        ListPreference listPref = new ListPreference(getActivity());
        populateGoogleCalendarList(listPref);
        listPref.setKey(getString(R.string.key_google_calendar));
        listPref.setOnPreferenceChangeListener((preference, newValue) -> {
            CalendarService.setCalendarId(getActivity(), Long.parseLong(newValue.toString()));
            populateGoogleCalendarList(listPref);
            return true;
        });

        listPref.setIcon(R.drawable.ic_google_calendar_24dp_w40dp);
        listPref.setTitle("Google Calendar");

        Optional.ofNullable((PreferenceCategory) findPreference(getString(R.string.key_external_services)))
                .ifPresent(c -> c.addPreference(listPref));
    }


    private void populateGoogleCalendarList(@NonNull ListPreference preference) {

        Map<Long, String> calendars = CalendarService.getCalendarIds(getActivity());
        if (calendars == null) { return; }

        calendars.put(-1L, "Disabled");

        // Display the currently selected calendar

        Long calendar = Optional.ofNullable(CalendarService.getCalendarId(getActivity()))
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

}

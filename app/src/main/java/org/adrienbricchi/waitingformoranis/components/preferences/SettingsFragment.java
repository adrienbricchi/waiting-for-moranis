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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
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


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Optional.ofNullable((ListPreference) findPreference("google_calendar"))
                .ifPresent(p -> {

                    Map<Long, String> calendars = CalendarService.getCalendarIds(getActivity());
                    if (calendars == null) {
                        return;
                    }

                    calendars.put(-1L, "Disabled");

                    p.setEntryValues(calendars.keySet()
                                              .stream()
                                              .map(Object::toString)
                                              .collect(toList())
                                              .toArray(new String[]{}));

                    p.setEntries(calendars.values()
                                          .toArray(new String[]{}));

                    p.setOnPreferenceChangeListener((preference, newValue) -> {
                        CalendarService.setCalendarId(getActivity(), Long.parseLong(newValue.toString()));
                        return true;
                    });
                });

        Optional.ofNullable((Preference) findPreference("github"))
                .ifPresent(p -> p.setOnPreferenceClickListener(preference -> {
                    Intent i = new Intent(ACTION_VIEW);
                    i.setData(Uri.parse(GITHUB_URL));
                    startActivity(i);
                    return true;
                }));
    }

}

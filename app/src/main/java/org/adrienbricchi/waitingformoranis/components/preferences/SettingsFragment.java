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
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import org.adrienbricchi.waitingformoranis.R;

import java.util.Optional;

import static android.content.Intent.ACTION_VIEW;


public class SettingsFragment extends PreferenceFragmentCompat {


    private final String GITHUB_URL = "https://github.com/adrienbricchi/waiting-for-moranis";


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Optional.ofNullable((Preference) findPreference("github"))
                .ifPresent(p -> p.setOnPreferenceClickListener(preference -> {
                    Intent i = new Intent(ACTION_VIEW);
                    i.setData(Uri.parse(GITHUB_URL));
                    startActivity(i);
                    return true;
                }));
    }


}

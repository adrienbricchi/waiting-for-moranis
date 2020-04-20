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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.adrienbricchi.waitingformoranis.databinding.SettingsActivityMainBinding;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;

import java.util.Arrays;
import java.util.Optional;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;


public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsActivityMainBinding binding = SettingsActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.settingsToolbar);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if ((requestCode == CalendarService.PERMISSION_REQUEST_CODE)
                && (grantResults.length == CalendarService.PERMISSIONS.size())
                && Arrays.stream(grantResults).allMatch(i -> i == PackageManager.PERMISSION_GRANTED)) {

            CalendarService.askAccount(this);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(LOG_TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);

        if ((resultCode == RESULT_OK) && (requestCode == CalendarService.ACCOUNT_SELECTION_REQUEST_CODE)) {
            CalendarService.setAccount(this, Optional.ofNullable(data)
                                                     .map(Intent::getExtras)
                                                     .map(i -> i.getString(KEY_ACCOUNT_NAME))
                                                     .orElse(null));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}

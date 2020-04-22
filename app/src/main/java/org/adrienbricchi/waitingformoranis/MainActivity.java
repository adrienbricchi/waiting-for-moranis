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
package org.adrienbricchi.waitingformoranis;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import org.adrienbricchi.waitingformoranis.components.AddMovieDialogFragment;
import org.adrienbricchi.waitingformoranis.components.MovieListFragment;
import org.adrienbricchi.waitingformoranis.components.preferences.SettingsActivity;
import org.adrienbricchi.waitingformoranis.databinding.ActivityMainBinding;

import static org.adrienbricchi.waitingformoranis.R.id.action_settings;
import static org.adrienbricchi.waitingformoranis.R.menu.menu_main;


public class MainActivity extends AppCompatActivity {

    public static final String APP_SHARED_PREFERENCES = "Waiting For Moranis";

    private ActivityMainBinding binding;


    // <editor-fold desc="LifeCycle">


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> onAddMovieFloatingButtonClicked());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // </editor-fold desc="LifeCycle">


    private void onAddMovieFloatingButtonClicked() {

        FragmentContainerView fragmentContainerView = binding.contentMain.navHostFragmentContainerView;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(fragmentContainerView.getId());

        if (navHostFragment == null) { return; }

        FragmentManager navFragmentManager = navHostFragment.getChildFragmentManager();
        MovieListFragment targetFragment = (MovieListFragment) navFragmentManager.getFragments().get(0);

        AddMovieDialogFragment fragment = new AddMovieDialogFragment();
        fragment.setKnownMovies(targetFragment.getAdapter().getDataSet());
        fragment.setTargetFragment(targetFragment, AddMovieDialogFragment.REQUEST_CODE);
        fragment.show(navFragmentManager, AddMovieDialogFragment.TAG);
    }

}

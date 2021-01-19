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
package org.adrienbricchi.waitingformoranis.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;
import org.adrienbricchi.waitingformoranis.databinding.ActivityMainBinding;
import org.adrienbricchi.waitingformoranis.ui.preferences.SettingsActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.viewpager2.widget.ViewPager2.*;
import static org.adrienbricchi.waitingformoranis.R.id.action_settings;
import static org.adrienbricchi.waitingformoranis.R.menu.menu_main;


public class MainActivity extends AppCompatActivity {


    public static final String FRAGMENT_REQUEST = "main_activity";
    public static final String FRAGMENT_ADD_FAB_BUTTON_CLICKED = "add_fab_button_clicked";


    // <editor-fold desc="LifeCycle">


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override public void onPageScrollStateChanged(int state) {
                switch (state) {

                    case SCROLL_STATE_DRAGGING:
                        binding.addMovieFab.hide();
                        break;

                    case SCROLL_STATE_SETTLING:
                        break;

                    case SCROLL_STATE_IDLE:
                    default:
                        binding.addMovieFab.show();
                }
            }

        });

        binding.viewPager.setAdapter(sectionsPagerAdapter);

        new TabLayoutMediator(
                binding.tabs,
                binding.viewPager,
                true,
                sectionsPagerAdapter::getPageTitle
        ).attach();

        binding.addMovieFab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(FRAGMENT_ADD_FAB_BUTTON_CLICKED, true);
            getSupportFragmentManager().setFragmentResult(FRAGMENT_REQUEST, bundle);
        });

        getSupportFragmentManager().setFragmentResultListener(
                MovieListFragment.FRAGMENT_REQUEST,
                this,
                (requestKey, bundle) -> {
                    int moviesCount = bundle.getInt(MovieListFragment.FRAGMENT_RESULT_MOVIES_COUNT);
                    binding.onboardingView.setVisibility((moviesCount == 0) ? VISIBLE : GONE);
                }
        );
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

}

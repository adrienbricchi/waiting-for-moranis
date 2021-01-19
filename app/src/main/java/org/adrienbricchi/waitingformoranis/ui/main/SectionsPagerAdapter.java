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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayout;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.ui.main.movieList.MovieListFragment;
import org.adrienbricchi.waitingformoranis.ui.main.showList.ShowWithSeasonsListFragment;


/**
 * A {@link FragmentStateAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {


    public SectionsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }


    @Override
    public @NonNull Fragment createFragment(int position) {
        if (position == 0) {
            return MovieListFragment.newInstance();
        } else {
            return ShowWithSeasonsListFragment.newInstance();
        }
    }


    @Override
    public int getItemCount() {
        return 2;
    }


    public void getPageTitle(@NonNull TabLayout.Tab tab, int position) {
        if (position == 0) {
            tab.setText(R.string.movies);
        } else {
            tab.setText(R.string.shows);
        }
    }


}
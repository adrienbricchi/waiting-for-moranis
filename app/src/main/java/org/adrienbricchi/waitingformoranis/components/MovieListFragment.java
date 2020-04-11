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
package org.adrienbricchi.waitingformoranis.components;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;

import java.util.ArrayList;


public class MovieListFragment extends Fragment {

    private MovieListAdapter movieListAdapter;


    // <editor-fold desc="LifeCycle">


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movie_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieListAdapter = new MovieListAdapter(new ArrayList<>());

        RecyclerView movieListView = view.findViewById(R.id.movie_list_recycler_view);
        movieListView.setHasFixedSize(true);
        movieListView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListView.setAdapter(movieListAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        new Thread(() -> {
            AppDatabase database = AppDatabase.getDatabase(getContext());
            movieListAdapter.getDataSet().clear();
            movieListAdapter.getDataSet().addAll(database.movieDao().getAll());
            new Handler(Looper.getMainLooper()).post(() -> movieListAdapter.notifyDataSetChanged());
        }).start();
    }


    // </editor-fold desc="LifeCycle">

}

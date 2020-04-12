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
import org.adrienbricchi.waitingformoranis.databinding.MovieListBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;

import java.util.ArrayList;
import java.util.List;

import static org.adrienbricchi.waitingformoranis.service.google.CalendarService.CALENDAR_PERMISSION_REQUEST_CODE;


public class MovieListFragment extends Fragment {

    private MovieListAdapter movieListAdapter;
    private MovieListBinding binding;


    // <editor-fold desc="LifeCycle">


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MovieListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieListAdapter = new MovieListAdapter(
                new ArrayList<>(),
                this::deleteMovieFromDb
        );

        binding.movieListRecyclerView.setHasFixedSize(true);
        binding.movieListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.movieListRecyclerView.setAdapter(movieListAdapter);

        binding.movieListSwipeRefreshLayout.setOnRefreshListener(this::onPullToRefresh);
    }


    @Override
    public void onStart() {
        super.onStart();
        refreshListFromDb();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            CalendarService.getCalendarId(getActivity());
        }
    }


    // </editor-fold desc="LifeCycle">


    private void onPullToRefresh() {

        new Thread(() -> {

            Long calendarId = CalendarService.getCalendarId(getActivity());
            if (calendarId != null) {
                CalendarService.addMoviesToCalendar(getActivity(), calendarId, movieListAdapter.getDataSet());

                AppDatabase database = AppDatabase.getDatabase(getContext());
                movieListAdapter.getDataSet()
                                .forEach(m -> database.movieDao().update(m));
            }

            new Handler(Looper.getMainLooper()).post(this::refreshListFromDb);
        }).start();
    }


    private void refreshListFromDb() {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            List<Movie> movies = database.movieDao().getAll();

            movieListAdapter.getDataSet().clear();
            movieListAdapter.getDataSet().addAll(movies);

            new Handler(Looper.getMainLooper()).post(() -> {
                movieListAdapter.notifyDataSetChanged();
                binding.movieListSwipeRefreshLayout.setRefreshing(false);
            });

        }).start();
    }


    private void deleteMovieFromDb(@NonNull Movie movie) {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            database.movieDao().remove(movie.getId());

            new Handler(Looper.getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }

}

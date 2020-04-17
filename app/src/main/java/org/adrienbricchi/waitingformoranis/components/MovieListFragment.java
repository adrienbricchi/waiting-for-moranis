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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.adrienbricchi.waitingformoranis.databinding.MovieListBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;
import org.adrienbricchi.waitingformoranis.utils.MovieUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.adrienbricchi.waitingformoranis.service.google.CalendarService.CALENDAR_PERMISSION_REQUEST_CODE;
import static org.adrienbricchi.waitingformoranis.utils.MovieUtils.checkForCalendarUpgradeNeed;


public class MovieListFragment extends Fragment {


    private MovieListAdapter adapter;
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

        adapter = new MovieListAdapter(
                new ArrayList<>(),
                this::deleteMovie
        );

        binding.movieListRecyclerView.setHasFixedSize(true);
        binding.movieListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.movieListRecyclerView.setAdapter(adapter);

        binding.movieListSwipeRefreshLayout.setOnRefreshListener(this::onPullToRefresh);
    }


    @Override
    public void onStart() {
        super.onStart();
        refreshListFromDb();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddMovieDialogFragment.REQUEST_CODE) {
            refreshListFromDb();
        }
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

            AppDatabase database = AppDatabase.getDatabase(getContext());
            Map<String, Movie> oldMoviesMap = database.movieDao()
                                                      .getAll()
                                                      .stream()
                                                      .collect(toMap(Movie::getId, movie -> movie));

            Long calendarId = CalendarService.getCalendarId(getActivity());
            if (calendarId != null) {

                Map<String, Long> existingEvents = CalendarService.getEvents(getActivity(), calendarId);

                // Movies that are in the Calendar, but not in the DB
                existingEvents.entrySet()
                              .stream()
                              .filter(e -> oldMoviesMap.get(e.getKey()) == null)
                              .forEach(e -> {
                                  Movie m = new Movie(e.getKey(), null, null, null, e.getValue(), true);
                                  oldMoviesMap.put(e.getKey(), m);
                                  database.movieDao().add(m);
                              });

                // Movies that are in the Calendar and the DB, but not mapped together
                oldMoviesMap.entrySet()
                            .stream()
                            .filter(m -> m.getValue().getCalendarEventId() == null)
                            .filter(m -> existingEvents.get(m.getKey()) != null)
                            .forEach(m -> m.getValue().setCalendarEventId(existingEvents.get(m.getKey())));

            }

            List<Movie> refreshedMovies = oldMoviesMap.values()
                                                      .stream()
                                                      .map(m -> TmdbService.getMovie(getActivity(), m.getId()))
                                                      .collect(toList());

            refreshedMovies.forEach(m -> {
                m.setUpdateNeededInCalendar(checkForCalendarUpgradeNeed(oldMoviesMap.get(m.getId()), m));
                m.setCalendarEventId(Optional.ofNullable(oldMoviesMap.get(m.getId()))
                                             .map(Movie::getCalendarEventId)
                                             .orElse(null));
            });

            if (calendarId != null) {

                refreshedMovies.stream()
                               .filter(m -> (m.getCalendarEventId() == null))
                               .filter(m -> (m.getReleaseDate() != null))
                               .forEach(m -> {
                                   Long calendarEventId = CalendarService.addMovieToCalendar(getActivity(), calendarId, m);
                                   m.setCalendarEventId(calendarEventId);
                               });

                refreshedMovies.stream()
                               .filter(m -> (m.getCalendarEventId() != null))
                               .filter(Movie::isUpdateNeededInCalendar)
                               .forEach(m -> {
                                   boolean edited = CalendarService.editMovieInCalendar(getActivity(), calendarId, m);
                                   m.setUpdateNeededInCalendar(!edited);
                               });

            }

            refreshedMovies.forEach(m -> database.movieDao().update(m));

            new Handler(Looper.getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }


    private void refreshListFromDb() {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            List<Movie> movies = database.movieDao().getAll();
            movies.sort(MovieUtils::compareReleaseDate);

            adapter.getDataSet().clear();
            adapter.getDataSet().addAll(movies);

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.notifyDataSetChanged();
                binding.movieListSwipeRefreshLayout.setRefreshing(false);
            });

        }).start();
    }


    private void deleteMovie(@NonNull Movie movie) {
        new Thread(() -> {

            Long calendarId = CalendarService.getCalendarId(getActivity());
            //noinspection unused
            boolean done = CalendarService.deleteMovieInCalendar(getActivity(), calendarId, movie);

            AppDatabase database = AppDatabase.getDatabase(getContext());
            database.movieDao().remove(movie.getId());

            new Handler(Looper.getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }

}

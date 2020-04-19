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
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.databinding.MovieListBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;
import org.adrienbricchi.waitingformoranis.utils.MovieUtils;

import java.util.*;
import java.util.stream.StreamSupport;

import static androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED;
import static androidx.recyclerview.selection.StorageStrategy.createStringStorage;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.adrienbricchi.waitingformoranis.service.google.CalendarService.PERMISSION_REQUEST_CODE;
import static org.adrienbricchi.waitingformoranis.utils.MovieUtils.checkForCalendarUpgradeNeed;


public class MovieListFragment extends Fragment {

    private static final String LOG_TAG = "MovieListFragment";
    private static final String SELECTION_ID_MOVIES_ID = "selection_id_movies_id";

    private MovieListAdapter adapter;
    private MovieListBinding binding;
    private ActionMode actionMode;


    // <editor-fold desc="LifeCycle">


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MovieListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MovieListAdapter(new ArrayList<>());

        binding.movieListRecyclerView.setHasFixedSize(true);
        binding.movieListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.movieListRecyclerView.setAdapter(adapter);

        // The RecyclerList must have an Adapter set before the SelectionTracker creation
        adapter.setSelectionTracker(new SelectionTracker.Builder<>(
                SELECTION_ID_MOVIES_ID,
                binding.movieListRecyclerView,
                buildAdapterMovieItemKeyProvider(),
                buildAdapterMovieItemDetailsLookup(),
                createStringStorage()
        ).build());

        adapter.getSelectionTracker().addObserver(buildMovieSelectionObserver());

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
        if (requestCode == PERMISSION_REQUEST_CODE) {
            CalendarService.getCalendarId(getActivity());
        }
    }


    // </editor-fold desc="LifeCycle">


    // <editor-fold desc="Setup">


    private @NonNull ItemKeyProvider<String> buildAdapterMovieItemKeyProvider() {

        return new ItemKeyProvider<String>(SCOPE_MAPPED) {

            @Override
            public @Nullable String getKey(int position) {
                return adapter.getMovieId(position);
            }


            @Override
            public int getPosition(@NonNull String key) {
                return adapter.getPosition(key);
            }

        };
    }


    private @NonNull ItemDetailsLookup<String> buildAdapterMovieItemDetailsLookup() {

        return new ItemDetailsLookup<String>() {

            @Override
            public @Nullable ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
                return Optional.ofNullable(binding.movieListRecyclerView.findChildViewUnder(e.getX(), e.getY()))
                               .map(v -> new ItemDetailsLookup.ItemDetails<String>() {

                                   @Override
                                   public int getPosition() {
                                       return binding.movieListRecyclerView.getChildViewHolder(v).getAdapterPosition();
                                   }


                                   @Override
                                   public String getSelectionKey() {
                                       return adapter.getDataSet().get(getPosition()).getId();
                                   }

                               })
                               .orElse(null);
            }

        };
    }


    private @NonNull ActionMode.Callback buildActionModeCallback() {
        return new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.action_mode, menu);

                Optional.ofNullable(getActivity())
                        .ifPresent(a -> a.getWindow().setStatusBarColor(getActivity().getColor(R.color.black)));

                return true;
            }


            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }


            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {

                    deleteMovies(adapter.getSelectionTracker().getSelection().spliterator());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                }
                return false;
            }


            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.i(LOG_TAG, "On destroy action mode");
                adapter.getSelectionTracker().clearSelection();

                Optional.ofNullable(getActivity())
                        .ifPresent(a -> a.getWindow().setStatusBarColor(getActivity().getColor(R.color.statusBarColor)));

                actionMode = null;
            }

        };
    }


    private SelectionTracker.SelectionObserver<String> buildMovieSelectionObserver() {
        return new SelectionTracker.SelectionObserver<String>() {

            @Override
            public void onSelectionChanged() {

                if (getActivity() == null) { return; }

                int rowsSelected = adapter.getSelectionTracker().getSelection().size();
                if (rowsSelected == 0) {
                    Optional.ofNullable(actionMode)
                            .ifPresent(ActionMode::finish);
                } else if (actionMode != null) {
                    actionMode.setTitle("" + rowsSelected + " items selected");
                } else {
                    actionMode = getActivity().startActionMode(buildActionModeCallback());
                    Optional.ofNullable(actionMode)
                            .ifPresent(m -> m.setTitle("0 item selected"));
                }
            }

        };
    }


    // </editor-fold desc="Setup">


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


    private void deleteMovies(@NonNull Spliterator<String> movieIdsIterator) {
        new Thread(() -> {

            Long calendarId = CalendarService.getCalendarId(getActivity());

            StreamSupport.stream(movieIdsIterator, false)
                         .map(id -> adapter.getMovie(id))
                         .filter(Objects::nonNull)
                         .forEach(m -> {
                             // Wrong "may be null" warning on Android Studio 3.6.2
                             //noinspection ConstantConditions
                             CalendarService.deleteMovieInCalendar(getActivity(), calendarId, m);
                             AppDatabase database = AppDatabase.getDatabase(getContext());
                             database.movieDao().remove(m.getId());
                         });

            new Handler(Looper.getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }

}

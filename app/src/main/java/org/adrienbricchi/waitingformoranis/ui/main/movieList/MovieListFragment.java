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
package org.adrienbricchi.waitingformoranis.ui.main.movieList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.Getter;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.databinding.MovieListBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;
import org.adrienbricchi.waitingformoranis.ui.main.MainActivity;

import java.util.*;
import java.util.stream.StreamSupport;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Looper.getMainLooper;
import static androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED;
import static androidx.recyclerview.selection.StorageStrategy.createStringStorage;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.adrienbricchi.waitingformoranis.R.plurals.n_selected_items;
import static org.adrienbricchi.waitingformoranis.utils.ReleaseUtils.checkForCalendarUpgradeNeed;
import static org.adrienbricchi.waitingformoranis.utils.ReleaseUtils.generateMovieReleaseDateComparator;


@Getter
public class MovieListFragment extends Fragment {

    private static final String LOG_TAG = "MovieListFragment";
    private static final String SELECTION_ID_MOVIES_ID = "selection_id_movies_id";

    public static final String FRAGMENT_TAG = "MovieListFragment";
    public static final String FRAGMENT_REQUEST = "movie_list_fragment";
    public static final String FRAGMENT_RESULT_MOVIES_COUNT = "movies_count";


    private MovieListAdapter adapter;
    private MovieListBinding binding;
    private ActionMode actionMode;


    // <editor-fold desc="LifeCycle">


    public static @NonNull MovieListFragment newInstance() {
        return new MovieListFragment();
    }


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

        getParentFragmentManager().setFragmentResultListener(
                AddMovieDialogFragment.FRAGMENT_REQUEST,
                this,
                (requestKey, result) -> {
                    if (result.getBoolean(AddMovieDialogFragment.FRAGMENT_RESULT_VALID, false)) {
                        refreshListFromDb(false);
                        onPullToRefresh();
                    }
                }
        );
    }


    @Override
    public void onStart() {
        super.onStart();
        refreshListFromDb();
    }


    @Override
    public void onResume() {
        super.onResume();
        getParentFragmentManager().setFragmentResultListener(
                MainActivity.FRAGMENT_REQUEST,
                this,
                (requestKey, result) -> {
                    if (result.getBoolean(MainActivity.FRAGMENT_ADD_FAB_BUTTON_CLICKED, false)) {
                        AddMovieDialogFragment addMovieDialogFragment = new AddMovieDialogFragment();
                        addMovieDialogFragment.setKnownMovies(adapter.getDataSet());
                        addMovieDialogFragment.show(getParentFragmentManager(), AddMovieDialogFragment.TAG);
                    }
                }
        );
    }


    @Override
    public void onPause() {
        super.onPause();
        getParentFragmentManager().clearFragmentResultListener(MainActivity.FRAGMENT_REQUEST);
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

                if (item.getItemId() == R.id.edit) {

                    Movie selectedMovie = StreamSupport
                            .stream(adapter.getSelectionTracker().getSelection().spliterator(), false)
                            .findFirst()
                            .map(adapter::getMovie)
                            .orElse(null);

                    Intent intent = TmdbService
                            .init(getActivity())
                            .filter(t -> selectedMovie != null)
                            .map(t -> t.getEditReleaseDatesUrl(selectedMovie))
                            .map(u -> new Intent(ACTION_VIEW, u))
                            .orElse(null);

                    Optional.ofNullable(getActivity())
                            .map(Activity::getPackageManager)
                            .filter(pm -> intent != null)
                            .filter(pm -> intent.resolveActivity(pm) != null)
                            .ifPresent(pm -> startActivity(intent));

                    mode.finish();
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
                Log.i(LOG_TAG, "onSelectionChanged");

                if (getActivity() == null) { return; }

                int rowsSelected = adapter.getSelectionTracker().getSelection().size();
                if (rowsSelected == 0) {
                    Optional.ofNullable(actionMode)
                            .ifPresent(ActionMode::finish);
                } else {
                    actionMode = Optional.ofNullable(actionMode)
                                         .orElseGet(() -> getActivity().startActionMode(buildActionModeCallback()));

                    Optional.ofNullable(actionMode)
                            .ifPresent(a -> a.setTitle(getResources().getQuantityString(n_selected_items, rowsSelected, rowsSelected)));

                    Optional.ofNullable(actionMode)
                            .map(ActionMode::getMenu)
                            .map(m -> m.findItem(R.id.edit))
                            .ifPresent(i -> {
                                i.setEnabled(rowsSelected == 1);
                                i.setVisible(rowsSelected == 1);
                            });
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

            Long calendarId = CalendarService.init(getActivity())
                                             .map(CalendarService::getCurrentCalendarId)
                                             .orElse(null);

            if (calendarId != null) {

                Map<String, Long> existingEvents = CalendarService.init(getActivity())
                                                                  .map(c -> c.getEvents(calendarId, true))
                                                                  .orElseGet(Collections::emptyMap);

                // Movies that are in the Calendar, but not in the DB
                existingEvents.keySet()
                              .stream()
                              .filter(i -> !oldMoviesMap.containsKey(i))
                              .map(i -> TmdbService.init(getActivity())
                                                   .map(t -> t.getMovie(i))
                                                   .orElse(null))
                              .filter(Objects::nonNull)
                              .peek(m -> new Handler(getMainLooper()).post(() -> {
                                  // Adding movies to the list,
                                  // on-the-fly, without going through the DB
                                  adapter.getDataSet().add(adapter.getDataSet().size(), m);
                                  adapter.notifyDataSetChanged();
                                  // Notify the parent Activity
                                  Bundle result = new Bundle();
                                  result.putInt(FRAGMENT_RESULT_MOVIES_COUNT, adapter.getDataSet().size());
                                  getParentFragmentManager().setFragmentResult(FRAGMENT_REQUEST, result);
                              }))
                              .forEach(m -> {
                                  oldMoviesMap.put(m.getId(), m);
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
                                                      .map(m -> TmdbService.init(getActivity())
                                                                           .map(t -> t.getMovie(m.getId()))
                                                                           .orElse(null))
                                                      .filter(Objects::nonNull)
                                                      .collect(toList());

            refreshedMovies.stream()
                           .peek(m -> m.setUpdateNeededInCalendar(checkForCalendarUpgradeNeed(oldMoviesMap.get(m.getId()), m)))
                           .forEach(m -> m.setCalendarEventId(Optional.ofNullable(oldMoviesMap.get(m.getId()))
                                                                      .map(Movie::getCalendarEventId)
                                                                      .orElse(null)));

            if (calendarId != null) {

                refreshedMovies.stream()
                               .filter(m -> (m.getCalendarEventId() == null))
                               .forEach(m -> {
                                   Long calendarEventId = CalendarService.init(getActivity())
                                                                         .map(c -> c.addMovieToCalendar(calendarId, m))
                                                                         .orElse(null);
                                   m.setCalendarEventId(calendarEventId);
                               });

                refreshedMovies.stream()
                               .filter(m -> (m.getCalendarEventId() != null))
                               .filter(Movie::isUpdateNeededInCalendar)
                               .forEach(m -> {
                                   boolean edited = CalendarService.init(getActivity())
                                                                   .map(c -> c.editMovieInCalendar(calendarId, m))
                                                                   .orElse(false);
                                   m.setUpdateNeededInCalendar(!edited);
                               });

            }

            refreshedMovies.forEach(m -> database.movieDao().update(m));

            new Handler(getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }


    private void refreshListFromDb() {
        refreshListFromDb(true);
    }


    private void refreshListFromDb(boolean disableSpinnerOnCompletion) {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            List<Movie> movies = database.movieDao().getAll();
            movies.sort(generateMovieReleaseDateComparator(Locale.getDefault()));

            adapter.getDataSet().clear();
            adapter.getDataSet().addAll(movies);

            new Handler(getMainLooper()).post(() -> {

                Bundle result = new Bundle();
                result.putInt(FRAGMENT_RESULT_MOVIES_COUNT, adapter.getDataSet().size());
                getParentFragmentManager().setFragmentResult(FRAGMENT_REQUEST, result);

                adapter.notifyDataSetChanged();
                binding.movieListSwipeRefreshLayout.setRefreshing(!disableSpinnerOnCompletion);
            });

        }).start();
    }


    private void deleteMovies(@NonNull Spliterator<String> movieIdsIterator) {
        List<String> selectedIds = StreamSupport.stream(movieIdsIterator, false).collect(toList());

        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            CalendarService.init(getActivity())
                           .ifPresent(c -> selectedIds.stream()
                                                      .map(id -> adapter.getMovie(id))
                                                      .filter(Objects::nonNull)
                                                      .forEach(m -> {
                                                          c.deleteEventInCalendar(m.getCalendarEventId());
                                                          database.movieDao().remove(m.getId());
                                                      }));

            new Handler(getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }


}

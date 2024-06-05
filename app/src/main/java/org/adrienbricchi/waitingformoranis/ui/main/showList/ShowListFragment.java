/*
 * Waiting For Moranis
 * Copyright (C) 2020-2024
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

package org.adrienbricchi.waitingformoranis.ui.main.showList;

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
import org.adrienbricchi.waitingformoranis.databinding.ShowListBinding;
import org.adrienbricchi.waitingformoranis.models.Show;
import org.adrienbricchi.waitingformoranis.service.google.CalendarService;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;
import org.adrienbricchi.waitingformoranis.ui.main.MainActivity;
import org.adrienbricchi.waitingformoranis.ui.main.SearchEventListener;

import java.util.*;
import java.util.stream.StreamSupport;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Looper.getMainLooper;
import static androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED;
import static androidx.recyclerview.selection.StorageStrategy.createStringStorage;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.adrienbricchi.waitingformoranis.R.plurals.n_selected_items;
import static org.adrienbricchi.waitingformoranis.utils.ReleaseUtils.*;


@Getter
public class ShowListFragment extends Fragment {

    private static final String LOG_TAG = "ShowListFragment";
    private static final String SELECTION_ID_SHOWS_ID = "selection_id_shows_id";

    public static final String FRAGMENT_TAG = "ShowListFragment";
    public static final String FRAGMENT_REQUEST = "show_list_fragment";
    public static final String FRAGMENT_RESULT_SHOWS_COUNT = "shows_count";


    private ShowListAdapter adapter;
    private ShowListBinding binding;
    private ActionMode actionMode;


    // <editor-fold desc="LifeCycle">


    public static @NonNull ShowListFragment newInstance() {
        return new ShowListFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ShowListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ShowListAdapter(new ArrayList<>());

        binding.showListRecyclerView.setHasFixedSize(true);
        binding.showListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.showListRecyclerView.setAdapter(adapter);

        // The RecyclerList must have an Adapter set before the SelectionTracker creation
        adapter.setSelectionTracker(new SelectionTracker.Builder<>(
                SELECTION_ID_SHOWS_ID,
                binding.showListRecyclerView,
                buildAdapterShowItemKeyProvider(),
                buildAdapterShowItemDetailsLookup(),
                createStringStorage()
        ).build());

        adapter.getSelectionTracker().addObserver(buildShowSelectionObserver());

        binding.showListSwipeRefreshLayout.setOnRefreshListener(this::onPullToRefresh);

        getParentFragmentManager().setFragmentResultListener(

                AddShowDialogFragment.FRAGMENT_REQUEST,
                this,
                (requestKey, result) -> {
                    if (result.getBoolean(AddShowDialogFragment.FRAGMENT_RESULT_VALID, false)) {
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


    @Override public void onResume() {
        super.onResume();
        getParentFragmentManager().setFragmentResultListener(
                MainActivity.FRAGMENT_REQUEST,
                this,
                (requestKey, result) -> {
                    if (result.getBoolean(MainActivity.FRAGMENT_ADD_FAB_BUTTON_CLICKED, false)) {
                        AddShowDialogFragment addShowDialogFragment = new AddShowDialogFragment();
                        addShowDialogFragment.setKnownShows(adapter.getDataSet());
                        addShowDialogFragment.show(getParentFragmentManager(), AddShowDialogFragment.TAG);
                    }
                }
        );
    }


    @Override public void onPause() {
        super.onPause();
        Optional.ofNullable(actionMode).ifPresent(ActionMode::finish);
        getParentFragmentManager().clearFragmentResultListener(MainActivity.FRAGMENT_REQUEST);
    }


    // </editor-fold desc="LifeCycle">


    // <editor-fold desc="Setup">


    private @NonNull ItemKeyProvider<String> buildAdapterShowItemKeyProvider() {

        return new ItemKeyProvider<String>(SCOPE_MAPPED) {

            @Override
            public @Nullable String getKey(int position) {
                return adapter.getShowId(position);
            }


            @Override
            public int getPosition(@NonNull String key) {
                return adapter.getPosition(key);
            }

        };
    }


    private @NonNull ItemDetailsLookup<String> buildAdapterShowItemDetailsLookup() {

        return new ItemDetailsLookup<String>() {

            @Override
            public @Nullable ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
                return Optional.ofNullable(binding.showListRecyclerView.findChildViewUnder(e.getX(), e.getY()))
                               .map(v -> new ItemDetails<String>() {

                                   @Override
                                   public int getPosition() {
                                       return binding.showListRecyclerView.getChildViewHolder(v).getAdapterPosition();
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
                // Nothing to do
                return false;
            }


            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                if (item.getItemId() == R.id.delete) {

                    deleteShows(adapter.getSelectionTracker().getSelection().spliterator());
                    mode.finish(); // Action picked, so close the CAB

                    return true;

                }

                if (item.getItemId() == R.id.edit) {

                    Show selectedShow = StreamSupport
                            .stream(adapter.getSelectionTracker().getSelection().spliterator(), false)
                            .findFirst()
                            .map(adapter::getShow)
                            .orElse(null);

                    Intent intent = TmdbService
                            .init(getActivity())
                            .filter(t -> selectedShow != null)
                            .map(t -> t.getEditReleaseDatesUrl(selectedShow))
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


    private SelectionTracker.SelectionObserver<String> buildShowSelectionObserver() {
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


    public void onSearchEvent(@NonNull String searchTerm) {
        adapter.setCurrentSearch(searchTerm);
        adapter.notifyDataSetChanged();
    }


    private void onPullToRefresh() {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            Map<String, Show> oldShowsMap = database.showDao()
                                                    .getAll()
                                                    .stream()
                                                    .collect(toMap(Show::getId, s -> s));

            Long calendarId = CalendarService.init(getActivity())
                                             .map(CalendarService::getCurrentCalendarId)
                                             .orElse(null);

            if (calendarId != null) {

                Map<String, Long> existingEvents = CalendarService.init(getActivity())
                                                                  .map(c -> c.getEvents(calendarId, false))
                                                                  .orElseGet(Collections::emptyMap);

                // Shows that are in the Calendar, but not in the DB
                existingEvents.keySet()
                              .stream()
                              .filter(i -> !oldShowsMap.containsKey(i))
                              .map(i -> TmdbService.init(getActivity())
                                                   .map(t -> t.getShow(i))
                                                   .orElse(null))
                              .filter(Objects::nonNull)
                              .peek(m -> new Handler(getMainLooper()).post(() -> {
                                  // Adding shows to the list,
                                  // on-the-fly, without going through the DB
                                  adapter.getDataSet().add(adapter.getDataSet().size(), m);
                                  binding.showListRecyclerView.getRecycledViewPool().clear();
                                  adapter.notifyDataSetChanged();
                                  // Notify the parent Activity
                                  Bundle result = new Bundle();
                                  result.putInt(FRAGMENT_RESULT_SHOWS_COUNT, adapter.getDataSet().size());
                                  getParentFragmentManager().setFragmentResult(FRAGMENT_REQUEST, result);
                              }))
                              .forEach(s -> {
                                  oldShowsMap.put(s.getId(), s);
                                  database.showDao().add(s);
                              });

                // Shows that are in the Calendar and the DB, but not mapped together
                oldShowsMap.entrySet()
                           .stream()
                           .filter(s -> s.getValue().getCalendarEventId() == null)
                           .filter(s -> existingEvents.get(s.getKey()) != null)
                           .forEach(s -> s.getValue().setCalendarEventId(existingEvents.get(s.getKey())));

            }

            List<Show> refreshedShows = oldShowsMap.values()
                                                   .stream()
                                                   .map(s -> TmdbService.init(getActivity())
                                                                        .map(t -> t.getShow(s.getId()))
                                                                        .orElse(null))
                                                   .filter(Objects::nonNull)
                                                   .collect(toList());

            refreshedShows.stream()
                          .peek(s -> s.setUpdateNeededInCalendar(checkForCalendarUpgradeNeed(oldShowsMap.get(s.getId()), s)))
                          .forEach(s -> s.setCalendarEventId(Optional.ofNullable(oldShowsMap.get(s.getId()))
                                                                     .map(Show::getCalendarEventId)
                                                                     .orElse(null)));

            if (calendarId != null) {

                refreshedShows.stream()
                              .filter(s -> (s.getCalendarEventId() == null))
                              .forEach(s -> {
                                  Long calendarEventId = CalendarService.init(getActivity())
                                                                        .map(c -> c.addShowToCalendar(calendarId, s))
                                                                        .orElse(null);
                                  s.setCalendarEventId(calendarEventId);
                              });

                refreshedShows.stream()
                              .filter(s -> (s.getCalendarEventId() != null))
                              .filter(Show::isUpdateNeededInCalendar)
                              .forEach(s -> {
                                  boolean edited = CalendarService.init(getActivity())
                                                                  .map(c -> c.editShowInCalendar(calendarId, s))
                                                                  .orElse(false);
                                  s.setUpdateNeededInCalendar(!edited);
                              });

            }

            refreshedShows.forEach(s -> database.showDao().update(s));

            new Handler(getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }


    private void refreshListFromDb() {
        refreshListFromDb(true);
    }


    private void refreshListFromDb(boolean disableSpinnerOnCompletion) {
        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            List<Show> shows = database.showDao().getAll();
            shows.sort(SHOW_COMPARATOR);

            adapter.getDataSet().clear();
            adapter.getDataSet().addAll(shows);

            new Handler(getMainLooper()).post(() -> {

                Bundle result = new Bundle();
                result.putInt(FRAGMENT_RESULT_SHOWS_COUNT, adapter.getDataSet().size());
                getParentFragmentManager().setFragmentResult(FRAGMENT_REQUEST, result);

                binding.showListRecyclerView.getRecycledViewPool().clear();
                adapter.notifyDataSetChanged();
                binding.showListSwipeRefreshLayout.setRefreshing(!disableSpinnerOnCompletion);
            });

        }).start();
    }


    private void deleteShows(@NonNull Spliterator<String> showIdsIterator) {
        List<String> selectedIds = StreamSupport.stream(showIdsIterator, false).collect(toList());

        new Thread(() -> {

            AppDatabase database = AppDatabase.getDatabase(getContext());
            CalendarService.init(getActivity())
                           .ifPresent(c -> selectedIds.stream()
                                                      .map(id -> adapter.getShow(id))
                                                      .filter(Objects::nonNull)
                                                      .forEach(s -> {
                                                          c.deleteEventInCalendar(s.getCalendarEventId());
                                                          database.showDao().remove(s.getId());
                                                      }));

            new Handler(getMainLooper()).post(this::refreshListFromDb);

        }).start();
    }


}

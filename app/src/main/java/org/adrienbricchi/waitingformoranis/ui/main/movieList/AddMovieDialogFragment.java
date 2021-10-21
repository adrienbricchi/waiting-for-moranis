/*
 * Waiting For Moranis
 * Copyright (C) 2020-2021
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.Setter;
import org.adrienbricchi.waitingformoranis.databinding.AddMovieMainBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Looper.getMainLooper;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
import static java.util.stream.Collectors.toSet;


@Setter
public class AddMovieDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "AddMovieDialogFragment";

    public static final String TAG = "AddMovieDialogFragment";
    public static final String FRAGMENT_REQUEST = "add_movie_dialog_fragment";
    public static final String FRAGMENT_RESULT_VALID = "result_valid";

    private AddMovieDialogListAdapter adapter;
    private AddMovieMainBinding binding;
    private List<Movie> knownMovies;


    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create the AlertDialog object and return it
        binding = AddMovieMainBinding.inflate(LayoutInflater.from(getContext()), null, false);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());

        Set<String> knownIds = knownMovies.stream()
                                          .map(Movie::getId)
                                          .collect(toSet());

        adapter = new AddMovieDialogListAdapter(knownIds, new ArrayList<>());
        binding.addMovieListRecyclerView.setHasFixedSize(true);
        binding.addMovieListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.addMovieListRecyclerView.setAdapter(adapter);
        binding.addMovieListRecyclerView.setVisibility(GONE);

        binding.searchAppCompatEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean keyboardEnterPressed = (event != null) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            boolean searchButtonPressed = (actionId == IME_ACTION_SEARCH);
            if (searchButtonPressed || keyboardEnterPressed) {
                searchMovie(v.getText().toString());
                return true;
            }
            return false;
        });

        AlertDialog dialog = builder.create();
        Optional.ofNullable(dialog.getWindow())
                .ifPresent(w -> w.getAttributes().gravity = Gravity.TOP);

        return dialog;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.searchAppCompatEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Bundle bundle = new Bundle();
        bundle.putBoolean(FRAGMENT_RESULT_VALID, true);
        getParentFragmentManager().setFragmentResult(FRAGMENT_REQUEST, bundle);
    }


    private void searchMovie(@NonNull String searchTerm) {

        if (getActivity() == null) { return; }

        binding.searchAppCompatEditText.clearFocus();
        Optional.ofNullable((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE))
                .ifPresent(in -> in.hideSoftInputFromWindow(binding.searchAppCompatEditText.getWindowToken(), 0));

        new Thread(() -> TmdbService.init(getActivity())
                                    .ifPresent(t -> t.searchMovie(
                                            searchTerm,
                                            movies -> {
                                                adapter.getDataSet().clear();
                                                adapter.getDataSet().addAll(movies);
                                                binding.addMovieListRecyclerView.setVisibility(VISIBLE);
                                                new Handler(getMainLooper()).post(() -> adapter.notifyDataSetChanged());
                                            },
                                            error -> Log.e(LOG_TAG, "That didn't work! " + error.getMessage())
                                    ))).start();
    }

}

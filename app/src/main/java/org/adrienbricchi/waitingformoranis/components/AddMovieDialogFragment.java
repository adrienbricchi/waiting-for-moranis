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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.adrienbricchi.waitingformoranis.databinding.AddMovieMainBinding;
import org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService;

import java.util.ArrayList;
import java.util.Optional;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;


public class AddMovieDialogFragment extends DialogFragment {

    public static final int REQUEST_CODE = 10404;
    public static final String TAG = "AddMovieDialogFragment";

    private AddMovieDialogListAdapter adapter;
    private AddMovieMainBinding binding;


    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create the AlertDialog object and return it
        binding = AddMovieMainBinding.inflate(LayoutInflater.from(getContext()), null, false);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(android.R.string.search_go)
               .setView(binding.getRoot());

        adapter = new AddMovieDialogListAdapter(new ArrayList<>());
        binding.addMovieListRecyclerView.setHasFixedSize(true);
        binding.addMovieListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.addMovieListRecyclerView.setAdapter(adapter);

        binding.searchAppCompatEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == IME_ACTION_SEARCH) {
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
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Optional.ofNullable(getTargetFragment())
                .ifPresent(f -> f.onActivityResult(REQUEST_CODE, RESULT_OK, null));
    }


    private void searchMovie(@NonNull String searchTerm) {

        if (getActivity() == null) { return; }

        binding.searchAppCompatEditText.clearFocus();
        Optional.ofNullable((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE))
                .ifPresent(in -> in.hideSoftInputFromWindow(binding.searchAppCompatEditText.getWindowToken(), 0));

        new Thread(() -> TmdbService.searchMovie(
                getActivity(),
                searchTerm,
                movies -> {
                    adapter.getDataSet().clear();
                    adapter.getDataSet().addAll(movies);
                    new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
                },
                error -> Log.e("Adrien", "That didn't work! " + error.getMessage())
        )).start();
    }

}

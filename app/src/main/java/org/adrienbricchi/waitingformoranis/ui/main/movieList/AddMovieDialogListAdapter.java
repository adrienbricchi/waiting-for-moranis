/*
 * Waiting For Moranis
 * Copyright (C) 2020-2023
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.databinding.AddMovieListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.text.DateFormat.SHORT;
import static org.adrienbricchi.waitingformoranis.R.string.unknown_release_date;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddMovieDialogListAdapter extends RecyclerView.Adapter<AddMovieDialogListAdapter.MovieViewHolder> {


    private Set<String> knownIds;
    private List<Movie> dataSet;


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static class MovieViewHolder extends RecyclerView.ViewHolder {

        Movie currentMovie;
        final AddMovieListCellBinding binding;
        final CompoundButton.OnCheckedChangeListener listener;


        MovieViewHolder(AddMovieListCellBinding binding) {
            super(binding.getRoot());
            this.currentMovie = null;
            this.binding = binding;
            this.listener = (buttonView, isChecked) ->
                    new Thread(() -> {
                        AppDatabase database = AppDatabase.getDatabase(buttonView.getContext());
                        if (isChecked) {
                            database.movieDao().add(currentMovie);
                        } else {
                            database.movieDao().remove(currentMovie.getId());
                        }
                    }).start();
        }
    }


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull AddMovieDialogListAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        AddMovieListCellBinding binding = AddMovieListCellBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new MovieViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Movie currentMovie = dataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();

        holder.currentMovie = currentMovie;
        holder.binding.addMovieTitleTextView.setText(currentMovie.getTitle());
        holder.binding.addMovieDateTextView.setText(
                Optional.ofNullable(currentMovie.getReleaseDate())
                        .map(Date::new)
                        .map(d -> SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault()).format(d))
                        .orElseGet(() -> currentContext.getString(unknown_release_date)));

        holder.binding.addMovieMaterialCheckBox.setOnCheckedChangeListener(null);
        holder.binding.addMovieMaterialCheckBox.setChecked(knownIds.contains(currentMovie.getId()));
        holder.binding.addMovieMaterialCheckBox.setOnCheckedChangeListener(holder.listener);
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

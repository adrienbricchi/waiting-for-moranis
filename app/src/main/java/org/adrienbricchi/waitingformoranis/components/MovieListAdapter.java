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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.databinding.MovieListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.text.DateFormat.SHORT;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    private static final int TAG_MOVIE = 1315220905;


    private List<Movie> dataSet;
    private OnMovieCellLongClicked longClickListener;


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static class MovieViewHolder extends RecyclerView.ViewHolder {

        MovieListCellBinding binding;


        MovieViewHolder(MovieListCellBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    public interface OnMovieCellLongClicked {

        void onMovieCellLongClicked(Movie movie);

    }


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull MovieListAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MovieListCellBinding binding = MovieListCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        binding.getRoot().setLongClickable(true);
        binding.getRoot().setOnLongClickListener(v -> {
            longClickListener.onMovieCellLongClicked((Movie) v.getTag(TAG_MOVIE));
            return false;
        });

        return new MovieViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Movie currentMovie = dataSet.get(position);

        Picasso.get()
               .load(dataSet.get(position).getImageUrl())
               .placeholder(R.drawable.ic_local_movies_48dp)
               .into(holder.binding.coverImageView);

        holder.binding.getRoot().setTag(TAG_MOVIE, currentMovie);
        holder.binding.titleTextView.setText(currentMovie.getTitle());
        holder.binding.dateTextView.setText(
                Optional.ofNullable(currentMovie.getReleaseDate())
                        .map(Date::new)
                        .map(d -> SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault()).format(d))
                        .orElse("(unknown)"));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

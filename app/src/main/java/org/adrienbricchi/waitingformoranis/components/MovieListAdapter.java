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
import org.adrienbricchi.waitingformoranis.databinding.MovieListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.text.DateFormat.SHORT;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {


    private List<Movie> dataSet;


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


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull MovieListAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MovieListCellBinding binding = MovieListCellBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new MovieViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Picasso.get()
               .load(dataSet.get(position).getImageUrl())
               .into(holder.binding.coverImageView);

        holder.binding.titleTextView.setText(dataSet.get(position).getTitle());
        holder.binding.dateTextView.setText(SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault())
                                                            .format(new Date(dataSet.get(position).getReleaseDate())));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

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

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.databinding.MovieListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.utils.MovieUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.text.DateFormat.FULL;
import static org.adrienbricchi.waitingformoranis.R.drawable.ic_local_movies_48dp;
import static org.adrienbricchi.waitingformoranis.R.string.unknown_between_parenthesis;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.DIGITAL;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {


    private @NonNull List<Movie> dataSet;
    private SelectionTracker<String> selectionTracker;


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
    public @NonNull MovieListAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MovieListCellBinding binding = MovieListCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Movie currentMovie = dataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();
        Locale currentLocale = MovieUtils.countryLocale(Locale.getDefault().getCountry());
        Release release = MovieUtils.getRelease(currentMovie, currentLocale);

        Picasso.get()
               .load(dataSet.get(position).getImageUrl())
               .placeholder(ic_local_movies_48dp)
               .into(holder.binding.coverImageView);

        holder.binding.coverImageView.setContentDescription(currentMovie.getTitle());
        holder.binding.titleTextView.setText(currentMovie.getTitle());
        holder.binding.dateTextView.setText(
                Optional.ofNullable(release)
                        .map(r -> {
                            boolean isWeirdType = (r.getType() != THEATRICAL);
                            boolean isLocal = r.getCountry().equals(currentLocale);
                            String dateString = SimpleDateFormat.getDateInstance(FULL, r.getCountry()).format(r.getDate());

                            if (isWeirdType && !isLocal) {
                                return currentContext.getString(
                                        R.string.movie_double_parenthesis,
                                        dateString,
                                        r.getCountry().getDisplayCountry(Locale.getDefault()),
                                        (r.getType() == DIGITAL) && !TextUtils.isEmpty(r.getDescription())
                                        ? r.getDescription()
                                        : currentContext.getString(r.getType().getLabelStringResource())
                                );
                            } else if (isWeirdType) {
                                return currentContext.getString(
                                        R.string.movie_single_parenthesis,
                                        dateString,
                                        (r.getType() == DIGITAL) && !TextUtils.isEmpty(r.getDescription())
                                        ? r.getDescription()
                                        : currentContext.getString(r.getType().getLabelStringResource())
                                );
                            } else if (!isLocal) {
                                return currentContext.getString(
                                        R.string.movie_single_parenthesis,
                                        dateString,
                                        r.getCountry().getDisplayCountry(Locale.getDefault())
                                );
                            } else {
                                return dateString;
                            }
                        })
                        .orElse(currentContext.getString(unknown_between_parenthesis)));

        holder.binding.getRoot().setActivated(selectionTracker.isSelected(currentMovie.getId()));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    // <editor-fold desc="Utils">


    @Nullable Movie getMovie(@NonNull String id) {
        return dataSet.stream()
                      .filter(m -> TextUtils.equals(id, m.getId()))
                      .findFirst()
                      .orElse(null);
    }


    @Nullable String getMovieId(int position) {
        return Optional.ofNullable(dataSet.get(position))
                       .map(Movie::getId)
                       .orElse(null);
    }


    int getPosition(@NonNull String movieId) {
        return IntStream.range(0, dataSet.size())
                        .filter(Objects::nonNull)
                        .filter(i -> TextUtils.equals(movieId, dataSet.get(i).getId()))
                        .findFirst()
                        .orElse(-1);
    }


    // <editor-fold desc="Utils">

}

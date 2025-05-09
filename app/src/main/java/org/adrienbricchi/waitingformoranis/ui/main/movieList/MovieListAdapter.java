/*
 * Waiting For Moranis
 * Copyright (C) 2020-2025
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.adrienbricchi.waitingformoranis.ui.main.movieList;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso3.Picasso;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.adrienbricchi.waitingformoranis.databinding.MovieListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.utils.ReleaseUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.text.DateFormat.FULL;
import static java.util.stream.Collectors.toList;
import static org.adrienbricchi.waitingformoranis.R.drawable.ic_local_movies_color_background_48dp;
import static org.adrienbricchi.waitingformoranis.R.string.*;
import static org.adrienbricchi.waitingformoranis.models.Movie.Status.CANCELED;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.DIGITAL;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovieListAdapter extends RecyclerView.Adapter<MovieViewHolder> {


    private static final String LOG_TAG = "MovieListAdapter";

    private @Nullable String currentSearch;
    private @NonNull List<Movie> dataSet;
    private SelectionTracker<String> selectionTracker;


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MovieListCellBinding binding = MovieListCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Log.d(LOG_TAG, "onBindViewHolder holder:" + holder + " position:" + position);

        List<Movie> filteredDataSet = getFilteredDataSet();
        Movie currentMovie = filteredDataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();
        Locale currentLocale = ReleaseUtils.countryLocale(Locale.getDefault().getCountry());
        Release release = ReleaseUtils.getRelease(currentMovie, currentLocale);

        new Picasso.Builder(currentContext).build()
                                           .load(filteredDataSet.get(position).getImageUrl())
                                           .placeholder(ic_local_movies_color_background_48dp)
                                           .into(holder.binding.coverImageView);

        holder.binding.coverImageView.setContentDescription(currentMovie.getTitle());
        holder.binding.titleTextView.setText(currentMovie.getTitle());
        holder.binding.dateTextView.setText(
                Optional.ofNullable(release)
                        .map(r -> {
                            boolean isWeirdType = (r.getType() != THEATRICAL);
                            boolean isLocal = r.getCountry().equals(currentLocale);
                            String dateString = SimpleDateFormat.getDateInstance(FULL, Locale.getDefault()).format(r.getDate());

                            if (isWeirdType && !isLocal) {
                                return currentContext.getString(
                                        movie_double_parenthesis,
                                        dateString,
                                        r.getCountry().getDisplayCountry(Locale.getDefault()),
                                        (r.getType() == DIGITAL) && !TextUtils.isEmpty(r.getDescription())
                                        ? r.getDescription()
                                        : currentContext.getString(r.getType().getLabelStringResource())
                                );
                            } else if (isWeirdType) {
                                return currentContext.getString(
                                        movie_single_parenthesis,
                                        dateString,
                                        (r.getType() == DIGITAL) && !TextUtils.isEmpty(r.getDescription())
                                        ? r.getDescription()
                                        : currentContext.getString(r.getType().getLabelStringResource())
                                );
                            } else if (!isLocal) {
                                return currentContext.getString(
                                        movie_single_parenthesis,
                                        dateString,
                                        r.getCountry().getDisplayCountry(Locale.getDefault())
                                );
                            } else {
                                return dateString;
                            }
                        })
                        .orElseGet(() -> {
                            if (currentMovie.getProductionStatus() == CANCELED) {
                                return currentContext.getString(canceled);
                            } else {
                                return currentContext.getString(unknown_release_date);
                            }
                        }));

        holder.binding.getRoot().setActivated(selectionTracker.isSelected(currentMovie.getId()));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return getFilteredDataSet().size();
    }


    // <editor-fold desc="Utils">


    private @NonNull List<Movie> getFilteredDataSet() {

        if (TextUtils.isEmpty(currentSearch)) {
            return dataSet;
        }

        return dataSet.stream()
                      .filter(movie -> !TextUtils.isEmpty(movie.getTitle()))
                      .filter(movie -> movie.getTitle().toLowerCase().contains(currentSearch.toLowerCase()))
                      .collect(toList());
    }


    @Nullable Movie getMovie(@NonNull String id) {
        return dataSet.stream()
                      .filter(m -> TextUtils.equals(id, m.getId()))
                      .findFirst()
                      .orElse(null);
    }


    @Nullable String getMovieId(int position) {
        return Optional.ofNullable(getFilteredDataSet().get(position))
                       .map(Movie::getId)
                       .orElse(null);
    }


    int getPosition(@NonNull String movieId) {
        List<Movie> filteredDataSet = getFilteredDataSet();
        return IntStream.range(0, filteredDataSet.size())
                        .filter(Objects::nonNull)
                        .filter(i -> TextUtils.equals(movieId, filteredDataSet.get(i).getId()))
                        .findFirst()
                        .orElse(-1);
    }


    // </editor-fold desc="Utils">


}

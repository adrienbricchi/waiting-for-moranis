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

package org.adrienbricchi.waitingformoranis.ui.main.showList;

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
import org.adrienbricchi.waitingformoranis.databinding.ShowListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.text.DateFormat.SHORT;
import static java.util.stream.Collectors.toList;
import static org.adrienbricchi.waitingformoranis.R.drawable.ic_live_tv_color_background_48dp;
import static org.adrienbricchi.waitingformoranis.R.string.*;
import static org.adrienbricchi.waitingformoranis.models.Show.Status.RETURNING_SERIES;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShowListAdapter extends RecyclerView.Adapter<ShowViewHolder> {

    private static final String LOG_TAG = "ShowListAdapter";


    private String currentSearch;
    private @NonNull List<Show> dataSet;
    private SelectionTracker<String> selectionTracker;


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShowListCellBinding binding = ShowListCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ShowViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ShowViewHolder holder, int position) {

        Log.d(LOG_TAG, "onBindViewHolder holder:" + holder + " position:" + position);

        List<Show> filteredDataSet = getFilteredDataSet();
        Show currentShow = filteredDataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();

        new Picasso.Builder(currentContext).build()
                                           .load(filteredDataSet.get(position).getImageUrl())
                                           .placeholder(ic_live_tv_color_background_48dp)
                                           .into(holder.binding.coverImageView);

        String nextEpisodeDateLabel = Optional.ofNullable(currentShow.getNextEpisodeAirDate())
                                              .map(d -> SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault()).format(d))
                                              .orElse(currentContext.getString(unknown_release_date));

        String dateLabel = ((currentShow.getNextEpisodeSeasonNumber() != null) && (currentShow.getNextEpisodeNumber() != null))
                           ? currentContext
                                   .getString(
                                           next_episode_code_date,
                                           currentShow.getNextEpisodeSeasonNumber(),
                                           currentShow.getNextEpisodeNumber(),
                                           nextEpisodeDateLabel
                                   )
                           : currentContext.getString(next_episode_date, nextEpisodeDateLabel);

        String statusLabel = currentContext.getString(currentShow.getProductionStatus().getStringRes());

        holder.binding.titleTextView.setText(currentShow.getTitle());
        holder.binding.coverImageView.setContentDescription(currentShow.getTitle());
        holder.binding.dateTextView.setText(currentShow.getProductionStatus() == RETURNING_SERIES ? dateLabel : statusLabel);

        holder.binding.getRoot().setActivated(selectionTracker.isSelected(currentShow.getId()));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return getFilteredDataSet().size();
    }


    // <editor-fold desc="Utils">


    @Nullable Show getShow(@NonNull String id) {
        return dataSet.stream()
                      .filter(s -> TextUtils.equals(id, s.getId()))
                      .findFirst()
                      .orElse(null);
    }


    private @NonNull List<Show> getFilteredDataSet() {

        if (TextUtils.isEmpty(currentSearch)) {
            return dataSet;
        }

        return dataSet.stream()
                      .filter(show -> !TextUtils.isEmpty(show.getTitle()))
                      .filter(show -> show.getTitle().toLowerCase().contains(currentSearch.toLowerCase()))
                      .collect(toList());
    }


    @Nullable String getShowId(int position) {
        return Optional.ofNullable(getFilteredDataSet().get(position))
                       .map(Show::getId)
                       .orElse(null);
    }


    int getPosition(@NonNull String showId) {
        List<Show> filteredDataSet = getFilteredDataSet();
        return IntStream.range(0, filteredDataSet.size())
                        .filter(Objects::nonNull)
                        .filter(i -> TextUtils.equals(showId, filteredDataSet.get(i).getId()))
                        .findFirst()
                        .orElse(-1);
    }


    // </editor-fold desc="Utils">


}

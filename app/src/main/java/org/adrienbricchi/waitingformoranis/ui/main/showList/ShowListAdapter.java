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
package org.adrienbricchi.waitingformoranis.ui.main.showList;

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
import org.adrienbricchi.waitingformoranis.databinding.ShowListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.text.DateFormat.SHORT;
import static org.adrienbricchi.waitingformoranis.R.drawable.ic_baseline_live_tv_48;
import static org.adrienbricchi.waitingformoranis.R.string.*;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShowListAdapter extends RecyclerView.Adapter<ShowListAdapter.ShowViewHolder> {

    private static final String LOG_TAG = "ShowListAdapter";


    private @NonNull List<Show> dataSet;
    private SelectionTracker<String> selectionTracker;


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static class ShowViewHolder extends RecyclerView.ViewHolder {

        final ShowListCellBinding binding;


        ShowViewHolder(ShowListCellBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


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

        Show currentShow = dataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();

        Picasso.get()
               .load(dataSet.get(position).getImageUrl())
               .placeholder(ic_baseline_live_tv_48)
               .into(holder.binding.coverImageView);

        String nextEpisodeDateLabel = Optional.ofNullable(currentShow.getNextEpisodeAirDate())
                                              .map(d -> SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault()).format(d))
                                              .orElse(currentContext.getString(unknown_between_parenthesis));

        String nextDateLabel = ((currentShow.getNextEpisodeSeasonNumber() != null) && (currentShow.getNextEpisodeNumber() != null))
                               ? currentContext
                                       .getString(
                                               next_episode_code_date,
                                               currentShow.getNextEpisodeSeasonNumber(),
                                               currentShow.getNextEpisodeNumber(),
                                               nextEpisodeDateLabel
                                       )
                               : currentContext.getString(next_episode_date, nextEpisodeDateLabel);

        holder.binding.titleTextView.setText(currentShow.isInProduction() ? nextDateLabel : currentShow.getTitle());
        holder.binding.coverImageView.setContentDescription(currentShow.getTitle());
        holder.binding.dateTextView.setText(currentShow.isInProduction() ? nextDateLabel : currentContext.getText(ended));

        holder.binding.getRoot().setActivated(selectionTracker.isSelected(currentShow.getId()));
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    // <editor-fold desc="Utils">


    @Nullable Show getShow(@NonNull String id) {
        return dataSet.stream()
                      .filter(s -> TextUtils.equals(id, s.getId()))
                      .findFirst()
                      .orElse(null);
    }


    @Nullable String getShowId(int position) {
        return Optional.ofNullable(dataSet.get(position))
                       .map(Show::getId)
                       .orElse(null);
    }


    int getPosition(@NonNull String showId) {
        return IntStream.range(0, dataSet.size())
                        .filter(Objects::nonNull)
                        .filter(i -> TextUtils.equals(showId, dataSet.get(i).getId()))
                        .findFirst()
                        .orElse(-1);
    }


    // </editor-fold desc="Utils">


}

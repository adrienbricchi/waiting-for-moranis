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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.databinding.AddShowListCellBinding;
import org.adrienbricchi.waitingformoranis.models.Show;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.text.DateFormat.SHORT;
import static org.adrienbricchi.waitingformoranis.R.string.unknown_release_date;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddShowDialogListAdapter extends RecyclerView.Adapter<AddShowDialogListAdapter.ShowViewHolder> {


    private Set<String> knownIds;
    private List<Show> dataSet;


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static class ShowViewHolder extends RecyclerView.ViewHolder {

        Show currentShow;
        final AddShowListCellBinding binding;
        final CompoundButton.OnCheckedChangeListener listener;


        ShowViewHolder(AddShowListCellBinding binding) {
            super(binding.getRoot());
            this.currentShow = null;
            this.binding = binding;
            this.listener = (buttonView, isChecked) ->
                    new Thread(() -> {
                        AppDatabase database = AppDatabase.getDatabase(buttonView.getContext());
                        if (isChecked) {
                            database.showDao().add(currentShow);
                        } else {
                            database.showDao().remove(currentShow.getId());
                        }
                    }).start();
        }
    }


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public @NonNull ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        AddShowListCellBinding binding = AddShowListCellBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ShowViewHolder(binding);
    }


    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ShowViewHolder holder, int position) {

        Show currentShow = dataSet.get(position);
        Context currentContext = holder.binding.getRoot().getContext();

        holder.currentShow = currentShow;
        holder.binding.addShowTitleTextView.setText(currentShow.getTitle());
        holder.binding.addShowDateTextView.setText(
                Optional.ofNullable(currentShow.getReleaseDate())
                        .map(Date::new)
                        .map(d -> SimpleDateFormat.getDateInstance(SHORT, Locale.getDefault()).format(d))
                        .orElseGet(() -> currentContext.getString(unknown_release_date)));

        holder.binding.addShowMaterialCheckBox.setOnCheckedChangeListener(null);
        holder.binding.addShowMaterialCheckBox.setChecked(knownIds.contains(currentShow.getId()));
        holder.binding.addShowMaterialCheckBox.setOnCheckedChangeListener(holder.listener);
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

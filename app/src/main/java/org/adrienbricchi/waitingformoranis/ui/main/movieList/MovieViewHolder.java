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

package org.adrienbricchi.waitingformoranis.ui.main.movieList;

import androidx.recyclerview.widget.RecyclerView;
import org.adrienbricchi.waitingformoranis.databinding.MovieListCellBinding;


/**
 * Provide a reference to the views for each data item
 * Complex data items may need more than one view per item, and
 * you provide access to all the views for a data item in a view holder
 */
public class MovieViewHolder extends RecyclerView.ViewHolder {

    final MovieListCellBinding binding;


    MovieViewHolder(MovieListCellBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}

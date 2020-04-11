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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.text.DateFormat.SHORT;


public class MovieListFragment extends Fragment {

    private ListView movieListView;
    ArrayList<Map<String, String>> movieListData = new ArrayList<>();
    SimpleAdapter simpleAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.movie_list, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieListView = view.findViewById(R.id.list_view);

        String[] from = {"name", "date"};
        int[] to = {R.id.movieTitleTextView, R.id.movieDateTextView};

        simpleAdapter = new SimpleAdapter(
                getContext(),
                movieListData,
                R.layout.movie_list_cell,
                from,
                to
        );

        movieListView.setAdapter(simpleAdapter);

        new Thread(() -> {
            AppDatabase database = AppDatabase.getDatabase(getContext());
            movieListData.clear();
            database.movieDao()
                    .getAll()
                    .forEach(m -> {
                        Map<String, String> movieMap = new HashMap<>();
                        movieMap.put("name", m.getTitle());
                        movieMap.put("date", SimpleDateFormat.getDateInstance(SHORT).format(new Date(m.getReleaseDate())));
                        movieMap.put("imageSrc", m.getImageUrl());
                        movieListData.add(movieMap);
                    });
            simpleAdapter.notifyDataSetChanged();
        }).start();
    }

}

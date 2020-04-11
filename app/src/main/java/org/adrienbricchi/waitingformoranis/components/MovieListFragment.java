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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MovieListFragment extends Fragment {

    private ListView movieListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.movie_list, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieListView = view.findViewById(R.id.list_view);
        ArrayList<Map<String, String>> arrayList = new ArrayList<>();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "James Bond");
        hashMap.put("date", "02/12/20");
        arrayList.add(hashMap);

        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("name", "Avengers");
        hashMap2.put("date", "04/05/21");
        arrayList.add(hashMap2);

        HashMap<String, String> hashMap3 = new HashMap<>();
        hashMap3.put("name", "Star Wars");
        hashMap3.put("date", "09/08/21");
        arrayList.add(hashMap3);


        String[] from = {"name", "date"};
        int[] to = {R.id.movieTitleTextView, R.id.movieDateTextView};

        SimpleAdapter simpleAdapter = new SimpleAdapter(
                getContext(),
                arrayList,
                R.layout.movie_list_cell,
                from,
                to
        );

        movieListView.setAdapter(simpleAdapter);
    }

}

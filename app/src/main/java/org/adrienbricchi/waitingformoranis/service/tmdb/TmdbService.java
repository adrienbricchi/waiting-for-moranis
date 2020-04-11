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
package org.adrienbricchi.waitingformoranis.service.tmdb;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbMovie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbPage;

import java.util.List;

import static com.android.volley.Request.Method.GET;
import static org.adrienbricchi.waitingformoranis.BuildConfig.TMDB_KEY;


public class TmdbService {


    public static void searchMovie(@NonNull Context context,
                                   @NonNull String searchTerm,
                                   @NonNull final Response.Listener<List<? extends Movie>> onSuccess,
                                   @NonNull final Response.ErrorListener onError) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = new Uri.Builder()
                .scheme("https").authority("api.themoviedb.org")
                .appendPath("3").appendPath("search").appendPath("movie")
                .appendQueryParameter("api_key", TMDB_KEY)
                .appendQueryParameter("language", "fr-FR")
                .appendQueryParameter("query", searchTerm)
                .build().toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                GET,
                url,
                response -> {
                    TmdbPage<TmdbMovie> movies = new Gson()
                            .fromJson(response, new TypeToken<TmdbPage<TmdbMovie>>() {}.getType());

                    movies.getResults()
                          .forEach(m -> {
                              m.setImageUrl("https://image.tmdb.org/t/p/w154" + m.getPosterPath());
                              m.setReleaseDate(m.getOriginalReleaseDate());
                          });

                    onSuccess.onResponse(movies.getResults());
                },
                onError
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


}

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
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbMovie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbPage;
import org.adrienbricchi.waitingformoranis.utils.TmdbDateDeserializer;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.android.volley.Request.Method.GET;
import static org.adrienbricchi.waitingformoranis.BuildConfig.TMDB_KEY;


public class TmdbService {

    private static final String HTTPS = "https";
    private static final String API = "3";
    private static final String URL = "api.themoviedb.org";

    private static final String PATH_SEARCH = "search";
    private static final String PATH_MOVIE = "movie";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String QUERY_PARAM = "query";


    public static void searchMovie(@NonNull Context context,
                                   @NonNull String searchTerm,
                                   @NonNull final Response.Listener<List<? extends Movie>> onSuccess,
                                   @NonNull final Response.ErrorListener onError) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(URL)
                .appendPath(API).appendPath(PATH_SEARCH).appendPath(PATH_MOVIE)
                .appendQueryParameter(API_KEY_PARAM, TMDB_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .appendQueryParameter(QUERY_PARAM, searchTerm)
                .build().toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                GET,
                url,
                response -> {

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Date.class, new TmdbDateDeserializer())
                            .create();

                    TmdbPage<TmdbMovie> movies = gson.fromJson(response, new TypeToken<TmdbPage<TmdbMovie>>() {}.getType());

                    movies.getResults()
                          .forEach(m -> {
                              // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
                              m.setImageUrl("https://image.tmdb.org/t/p/w154" + m.getPosterPath());
                              m.setReleaseDate(Optional.ofNullable(m.getOriginalReleaseDate())
                                                       .map(Date::getTime)
                                                       .orElse(null));
                          });

                    onSuccess.onResponse(movies.getResults());
                },
                onError
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public static @Nullable Movie getMovie(@Nullable Context context,
                                           @NonNull String id) {

        if (context == null) { return null; }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<String> future = RequestFuture.newFuture();

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(URL)
                .appendPath(API).appendPath(PATH_MOVIE).appendPath(id)
                .appendQueryParameter(API_KEY_PARAM, TMDB_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .build().toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(GET, url, future, future);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        try {
            String response = future.get();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new TmdbDateDeserializer())
                    .create();

            TmdbMovie movie = gson.fromJson(response, TmdbMovie.class);

            // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
            movie.setImageUrl("https://image.tmdb.org/t/p/w154" + movie.getPosterPath());
            movie.setReleaseDate(Optional.ofNullable(movie.getOriginalReleaseDate())
                                         .map(Date::getTime)
                                         .orElse(null));

            return movie;

        }
        catch (InterruptedException | ExecutionException e) {
            Log.e("Adrien", "Error on request : " + e.getLocalizedMessage());
            return null;
        }
    }

}

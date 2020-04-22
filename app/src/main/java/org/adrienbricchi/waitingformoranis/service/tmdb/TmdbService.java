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
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbMovie;
import org.adrienbricchi.waitingformoranis.models.tmdb.TmdbPage;
import org.adrienbricchi.waitingformoranis.utils.JacksonRequest;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;
import static org.adrienbricchi.waitingformoranis.BuildConfig.TMDB_KEY;
import static org.adrienbricchi.waitingformoranis.MainActivity.APP_SHARED_PREFERENCES;


public class TmdbService {

    private static final String LOG_TAG = "TmdbService";
    private static final String SHARED_PREFERENCES_TMDB_API_KEY = "tmdb_api_key";

    private static final String HTTPS = "https";
    private static final String API = "3";
    private static final String URL = "api.themoviedb.org";

    private static final String PATH_SEARCH = "search";
    private static final String PATH_MOVIE = "movie";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String QUERY_PARAM = "query";


    private @NonNull Context context;


    // <editor-fold desc="Constructor">


    public static Optional<TmdbService> init(@Nullable Context context) {
        return (context == null)
               ? Optional.empty()
               : Optional.of(new TmdbService(context));
    }


    private TmdbService(@NonNull Context context) {
        this.context = context;
    }


    // </editor-fold desc="Constructor">


    public void setPrivateApiKey(@Nullable String apiKey) {

        if (TextUtils.isEmpty(apiKey)) {
            apiKey = null;
        }

        context.getSharedPreferences(APP_SHARED_PREFERENCES, MODE_PRIVATE)
               .edit()
               .putString(SHARED_PREFERENCES_TMDB_API_KEY, apiKey)
               .apply();
    }


    public @NonNull Optional<String> getPrivateApiKey() {
        return Optional.ofNullable(context.getSharedPreferences(APP_SHARED_PREFERENCES, MODE_PRIVATE)
                                          .getString(SHARED_PREFERENCES_TMDB_API_KEY, null));
    }


    public void searchMovie(@NonNull String searchTerm,
                            @NonNull final Response.Listener<List<? extends Movie>> onSuccess,
                            @NonNull final Response.ErrorListener onError) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(URL)
                .appendPath(API).appendPath(PATH_SEARCH).appendPath(PATH_MOVIE)
                .appendQueryParameter(API_KEY_PARAM, getPrivateApiKey().orElse(TMDB_KEY))
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .appendQueryParameter(QUERY_PARAM, searchTerm)
                .build().toString();

        // Request response from the provided URL.
        JacksonRequest<TmdbPage<TmdbMovie>> jacksonRequest = new JacksonRequest<>(
                url,
                new TypeReference<TmdbPage<TmdbMovie>>() {},
                response -> onSuccess.onResponse(response.getResults()),
                onError
        );

        // Add the request to the RequestQueue.
        queue.add(jacksonRequest);
    }


    public @Nullable Movie getMovie(@NonNull String id) {
        Log.v(LOG_TAG, "getMovie id:" + id);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<TmdbMovie> future = RequestFuture.newFuture();

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(URL)
                .appendPath(API).appendPath(PATH_MOVIE).appendPath(id)
                .appendQueryParameter(API_KEY_PARAM, getPrivateApiKey().orElse(TMDB_KEY))
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .build().toString();

        // Request from the provided URL.
        JacksonRequest<TmdbMovie> jacksonRequest = new JacksonRequest<>(
                url,
                new TypeReference<TmdbMovie>() {},
                future,
                future
        );

        // Add the request to the RequestQueue.
        queue.add(jacksonRequest);

        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}

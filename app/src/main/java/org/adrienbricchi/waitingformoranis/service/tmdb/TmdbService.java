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
import androidx.preference.PreferenceManager;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.utils.JacksonRequest;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.adrienbricchi.waitingformoranis.BuildConfig.TMDB_KEY;


public class TmdbService {

    public static final String COVER_URL = "https://image.tmdb.org/t/p/w154%s";

    private static final String LOG_TAG = "TmdbService";

    private static final Map<String, Long> sDelaySinceLastRequest = new HashMap<>();
    private static final Long DELAY_TIMEOUT_MS = 30 * 60 * 1000L;

    private static final String SHARED_PREFERENCES_TMDB_API_KEY = "tmdb_api_key";

    private static final String HTTPS = "https";
    private static final String API_URL = "api.themoviedb.org";
    private static final String WEB_URL = "www.themoviedb.org";
    private static final String API_VERSION = "3";

    private static final String PATH_SEARCH = "search";
    private static final String PATH_MOVIE = "movie";
    private static final String PATH_EDIT = "edit";
    private static final String PATH_RELEASE_DATES = "release_dates";

    private static final String PATH_QUERY_ACTIVE_NAV_ITEM = "active_nav_item";
    private static final String PATH_QUERY_RELEASE_INFORMATION = "release_information";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String QUERY_PARAM = "query";
    private static final String APPEND_TO_RESPONSE_PARAM = "append_to_response";


    private @NonNull final Context context;


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


    public @NonNull Uri getEditReleaseDatesUrl(@NonNull Movie movie) {
        return new Uri.Builder()
                .scheme(HTTPS).authority(WEB_URL)
                .appendPath(PATH_MOVIE).appendPath(movie.getId()).appendPath(PATH_EDIT)
                .appendQueryParameter(PATH_QUERY_ACTIVE_NAV_ITEM, PATH_QUERY_RELEASE_INFORMATION)
                .build();
    }


    public void setPrivateApiKey(@Nullable String apiKey) {

        if (TextUtils.isEmpty(apiKey)) {
            apiKey = null;
        }

        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                         .edit()
                         .putString(SHARED_PREFERENCES_TMDB_API_KEY, apiKey)
                         .apply();
    }


    public @NonNull Optional<String> getPrivateApiKey() {
        return Optional.ofNullable(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                                                    .getString(SHARED_PREFERENCES_TMDB_API_KEY, null));
    }


    public void searchMovie(@NonNull String searchTerm,
                            @NonNull final Response.Listener<List<? extends Movie>> onSuccess,
                            @NonNull final Response.ErrorListener onError) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(API_URL)
                .appendPath(API_VERSION).appendPath(PATH_SEARCH).appendPath(PATH_MOVIE)
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

        // Delay test

        if (Optional.ofNullable(sDelaySinceLastRequest.get(id))
                    .filter(t -> t < currentTimeMillis() + DELAY_TIMEOUT_MS)
                    .isPresent()) {

            Log.i(LOG_TAG, format("getMovie postponed id:%s, timeout delay not yet finished", id));
            return null;
        }

        // Instantiate the RequestQueue.

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<TmdbMovie> future = RequestFuture.newFuture();

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(API_URL)
                .appendPath(API_VERSION).appendPath(PATH_MOVIE).appendPath(id)
                .appendQueryParameter(APPEND_TO_RESPONSE_PARAM, PATH_RELEASE_DATES)
                .appendQueryParameter(API_KEY_PARAM, getPrivateApiKey().orElse(TMDB_KEY))
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .build().toString();

        Log.v(LOG_TAG, url);

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
            Movie movie = future.get();
            Log.d(LOG_TAG, format("getMovie successful id:%s title:%s", movie.getId(), movie.getTitle()));
            sDelaySinceLastRequest.put(movie.getId(), currentTimeMillis());
            return movie;
        }
        catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}

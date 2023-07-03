/*
 * Waiting For Moranis
 * Copyright (C) 2020-2023
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Show;
import org.adrienbricchi.waitingformoranis.utils.JacksonRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.android.volley.toolbox.HttpHeaderParser.parseCharset;
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
    private static final String PATH_TV = "tv";
    private static final String PATH_EDIT = "edit";
    private static final String PATH_SEASON = "season";
    private static final String PATH_RELEASE_DATES = "release_dates";

    private static final String PATH_QUERY_ACTIVE_NAV_ITEM = "active_nav_item";
    private static final String PATH_QUERY_RELEASE_INFORMATION = "release_information";
    private static final String PATH_QUERY_EPISODES = "episodes";
    private static final String PATH_QUERY_SEASONS = "seasons";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String QUERY_PARAM = "query";
    private static final String APPEND_TO_RESPONSE_PARAM = "append_to_response";


    private @NonNull final Context context;
    private ObjectMapper objectMapper = new ObjectMapper();


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


    public @NonNull Uri getEditReleaseDatesUrl(@NonNull Show show) {

        if (show.getNextEpisodeSeasonNumber() != null) {

            // https://www.themoviedb.org/tv/85271-wandavision/season/1/edit?active_nav_item=episodes
            return new Uri.Builder()
                    .scheme(HTTPS).authority(WEB_URL)
                    .appendPath(PATH_TV).appendPath(show.getId())
                    .appendPath(PATH_SEASON).appendPath(String.valueOf(show.getNextEpisodeSeasonNumber()))
                    .appendPath(PATH_EDIT)
                    .appendQueryParameter(PATH_QUERY_ACTIVE_NAV_ITEM, PATH_QUERY_EPISODES)
                    .build();
        } else {

            // https://www.themoviedb.org/tv/85271-wandavision/edit?active_nav_item=seasons
            return new Uri.Builder()
                    .scheme(HTTPS).authority(WEB_URL)
                    .appendPath(PATH_TV).appendPath(show.getId()).appendPath(PATH_EDIT)
                    .appendQueryParameter(PATH_QUERY_ACTIVE_NAV_ITEM, PATH_QUERY_SEASONS)
                    .build();
        }

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


    public void searchShow(@NonNull String searchTerm,
                           @NonNull final Response.Listener<List<? extends Show>> onSuccess,
                           @NonNull final Response.ErrorListener onError) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(API_URL)
                .appendPath(API_VERSION).appendPath(PATH_SEARCH).appendPath(PATH_TV)
                .appendQueryParameter(API_KEY_PARAM, getPrivateApiKey().orElse(TMDB_KEY))
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .appendQueryParameter(QUERY_PARAM, searchTerm)
                .build().toString();

        // Request response from the provided URL.
        JacksonRequest<TmdbPage<TmdbShow>> jacksonRequest = new JacksonRequest<>(
                url,
                new TypeReference<TmdbPage<TmdbShow>>() {},
                response -> onSuccess.onResponse(response.getResults()),
                onError
        );

        // Add the request to the RequestQueue.
        queue.add(jacksonRequest);
    }


    public @Nullable Movie refreshMovie(@NonNull Movie oldMovie) {
        Log.v(LOG_TAG, "getMovie id:" + oldMovie.getId());

        // Delay test

        if (Optional.ofNullable(sDelaySinceLastRequest.get(oldMovie.getId()))
                    .filter(t -> t < currentTimeMillis() + DELAY_TIMEOUT_MS)
                    .isPresent()) {

            Log.i(LOG_TAG, format("getMovie postponed id:%s, timeout delay not yet finished", oldMovie.getId()));
            return null;
        }

        // Instantiate the RequestQueue.

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<TmdbMovie> future = RequestFuture.newFuture();
        AtomicReference<TmdbError> atomicError = new AtomicReference<>(null);
        Response.ErrorListener errorListener = error -> {
            if (error.networkResponse != null &&error.networkResponse.statusCode == 404 && error.networkResponse.data != null) {
                try {
                    String errorCharset = parseCharset(error.networkResponse.headers);
                    String responseBody = new String(error.networkResponse.data, errorCharset);
                    TmdbError tmdbError = objectMapper.readValue(responseBody, TmdbError.class);
                    atomicError.set(tmdbError);
                } catch (IOException e) {
                    Log.w(LOG_TAG, e);
                }
            }
            // The future method has to be called too,
            // otherwise the request will never end.
            future.onErrorResponse(error);
        };

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(API_URL)
                .appendPath(API_VERSION).appendPath(PATH_MOVIE).appendPath(oldMovie.getId())
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
                errorListener
        );

        // Add the request to the RequestQueue.
        queue.add(jacksonRequest);

        try {
            Movie movie = future.get();
            Log.d(LOG_TAG, format("getMovie successful id:%s title:%s", movie.getId(), movie.getTitle()));
            sDelaySinceLastRequest.put(movie.getId(), currentTimeMillis());
            return movie;
        }
        catch (ExecutionException | InterruptedException e) {
            if (atomicError.get() != null) {
                Log.w(LOG_TAG, "Deleted movie from TMDB: " + oldMovie.getId());
                oldMovie.setProductionStatus(Movie.Status.CANCELED);
                return oldMovie;
            }
            Log.w(LOG_TAG, e);
            return null;
        }
    }


    public @Nullable Show getShow(@NonNull String id) {
        Log.v(LOG_TAG, "getShow id:" + id);

        // Delay test

        if (Optional.ofNullable(sDelaySinceLastRequest.get(id))
                    .filter(t -> t < currentTimeMillis() + DELAY_TIMEOUT_MS)
                    .isPresent()) {

            Log.i(LOG_TAG, format("getShow postponed id:%s, timeout delay not yet finished", id));
            return null;
        }

        // Instantiate the RequestQueue.

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<TmdbShow> future = RequestFuture.newFuture();

        String url = new Uri.Builder()
                .scheme(HTTPS).authority(API_URL)
                .appendPath(API_VERSION).appendPath(PATH_TV).appendPath(id)
                .appendQueryParameter(APPEND_TO_RESPONSE_PARAM, PATH_RELEASE_DATES)
                .appendQueryParameter(API_KEY_PARAM, getPrivateApiKey().orElse(TMDB_KEY))
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toLanguageTag())
                .build().toString();

        Log.v(LOG_TAG, url);

        // Request from the provided URL.
        JacksonRequest<TmdbShow> jacksonRequest = new JacksonRequest<>(
                url,
                new TypeReference<TmdbShow>() {},
                future,
                future
        );

        // Add the request to the RequestQueue.
        queue.add(jacksonRequest);

        try {
            TmdbShow tmdbShow = future.get();
            sDelaySinceLastRequest.put(tmdbShow.getId(), currentTimeMillis());
            return tmdbShow;
        }
        catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }


}

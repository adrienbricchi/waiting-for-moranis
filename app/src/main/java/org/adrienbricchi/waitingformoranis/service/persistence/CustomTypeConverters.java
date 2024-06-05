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

package org.adrienbricchi.waitingformoranis.service.persistence;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.util.*;

import static android.util.Log.getStackTraceString;
import static java.util.Collections.emptySet;
import static java.util.Locale.ROOT;
import static org.adrienbricchi.waitingformoranis.models.Show.Status.UNKNOWN;


public class CustomTypeConverters {

    private static final String LOG_TAG = "CustomTypeConverters";


    @TypeConverter
    public @NonNull Movie.Status fromMovieStatusString(String value) {

        String cleanString = Optional.ofNullable(value)
                .filter(string -> !TextUtils.isEmpty(string))
                .map(string -> string.toUpperCase(ROOT))
                .map(string -> string.replace(" ", "_"))
                .orElse("");

        switch (cleanString) {
            case "IN_PRODUCTION":
                return Movie.Status.IN_PRODUCTION;
            case "POST_PRODUCTION":
                return Movie.Status.POST_PRODUCTION;
            case "RELEASED":
                return Movie.Status.RELEASED;
            case "CANCELED":
                return Movie.Status.CANCELED;
            default:
                return Movie.Status.UNKNOWN;
        }
    }


    @TypeConverter
    public @NonNull String toStatusString(Movie.Status status) {
        return Optional.ofNullable(status)
                       .map(Movie.Status::name)
                       .orElse(UNKNOWN.name());
    }


    @TypeConverter
    public @NonNull Show.Status fromShowStatusString(String value) {

        String cleanString = Optional.ofNullable(value)
                                     .filter(string -> !TextUtils.isEmpty(string))
                                     .map(string -> string.toUpperCase(ROOT))
                                     .map(string -> string.replace(" ", "_"))
                                     .orElse("");

        switch (cleanString) {
            case "RETURNING_SERIES":
            case "IN_PRODUCTION":
            case "PILOT":
            case "PLANNED":
                return Show.Status.RETURNING_SERIES;
            case "CANCELED":
                return Show.Status.CANCELED;
            case "ENDED":
                return Show.Status.ENDED;
            default:
                return Show.Status.UNKNOWN;
        }
    }


    @TypeConverter
    public @NonNull String toStatusString(Show.Status status) {
        return Optional.ofNullable(status)
                       .map(Show.Status::name)
                       .orElse(UNKNOWN.name());
    }


    @TypeConverter
    public @NonNull List<Release> fromReleaseDateString(String value) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(value, new TypeReference<List<Release>>() {});
        }
        catch (JsonProcessingException e) {
            Log.w(LOG_TAG, getStackTraceString(e));
            return new ArrayList<>();
        }
    }


    @TypeConverter
    public @NonNull String fromReleaseDateMap(List<Release> list) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(list);
        }
        catch (JsonProcessingException e) {
            Log.w(LOG_TAG, getStackTraceString(e));
            return "[]";
        }
    }


    @TypeConverter
    public @NonNull Set<Locale> fromLocaleString(String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(value, new TypeReference<Set<Locale>>() {});
        }
        catch (JsonProcessingException e) {
            Log.w(LOG_TAG, getStackTraceString(e));
            return emptySet();
        }
    }


    @TypeConverter
    public @NonNull String fromLocaleList(Set<Locale> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        }
        catch (JsonProcessingException e) {
            Log.w(LOG_TAG, getStackTraceString(e));
            return "[]";
        }
    }


}

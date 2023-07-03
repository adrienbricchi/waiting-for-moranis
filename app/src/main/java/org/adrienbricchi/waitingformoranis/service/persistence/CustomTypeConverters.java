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

package org.adrienbricchi.waitingformoranis.service.persistence;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.util.*;

import static java.util.Collections.emptySet;
import static org.adrienbricchi.waitingformoranis.models.Show.Status.*;


public class CustomTypeConverters {


    @TypeConverter
    public @NonNull Show.Status fromStatusString(String value) {
        switch (value) {
            case "Returning Series":
            case "RETURNING_SERIES":
            case "In Production":
            case "IN_PRODUCTION":
            case "Pilot":
            case "PILOT":
            case "Planned":
            case "PLANNED":
                return RETURNING_SERIES;
            case "Canceled":
            case "CANCELED":
                return CANCELED;
            case "Ended":
            case "ENDED":
                return ENDED;
            default:
                return UNKNOWN;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return "[]";
        }
    }


}

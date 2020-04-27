package org.adrienbricchi.waitingformoranis.service.persistence;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.ReleaseType;

import java.util.*;

import static java.util.Collections.emptyList;


class CustomTypeConverters {


    @TypeConverter
    public @NonNull Map<Locale, Map<ReleaseType, Date>> fromReleaseDateString(String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(value, new TypeReference<Map<Locale, Map<ReleaseType, Date>>>() {});
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }


    @TypeConverter
    public @NonNull String fromReleaseDateMap(Map<Locale, Map<ReleaseType, Date>> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }


    @TypeConverter
    public @NonNull List<Locale> fromLocaleString(String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(value, new TypeReference<List<Locale>>() {});
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return emptyList();
        }
    }


    @TypeConverter
    public @NonNull String fromLocaleList(List<Locale> map) {
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

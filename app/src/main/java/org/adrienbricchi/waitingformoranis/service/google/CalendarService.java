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
package org.adrienbricchi.waitingformoranis.service.google;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.adrienbricchi.waitingformoranis.R;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.provider.BaseColumns._ID;
import static android.provider.CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
import static android.provider.CalendarContract.Events.*;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class CalendarService {

    private static final String LOG_TAG = "CalendarService";

    private static final String SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY = "current_google_calendar_id";
    public static final int CALENDAR_PERMISSION_REQUEST_CODE = 30112;

    /**
     * Projection array. Creating indices for this array instead of doing
     * dynamic lookups improves performance.
     */
    private static final String[] CALENDAR_PROJECTION = new String[]{_ID, CALENDAR_DISPLAY_NAME};
    private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
    private static final int PROJECTION_CALENDAR_DISPLAY_NAME_INDEX = 1;

    private static final String[] EVENT_PROJECTION = new String[]{_ID, DESCRIPTION};
    private static final int PROJECTION_EVENT_ID_INDEX = 0;
    private static final int PROJECTION_EVENT_DESCRIPTION = 1;


    public static @Nullable Long getCalendarId(@Nullable Activity activity) {

        if (activity == null) {
            return null;
        }

        long calendarId = activity.getSharedPreferences(activity.getString(R.string.app_name), MODE_PRIVATE)
                                  .getLong(SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY, -1);

        if ((calendarId > 0) && Stream.of(READ_CALENDAR, WRITE_CALENDAR)
                                      .map(p -> checkSelfPermission(activity, p))
                                      .anyMatch(p -> p == PERMISSION_DENIED)) {

            requestPermissions(activity, new String[]{READ_CALENDAR, WRITE_CALENDAR}, CALENDAR_PERMISSION_REQUEST_CODE);
            return null;
        }

        return (calendarId > 0) ? calendarId : null;
    }


    public static void setCalendarId(@Nullable Activity activity, long calendarId) {

        if (activity == null) {
            return;
        }

        activity.getSharedPreferences(activity.getString(R.string.app_name), MODE_PRIVATE)
                .edit()
                .putLong(SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY, calendarId)
                .apply();
    }


    public static @Nullable Map<Long, String> getCalendarIds(Activity activity) {

        if (Stream.of(READ_CALENDAR, WRITE_CALENDAR)
                  .map(p -> checkSelfPermission(activity, p))
                  .anyMatch(p -> p == PERMISSION_DENIED)) {

            requestPermissions(activity, new String[]{READ_CALENDAR, WRITE_CALENDAR}, CALENDAR_PERMISSION_REQUEST_CODE);
            return null;
        }

        Map<Long, String> result = new HashMap<>();

        // Run query
        Cursor cursor = activity.getContentResolver()
                                .query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        if (cursor != null) {
            while (cursor.moveToNext()) {
                result.put(
                        cursor.getLong(PROJECTION_CALENDAR_ID_INDEX),
                        cursor.getString(PROJECTION_CALENDAR_DISPLAY_NAME_INDEX)
                );
            }
            cursor.close();
        }

        return result;
    }


    @SuppressLint("MissingPermission")
    public static @NonNull Map<String, Long> getEvents(Activity activity, @Nullable Long calendarId) {
        Map<String, Long> result = new HashMap<>();

        if ((activity == null) || (calendarId == null)) {
            return result;
        }

        // Run query
        String selection = "(" + CALENDAR_ID + " = ?)";
        String[] selectionArgs = new String[]{calendarId.toString()};
        Cursor cursor = activity.getContentResolver()
                                .query(CalendarContract.Events.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        if (cursor != null) {
            while (cursor.moveToNext()) {

                long eventId = cursor.getLong(PROJECTION_EVENT_ID_INDEX);
                String movieId = cursor.getString(PROJECTION_EVENT_DESCRIPTION);

                if (!TextUtils.isEmpty(movieId)) {
                    result.put(movieId, eventId);
                }
            }
            cursor.close();
        }

        return result;
    }


    @SuppressLint("MissingPermission")
    public static @Nullable Long addMovieToCalendar(@Nullable Activity activity, @Nullable Long calendarId, @NonNull Movie movie) {
        Log.v(LOG_TAG, "addMovieToCalendar title:" + movie.getTitle());

        if ((activity == null) || (calendarId == null)) {
            return null;
        }

        ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, movie.getReleaseDate());
        values.put(DTEND, movie.getReleaseDate());
        values.put(ALL_DAY, true);
        values.put(TITLE, movie.getTitle() + " #film");
        values.put(CALENDAR_ID, calendarId);
        values.put(DESCRIPTION, movie.getId());
        values.put(EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri == null) { return null; }

        // Return the event Id that is the last element in the Uri
        return Optional.ofNullable(uri.getLastPathSegment())
                       .map(Long::parseLong)
                       .orElse(null);
    }


    @SuppressLint("MissingPermission")
    public static boolean editMovieInCalendar(@Nullable Activity activity, @Nullable Long calendarId, @NonNull Movie movie) {
        Log.v(LOG_TAG, "editMovieInCalendar title:" + movie.getTitle());

        if ((activity == null) || (calendarId == null) || (movie.getReleaseDate() == null)) {
            return false;
        }

        ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, movie.getReleaseDate());
        values.put(DTEND, movie.getReleaseDate());
        values.put(ALL_DAY, true);
        values.put(TITLE, movie.getTitle() + " #film");
        values.put(CALENDAR_ID, calendarId);
        values.put(EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, movie.getCalendarEventId());
        int numRowsUpdated = cr.update(updateUri, values, null, null);

        return (numRowsUpdated != 0);
    }


    @SuppressLint("MissingPermission")
    public static boolean deleteMovieInCalendar(@Nullable Activity activity, @Nullable Long calendarId, @NonNull Movie movie) {
        Log.v(LOG_TAG, "deleteMovieInCalendar title:" + movie.getTitle());

        if ((activity == null) || (calendarId == null) || (movie.getReleaseDate() == null)) {
            return false;
        }

        ContentResolver cr = activity.getContentResolver();
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, movie.getCalendarEventId());
        int numRowsUpdated = cr.delete(updateUri, null, null);

        return (numRowsUpdated != 0);
    }

}

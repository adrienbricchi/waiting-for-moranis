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
import androidx.preference.PreferenceManager;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.service.persistence.AppDatabase;
import org.adrienbricchi.waitingformoranis.utils.MovieUtils;

import java.util.*;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.CalendarContract.Events.*;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static java.util.Arrays.asList;
import static org.adrienbricchi.waitingformoranis.R.string.google_calendar_event_description;
import static org.adrienbricchi.waitingformoranis.R.string.hashtagged_movie;


@SuppressLint("MissingPermission")
public class CalendarService {

    public static final int PERMISSION_REQUEST_CODE = 30112;

    private static final String LOG_TAG = "CalendarService";
    private static final List<String> PERMISSIONS = asList(READ_CALENDAR, WRITE_CALENDAR);
    private static final String SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY = "current_google_calendar_id";
    private static final List<String> CALENDAR_PROJECTION = asList(_ID, CALENDAR_DISPLAY_NAME, OWNER_ACCOUNT);
    private static final List<String> EVENT_PROJECTION = asList(_ID, DESCRIPTION);


    private @NonNull final Activity activity;


    // <editor-fold desc="Constructor">


    public static Optional<CalendarService> init(@Nullable Activity activity) {
        return (activity == null)
               ? Optional.empty()
               : Optional.of(new CalendarService(activity));
    }


    private CalendarService(@NonNull Activity activity) {
        this.activity = activity;
    }


    // </editor-fold desc="Constructor">


    public boolean hasPermissions() {
        return PERMISSIONS.stream()
                          .map(p -> checkSelfPermission(activity, p))
                          .allMatch(p -> p == PERMISSION_GRANTED);
    }


    public void askPermissions() {
        requestPermissions(activity, PERMISSIONS.toArray(new String[]{}), PERMISSION_REQUEST_CODE);
    }


    public @Nullable Long getCurrentCalendarId() {

        if (!hasPermissions()) {
            return null;
        }

        long calendarId = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext())
                                           .getLong(SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY, -1);

        return (calendarId > 0) ? calendarId : null;
    }


    public void setCalendarId(long calendarId) {

        if (!hasPermissions()) {
            return;
        }

        PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext())
                         .edit()
                         .putLong(SHARED_PREFERENCES_CURRENT_GOOGLE_CALENDAR_ID_KEY, calendarId)
                         .apply();
    }


    public @Nullable Map<Long, String> getCalendarIds() {

        if (!hasPermissions()) {
            return null;
        }

        Map<Long, String> result = new HashMap<>();

        // Run query

        String selection = "(" + CalendarContract.Calendars.OWNER_ACCOUNT + " NOT LIKE ?)";
        String[] selectionArgs = new String[]{"%@group.v.calendar.google.com"};
        Cursor cursor = activity.getContentResolver()
                                .query(
                                        CalendarContract.Calendars.CONTENT_URI,
                                        CALENDAR_PROJECTION.toArray(new String[]{}),
                                        selection,
                                        selectionArgs,
                                        null
                                );

        // Use the cursor to step through the returned records
        if (cursor != null) {
            while (cursor.moveToNext()) {
                result.put(
                        cursor.getLong(CALENDAR_PROJECTION.indexOf(_ID)),
                        cursor.getString(CALENDAR_PROJECTION.indexOf(CALENDAR_DISPLAY_NAME))
                );
            }
            cursor.close();
        }

        return result;
    }


    public @NonNull Map<String, Long> getEvents(@Nullable Long calendarId) {
        Map<String, Long> result = new HashMap<>();

        if ((calendarId == null) || !hasPermissions()) {
            return result;
        }

        // Run query
        String selection = "(" + CALENDAR_ID + " = ?)";
        String[] selectionArgs = new String[]{calendarId.toString()};
        Cursor cursor = activity.getContentResolver()
                                .query(
                                        CalendarContract.Events.CONTENT_URI,
                                        EVENT_PROJECTION.toArray(new String[]{}),
                                        selection,
                                        selectionArgs,
                                        null
                                );

        // Use the cursor to step through the returned records
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(EVENT_PROJECTION.indexOf(_ID));
                String eventDescription = cursor.getString(EVENT_PROJECTION.indexOf(DESCRIPTION));
                check100To101Patch(calendarId, eventDescription);

                Optional.ofNullable(MovieUtils.getIdFromCalendarDescription(eventDescription))
                        .ifPresent(id -> result.put(id, eventId));
            }
            cursor.close();
        }

        return result;
    }


    public @Nullable Long addMovieToCalendar(@Nullable Long calendarId, @NonNull Movie movie) {
        Log.v(LOG_TAG, "addMovieToCalendar title:" + movie.getTitle());

        Release release = MovieUtils.getRelease(movie, Locale.getDefault());
        if ((calendarId == null) || (release == null) || !hasPermissions()) {
            return null;
        }

        ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, release.getDate().getTime());
        values.put(DTEND, release.getDate().getTime());
        values.put(ALL_DAY, true);
        values.put(TITLE, activity.getString(hashtagged_movie, movie.getTitle()));
        values.put(CALENDAR_ID, calendarId);
        values.put(DESCRIPTION, activity.getString(google_calendar_event_description, movie.getId()));
        values.put(EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri == null) { return null; }

        // Return the event Id that is the last element in the Uri
        return Optional.ofNullable(uri.getLastPathSegment())
                       .map(Long::parseLong)
                       .orElse(null);
    }


    public boolean editMovieInCalendar(@Nullable Long calendarId, @NonNull Movie movie) {
        return editMovieInCalendar(calendarId, movie, false);
    }


    private boolean editMovieInCalendar(@Nullable Long calendarId, @NonNull Movie movie, boolean refreshDescription) {
        Log.v(LOG_TAG, "editMovieInCalendar title:" + movie.getTitle());

        Release release = MovieUtils.getRelease(movie, Locale.getDefault());
        if ((calendarId == null) || (release == null) || !hasPermissions()) {
            return false;
        }

        ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, release.getDate().getTime());
        values.put(DTEND, release.getDate().getTime());
        values.put(ALL_DAY, true);
        values.put(TITLE, activity.getString(hashtagged_movie, movie.getTitle()));
        values.put(CALENDAR_ID, calendarId);
        values.put(EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (refreshDescription) {
            values.put(DESCRIPTION, activity.getString(google_calendar_event_description, movie.getId()));
        }

        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, movie.getCalendarEventId());
        int numRowsUpdated = cr.update(updateUri, values, null, null);

        return (numRowsUpdated != 0);
    }


    @SuppressWarnings("UnusedReturnValue")
    public boolean deleteMovieInCalendar(@NonNull Movie movie) {
        Log.v(LOG_TAG, "deleteMovieInCalendar title:" + movie.getTitle());

        if ((movie.getCalendarEventId() == null) || !hasPermissions()) {
            return false;
        }

        ContentResolver cr = activity.getContentResolver();
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, movie.getCalendarEventId());
        int numRowsUpdated = cr.delete(updateUri, null, null);

        return (numRowsUpdated != 0);
    }


    // <editor-fold desc="Patches">


    private void check100To101Patch(@Nullable Long calendarId, @Nullable String desc) {

        if ((calendarId == null) || (desc == null)) {
            return;
        }

        // Refreshing entirely the event.
        // Title and description were improved from 1.0.0 to 1.0.1
        if (desc.matches("\\d{5,6}")) {
            AppDatabase.getDatabase(activity).movieDao().get(desc)
                       .stream()
                       .peek(m -> Log.i(LOG_TAG, "Patch 1.0.0 → 1.0.1 - Updating title:" + m.getTitle()))
                       .findFirst()
                       .ifPresent(m -> this.editMovieInCalendar(calendarId, m, true));
        }

        // Removing non-movie events from the DB
        if ((!TextUtils.isEmpty(desc)) && MovieUtils.getIdFromCalendarDescription(desc) == null) {
            Log.i(LOG_TAG, "Patch 1.0.0 → 1.0.1 - Deleting : " + desc);
            AppDatabase.getDatabase(activity).movieDao().remove(desc);
        }
    }


    // </editor-fold desc="Patches">

}

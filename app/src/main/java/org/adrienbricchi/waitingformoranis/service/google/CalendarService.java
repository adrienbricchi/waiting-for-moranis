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
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import androidx.annotation.Nullable;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.provider.BaseColumns._ID;
import static android.provider.CalendarContract.Calendars.ACCOUNT_NAME;
import static android.provider.CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
import static android.provider.CalendarContract.Calendars.OWNER_ACCOUNT;
import static android.provider.CalendarContract.Calendars.SYNC_EVENTS;
import static android.provider.CalendarContract.Calendars.VISIBLE;
import static android.provider.CalendarContract.Events.CONTENT_URI;
import static android.provider.CalendarContract.Events.*;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class CalendarService {

    public static final int CALENDAR_PERMISSION_REQUEST_CODE = 30112;
    private static final String CURRENT_CALENDAR_DISPLAY_NAME = "Films";

    /**
     * Projection array. Creating indices for this array instead of doing
     * dynamic lookups improves performance.
     */
    private static final String[] EVENT_PROJECTION = new String[]{
            _ID, ACCOUNT_NAME, CALENDAR_DISPLAY_NAME, OWNER_ACCOUNT
    };

    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;


    public static @Nullable Long getCalendarId(Activity activity) {

        if (Stream.of(READ_CALENDAR, WRITE_CALENDAR)
                  .map(p -> checkSelfPermission(activity, p))
                  .anyMatch(p -> p == PERMISSION_DENIED)) {
            requestPermissions(activity, new String[]{READ_CALENDAR, WRITE_CALENDAR}, CALENDAR_PERMISSION_REQUEST_CODE);
            return null;
        }

        Long resultId = null;

        // Run query
        String selection = "(" + CALENDAR_DISPLAY_NAME + " = ?)";
        String[] selectionArgs = new String[]{CURRENT_CALENDAR_DISPLAY_NAME};
        Cursor cursor = activity.getContentResolver()
                                .query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        if (cursor != null) {
            while (cursor.moveToNext()) {
                resultId = cursor.getLong(PROJECTION_ID_INDEX);
            }
            cursor.close();
        }

        return resultId;
    }


    @SuppressLint("MissingPermission")
    public static void addMoviesToCalendar(Activity activity, long calendarId, List<Movie> movies) {

        movies.stream()
              .filter(m -> m.getCalendarEventId() == null)
              .forEach(m -> {
                  ContentResolver cr = activity.getContentResolver();
                  ContentValues values = new ContentValues();
                  values.put(DTSTART, m.getReleaseDate());
                  values.put(DTEND, m.getReleaseDate());
                  values.put(ALL_DAY, true);
                  values.put(TITLE, m.getTitle() + " #film");
                  values.put(CALENDAR_ID, calendarId);
                  values.put(EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());

                  Uri uri = cr.insert(CONTENT_URI, values);
                  if (uri == null) { return; }

                  // get the event ID that is the last element in the Uri
                  Long eventId = Optional.ofNullable(uri.getLastPathSegment())
                                         .map(Long::parseLong)
                                         .orElse(null);

                  m.setCalendarEventId(eventId);
              });
    }

}

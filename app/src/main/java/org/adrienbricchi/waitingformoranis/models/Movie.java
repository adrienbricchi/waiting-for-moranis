/*
 * Waiting For Moranis
 * Copyright (C) 2020-2025
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.adrienbricchi.waitingformoranis.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

import static org.adrienbricchi.waitingformoranis.R.string.*;
import static org.adrienbricchi.waitingformoranis.R.string.unknown_release_date;


@Keep
@Data
@NoArgsConstructor
@Entity(tableName = Movie.TABLE_NAME,
        indices = {@Index(value = Movie.FIELD_ID)})
public class Movie {

    public static final String TABLE_NAME = "movie";
    public static final String FIELD_ID = "id";


    @Keep
    @Getter
    @AllArgsConstructor
    public enum Status {

        CANCELED(canceled),
        IN_PRODUCTION(in_production),
        POST_PRODUCTION(post_production),
        RELEASED(released),
        UNKNOWN(unknown_release_date);

        private final @StringRes int stringRes;

    }


    protected @PrimaryKey @NonNull String id = UUID.randomUUID().toString();
    protected String title;
    protected String imageUrl;
    protected Long calendarEventId;

    protected @NonNull Set<Locale> productionCountries = new HashSet<>();
    protected @NonNull List<Release> releaseDates = new ArrayList<>();

    protected Long releaseDate;
    protected boolean isUpdateNeededInCalendar = false;

    protected Movie.Status productionStatus;


    public Movie(@NonNull String id) {
        this.id = id;
    }

}

/*
 * Waiting For Moranis
 * Copyright (C) 2020-2021
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
package org.adrienbricchi.waitingformoranis.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.room.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static org.adrienbricchi.waitingformoranis.R.string.*;


@Data
@Keep
@NoArgsConstructor
@Entity(tableName = Show.TABLE_NAME,
        indices = {@Index(value = Show.FIELD_ID)})
public class Show {

    public static final String TABLE_NAME = "show";
    public static final String FIELD_ID = "id";


    @Getter
    @AllArgsConstructor
    public enum Status {

        CANCELED(canceled),
        ENDED(ended),
        RETURNING_SERIES(unknown_release_date),
        UNKNOWN(unknown_release_date);

        private final @StringRes int stringRes;

    }


    @PrimaryKey
    @ColumnInfo(name = FIELD_ID)
    protected @NonNull String id = UUID.randomUUID().toString();

    protected String title;
    protected String imageUrl;

    protected Status productionStatus;
    protected Long releaseDate;

    protected Long lastEpisodeAirDate;
    protected Integer lastEpisodeNumber;
    protected Integer lastEpisodeSeasonNumber;
    protected Long nextEpisodeAirDate;
    protected Integer nextEpisodeNumber;
    protected Integer nextEpisodeSeasonNumber;

    protected Long calendarEventId;
    protected boolean isUpdateNeededInCalendar = false;

}

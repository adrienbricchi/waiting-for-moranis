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
package org.adrienbricchi.waitingformoranis.models;

import android.util.SparseArray;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@Keep
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = Show.TABLE_NAME,
        indices = {@Index(value = Show.FIELD_ID)})
public class Show {

    public static final String TABLE_NAME = "show";
    public static final String FIELD_ID = "id";

    protected @PrimaryKey @NonNull String id = UUID.randomUUID().toString();
    protected String title;
    protected String imageUrl;

    protected String status;
    protected @NonNull SparseArray<Season> seasonSparseArray = new SparseArray<>();


    public static class Season {

        protected String imageUrl;
        protected Long calendarEventId;

        protected String status;
        protected @NonNull List<Release> airDates = new ArrayList<>();

        protected Long nextAirDate;
        protected boolean isUpdateNeededInCalendar = false;

    }


}

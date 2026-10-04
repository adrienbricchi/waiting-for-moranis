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

package org.adrienbricchi.waitingformoranis.service.persistence;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.room.migration.AutoMigrationSpec;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Show;


@Database(
        entities = {Movie.class, Show.class},
        version = 16,
        autoMigrations = {
                @AutoMigration(from = 14, to = 15),
                @AutoMigration(from = 15, to = 16, spec = AppDatabase.FuzzyReleaseDateMigration.class)
        }
)
@TypeConverters({CustomTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;


    /**
     * Release dates moved from epoch millis to ISO-8601 strings.
     * TMDB dates were parsed at local midnight plus 12 hours, so the UTC day is the intended one.
     * Release lists stored as JSON are read back by {@link org.adrienbricchi.waitingformoranis.models.FuzzyDate#fromLegacyEpochMillis}.
     */
    public static class FuzzyReleaseDateMigration implements AutoMigrationSpec {

        @Override
        public void onPostMigrate(@NonNull SQLiteConnection connection) {
            SQLite.execSQL(connection, "UPDATE movie SET releaseDate = date(releaseDate / 1000, 'unixepoch') WHERE releaseDate IS NOT NULL");
            SQLite.execSQL(connection, "UPDATE show SET releaseDate = date(releaseDate / 1000, 'unixepoch') WHERE releaseDate IS NOT NULL");
        }

    }


    public abstract MovieDao movieDao();


    public abstract ShowDao showDao();


    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "appdatabase")
                           .fallbackToDestructiveMigration()
                           .build();
        }
        return INSTANCE;
    }


}
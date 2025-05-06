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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;
import static org.adrienbricchi.waitingformoranis.models.Movie.TABLE_NAME;


@Dao
public interface MovieDao {

    @Insert(onConflict = REPLACE)
    void add(Movie show);

    @Query("select * from " + TABLE_NAME)
    List<Movie> getAll();

    @Query("select * from " + TABLE_NAME + " where id = :id limit 1")
    List<Movie> get(String id);

    @Update(onConflict = REPLACE)
    void update(Movie show);

    @Query("delete from " + TABLE_NAME + " where id = :id")
    void remove(String id);

    @Query("delete from " + TABLE_NAME)
    void removeAll();

}
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

package org.adrienbricchi.waitingformoranis.service.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;
import static org.adrienbricchi.waitingformoranis.models.Show.TABLE_NAME;


@Dao
public interface ShowDao {

    @Insert(onConflict = REPLACE)
    void add(Show show);

    @Query("select * from " + TABLE_NAME)
    List<Show> getAll();

    @Query("select * from " + TABLE_NAME + " where id = :id limit 1")
    List<Show> get(String id);

    @Update(onConflict = REPLACE)
    void update(Show show);

    @Query("delete from " + TABLE_NAME + " where id = :id")
    void remove(String id);

    @Query("delete from " + TABLE_NAME)
    void removeAll();

}
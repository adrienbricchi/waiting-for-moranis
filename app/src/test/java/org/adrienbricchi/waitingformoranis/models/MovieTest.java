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

import org.junit.Test;

import java.util.Date;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.US;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;


public class MovieTest {

    @Test
    public void allArgsConstructor() {
        new Movie(
                "id_01",
                "title_z1",
                "http://image.jpg",
                1234L,
                singleton(US),
                singletonList(new Release(THEATRICAL, new Date(0L), CANADA_FRENCH)),
                9999L,
                true
        );
    }

}
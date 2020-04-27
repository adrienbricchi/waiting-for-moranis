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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;
import java.util.Locale;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Release {


    public enum Type {

        THEATRICAL,
        DIGITAL,
        PHYSICAL,
        THEATRICAL_LIMITED,
        TV,
        PREMIERE,
        UNKNOWN;


        public static int compare(Type x, Type y) {
            return (x.ordinal() < y.ordinal()) ? -1 : ((x == y) ? 0 : 1);
        }

    }


    private @NonNull Type type;
    private @NonNull Date date;
    private @NonNull Locale country;

}

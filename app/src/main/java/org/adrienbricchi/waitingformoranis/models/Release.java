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
import androidx.annotation.StringRes;
import lombok.*;

import java.util.Date;
import java.util.Locale;

import static org.adrienbricchi.waitingformoranis.R.string.*;


@Keep
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Release {


    @Keep
    @Getter
    @AllArgsConstructor
    public enum Type {

        THEATRICAL(theatrical),
        DIGITAL(digital),
        PHYSICAL(physical),
        THEATRICAL_LIMITED(theatrical_limited),
        TV(tv),
        PREMIERE(premiere),
        UNKNOWN(-1);

        private final @StringRes int labelStringResource;
    }


    private @NonNull Type type;
    private @NonNull Date date;
    private @NonNull Locale country;
    private String description = null;

}

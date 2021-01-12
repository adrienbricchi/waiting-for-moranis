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
package org.adrienbricchi.waitingformoranis.service.tmdb;

import androidx.annotation.Keep;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.models.Show;

import static org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService.COVER_URL;


@Data
@Keep
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbShow extends Show {


    @JsonProperty("poster_path")
    private void parsePosterPath(String posterPath) {
        // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
        imageUrl = String.format(COVER_URL, posterPath);
    }

}

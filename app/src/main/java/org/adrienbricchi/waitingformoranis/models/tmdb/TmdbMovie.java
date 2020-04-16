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
package org.adrienbricchi.waitingformoranis.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.models.Movie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class TmdbMovie extends Movie {

    public static final String COVER_URL = "https://image.tmdb.org/t/p/w154%s";


    private @JsonAlias("original_title") String originalTitle;
    private @JsonAlias("original_language") String originalLanguage;
    private String overview;
    private boolean video;
    private boolean adult;
    private @JsonAlias("backdrop_path") String backdropPath;
    private float popularity;
    private @JsonAlias("vote_count") int voteCount;
    private @JsonAlias("vote_average") float voteAverage;


    @JsonAlias("release_date")
    private void setReleaseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            releaseDate = Optional.ofNullable(format.parse(date))
                                  .map(Date::getTime)
                                  // We add 12 hours to it, to ease everything.
                                  // We're getting the right date, at 00:00, and GMT+/-1 tends to change the day.
                                  .map(t -> t + (12 * 60 * 60 * 1000))
                                  .orElse(null);
        }
        catch (ParseException exp) { /* Not used */ }
    }


    @JsonAlias("poster_path")
    private void setPosterPath(String posterPath) {
        // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
        imageUrl = String.format(COVER_URL, posterPath);
    }

}

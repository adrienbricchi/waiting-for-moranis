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

package org.adrienbricchi.waitingformoranis.service.tmdb;

import androidx.annotation.Keep;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.utils.ReleaseUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.*;
import static org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService.COVER_URL;
import static org.adrienbricchi.waitingformoranis.utils.ReleaseUtils.countryLocale;


@Data
@Keep
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class TmdbMovie extends Movie {


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


    @JsonProperty("release_dates")
    private void parseReleaseDates(TmdbPage<ReleaseDate> page) {
        releaseDates = new ArrayList<>();

        page.getResults()
            .forEach(w -> w.getDateWrapper()
                           .forEach(r -> releaseDates.add(new Release(
                                   r.getReleaseType(),
                                   r.releaseDate,
                                   countryLocale(w.country),
                                   r.getNote()
                           ))));
    }


    @JsonProperty("production_countries")
    private void parseProductionCountries(List<ProductionCountry> list) {
        productionCountries = list.stream()
                                  .map(ProductionCountry::getCountry)
                                  .map(ReleaseUtils::countryLocale)
                                  .filter(Objects::nonNull)
                                  .collect(toSet());
    }


    @JsonProperty("poster_path")
    private void parsePosterPath(String posterPath) {
        // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
        imageUrl = String.format(COVER_URL, posterPath);
    }


    @Data
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProductionCountry {

        private @JsonAlias("iso_3166_1") String country;
        private String name;

    }


    @Data
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ReleaseDate {

        private @JsonAlias("iso_3166_1") String country;
        private @JsonAlias("release_dates") List<TmdbDateWrapper> dateWrapper;


        @Data
        @Keep
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class TmdbDateWrapper {

            private Release.Type releaseType;
            private String certification;
            private String note;
            private @JsonAlias("iso_639_1") String language;
            private @JsonAlias("release_date") Date releaseDate;


            @JsonProperty("type")
            private void parseType(int typeIndex) {
                // https://developers.themoviedb.org/3/movies/get-movie-release-dates
                switch (typeIndex) {
                    case 1:
                        releaseType = PREMIERE;
                        break;
                    case 2:
                        releaseType = THEATRICAL_LIMITED;
                        break;
                    case 3:
                        releaseType = THEATRICAL;
                        break;
                    case 4:
                        releaseType = DIGITAL;
                        break;
                    case 5:
                        releaseType = PHYSICAL;
                        break;
                    case 6:
                        releaseType = TV;
                        break;
                    default:
                        releaseType = UNKNOWN;
                }
            }

        }

    }

}

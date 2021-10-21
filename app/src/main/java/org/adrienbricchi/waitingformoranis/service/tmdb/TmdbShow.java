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
import androidx.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.adrienbricchi.waitingformoranis.models.Show;
import org.adrienbricchi.waitingformoranis.service.persistence.CustomTypeConverters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static org.adrienbricchi.waitingformoranis.service.tmdb.TmdbService.COVER_URL;


@Data
@Keep
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbShow extends Show {


    @Keep
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EpisodeToAir {

        private Long airDate;
        private @JsonAlias("episode_number") Integer number;
        private @JsonAlias("season_number") Integer seasonNumber;


        @JsonProperty("air_date")
        private void parseAirDate(String date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                airDate = Optional.ofNullable(format.parse(date))
                                  .map(Date::getTime)
                                  // We add 12 hours to it, to ease everything.
                                  // We're getting the right date, at 00:00, and GMT+/-1 tends to change the day.
                                  .map(t -> t + (12 * 60 * 60 * 1000))
                                  .orElse(null);
            }
            catch (ParseException exp) { /* Not used */ }
        }

    }


    @JsonProperty("name")
    private void parseTitle(String name) {
        title = name;
    }


    @JsonProperty("poster_path")
    private void parsePosterPath(String posterPath) {
        // https://www.themoviedb.org/talk/53c11d4ec3a3684cf4006400
        imageUrl = String.format(COVER_URL, posterPath);
    }


    @JsonAlias("first_air_date")
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


    @JsonProperty("last_episode_to_air")
    private void parseLastEpisode(@Nullable EpisodeToAir lastEpisode) {
        lastEpisodeAirDate = Optional.ofNullable(lastEpisode).map(EpisodeToAir::getAirDate).orElse(null);
        lastEpisodeNumber = Optional.ofNullable(lastEpisode).map(EpisodeToAir::getNumber).orElse(null);
        lastEpisodeSeasonNumber = Optional.ofNullable(lastEpisode).map(EpisodeToAir::getSeasonNumber).orElse(null);
    }


    @JsonProperty("next_episode_to_air")
    private void parseNextEpisode(@Nullable EpisodeToAir nextEpisode) {
        nextEpisodeAirDate = Optional.ofNullable(nextEpisode).map(EpisodeToAir::getAirDate).orElse(null);
        nextEpisodeNumber = Optional.ofNullable(nextEpisode).map(EpisodeToAir::getNumber).orElse(null);
        nextEpisodeSeasonNumber = Optional.ofNullable(nextEpisode).map(EpisodeToAir::getSeasonNumber).orElse(null);
    }


    @JsonProperty("status")
    private void parseProductionStatus(@Nullable String status) {
        this.productionStatus = Optional.ofNullable(status)
                                        .map(s -> new CustomTypeConverters().fromStatusString(status))
                                        .orElse(Status.UNKNOWN);
    }


}

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

import com.google.gson.annotations.SerializedName;
import org.adrienbricchi.waitingformoranis.models.Movie;


public class TmdbMovie extends Movie {

    private @SerializedName("original_title") String originalTitle;
    private @SerializedName("original_language") String originalLanguage;
    private @SerializedName("release_date") String originalReleaseDate;
    private String overview;
    private boolean video;
    private boolean adult;
    private @SerializedName("poster_path") String posterPath;
    private @SerializedName("backdrop_path") String backdropPath;
    private float popularity;
    private @SerializedName("vote_count") int voteCount;
    private @SerializedName("vote_average") float voteAverage;

}

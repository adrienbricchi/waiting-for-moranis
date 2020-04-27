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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.junit.Test;

import java.util.Date;

import static java.util.Arrays.asList;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;
import static org.adrienbricchi.waitingformoranis.utils.MovieUtils.countryLocale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TmdbMovieTest {

    @SuppressWarnings("FieldCanBeLocal")
    private static String TMDB_MOVIE_FULL_EXAMPLE = "" +
            "{" +
            "    \"adult\": false," +
            "    \"backdrop_path\": \"/g5hIWYC46G1eVYPNkwaX5AisIcc.jpg\"," +
            "    \"belongs_to_collection\": null," +
            "    \"budget\": 0," +
            "    \"genres\": [{" +
            "        \"id\": 28," +
            "        \"name\": \"Action\"" +
            "    }]," +
            "    \"homepage\": \"https://www.tenetfilm.com/\"," +
            "    \"id\": 577922," +
            "    \"imdb_id\": \"tt6723592\"," +
            "    \"original_language\": \"en\"," +
            "    \"original_title\": \"Tenet\"," +
            "    \"overview\": \"Plot unknown. The project is described as an action epic revolving around international espionage.\"," +
            "    \"popularity\": 11.611," +
            "    \"poster_path\": \"/k68nPLbIST6NP96JmTxmZijEvCA.jpg\"," +
            "    \"production_companies\": [{" +
            "        \"id\": 9996," +
            "        \"logo_path\": \"/3tvBqYsBhxWeHlu62SIJ1el93O7.png\"," +
            "        \"name\": \"Syncopy\"," +
            "        \"origin_country\": \"GB\"" +
            "    }, {" +
            "        \"id\": 174," +
            "        \"logo_path\": \"/IuAlhI9eVC9Z8UQWOIDdWRKSEJ.png\"," +
            "        \"name\": \"Warner Bros. Pictures\"," +
            "        \"origin_country\": \"US\"" +
            "    }]," +
            "    \"production_countries\": [{" +
            "        \"iso_3166_1\": \"CA\"," +
            "        \"name\": \"Canada\"" +
            "    }, {" +
            "        \"iso_3166_1\": \"NO\"," +
            "        \"name\": \"Norway\"" +
            "    }, {" +
            "        \"iso_3166_1\": \"GB\"," +
            "        \"name\": \"United Kingdom\"" +
            "    }, {" +
            "        \"iso_3166_1\": \"US\"," +
            "        \"name\": \"United States of America\"" +
            "    }]," +
            "    \"release_date\": \"2020-07-15\"," +
            "    \"revenue\": 0," +
            "    \"runtime\": 195," +
            "    \"spoken_languages\": [{" +
            "        \"iso_639_1\": \"en\"," +
            "        \"name\": \"English\"" +
            "    }]," +
            "    \"status\": \"Post Production\"," +
            "    \"tagline\": \"Time runs out.\"," +
            "    \"title\": \"Tenet\"," +
            "    \"video\": false," +
            "    \"vote_average\": 0.0," +
            "    \"vote_count\": 0," +
            "    \"release_dates\": {" +
            "        \"results\": [{" +
            "            \"iso_3166_1\": \"MX\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": null," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-17T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"NO\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-17T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"RU\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-16T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"JP\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-09-18T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"FR\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"fr\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-22T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"GB\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-17T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"US\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-17T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"CH\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-22T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"GR\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-16T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"ES\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": null," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-17T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }, {" +
            "            \"iso_3166_1\": \"PH\"," +
            "            \"release_dates\": [{" +
            "                \"certification\": \"\"," +
            "                \"iso_639_1\": \"\"," +
            "                \"note\": \"\"," +
            "                \"release_date\": \"2020-07-16T00:00:00.000Z\"," +
            "                \"type\": 3" +
            "            }]" +
            "        }]" +
            "    }" +
            "}";


    @Test public void parse() throws JsonProcessingException {
        TmdbMovie movie = new ObjectMapper().readValue(TMDB_MOVIE_FULL_EXAMPLE, TmdbMovie.class);

        assertEquals("577922", movie.getId());
        assertEquals("https://image.tmdb.org/t/p/w154/k68nPLbIST6NP96JmTxmZijEvCA.jpg", movie.getImageUrl());

        assertEquals(4, movie.getProductionCountries().size());
        assertTrue(movie.getProductionCountries()
                        .containsAll(asList(
                                countryLocale("CA"),
                                countryLocale("NO"),
                                countryLocale("GB"),
                                countryLocale("US")
                        )));

        assertEquals(11, movie.getReleaseDates().size());
        assertEquals(1594807200000L, movie.getReleaseDate().longValue());
        assertEquals(1594944000000L, movie.getReleaseDates()
                                          .stream()
                                          .filter(r -> r.getType() == THEATRICAL)
                                          .filter(r -> r.getCountry().hashCode() == countryLocale("US").hashCode())
                                          .findFirst()
                                          .map(Release::getDate)
                                          .map(Date::getTime)
                                          .orElse(-1L)
                                          .longValue());
    }

}
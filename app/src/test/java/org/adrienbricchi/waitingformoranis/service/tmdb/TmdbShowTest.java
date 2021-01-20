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
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TmdbShowTest {


    @SuppressWarnings("FieldCanBeLocal")
    private static final String TMDB_SHOW_FULL_EXAMPLE = "" +
            "{" +
            "  \"backdrop_path\": \"/suopoADq0k8YZr4dQXcU6pToj6s.jpg\"," +
            "  \"created_by\": [" +
            "    {" +
            "      \"id\":9813," +
            "      \"credit_id\":\"5256c8c219c2956ff604858a\"," +
            "      \"name\":\"David Benioff\"," +
            "      \"gender\":2," +
            "      \"profile_path\":\"/xvNN5huL0X8yJ7h3IZfGG4O2zBD.jpg\"" +
            "    }," +
            "    {" +
            "      \"id\":228068," +
            "      \"credit_id\":\"552e611e9251413fea000901\"," +
            "      \"name\":\"D. B. Weiss\"," +
            "      \"gender\":2," +
            "      \"profile_path\":\"/2RMejaT793U9KRk2IEbFfteQntE.jpg\"" +
            "    }" +
            "  ]," +
            "  \"episode_run_time\": [ 60 ]," +
            "  \"first_air_date\":\"2011-04-17\"," +
            "  \"genres\": [" +
            "    {" +
            "      \"id\":10765," +
            "      \"name\":\"Sci-Fi & Fantasy\"" +
            "    }," +
            "    {" +
            "      \"id\":18," +
            "      \"name\":\"Drama\"" +
            "    }," +
            "    {" +
            "      \"id\":10759," +
            "      \"name\":\"Action & Adventure\"" +
            "    }," +
            "    {" +
            "      \"id\":9648," +
            "      \"name\":\"Mystery\"" +
            "    }" +
            "  ]," +
            "  \"homepage\":\"http://www.hbo.com/game-of-thrones\"," +
            "  \"id\":1399," +
            "  \"in_production\":false," +
            "  \"languages\": [ \"en\" ]," +
            "  \"last_air_date\":\"2019-05-19\"," +
            "  \"last_episode_to_air\": {" +
            "    \"air_date\":\"2019-05-19\"," +
            "    \"episode_number\":6," +
            "    \"id\":1551830," +
            "    \"name\":\"The Iron Throne\"," +
            "    \"overview\":\"In the aftermath of the devastating attack on King's Landing, Daenerys must face the survivors.\"," +
            "    \"production_code\":\"806\"," +
            "    \"season_number\":8," +
            "    \"still_path\":\"/3x8tJon5jXFa1ziAM93hPKNyW7i.jpg\"," +
            "    \"vote_average\":4.8," +
            "    \"vote_count\":106" +
            "  }," +
            "  \"name\":\"Game of Thrones\"," +
            "  \"next_episode_to_air\":null," +
            "  \"networks\": [" +
            "    {" +
            "      \"name\":\"HBO\"," +
            "      \"id\":49," +
            "      \"logo_path\":\"/tuomPhY2UtuPTqqFnKMVHvSb724.png\"," +
            "      \"origin_country\":\"US\"" +
            "    }" +
            "  ]," +
            "  \"number_of_episodes\":73," +
            "  \"number_of_seasons\":8," +
            "  \"origin_country\":[ \"US\" ]," +
            "  \"original_language\":\"en\"," +
            "  \"original_name\":\"Game of Thrones\"," +
            "  \"overview\":\"Seven noble families fight for control of the mythical land of Westeros.\"," +
            "  \"popularity\":369.594," +
            "  \"poster_path\":\"/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg\"," +
            "  \"production_companies\":[" +
            "    {" +
            "      \"id\":76043," +
            "      \"logo_path\":\"/9RO2vbQ67otPrBLXCaC8UMp3Qat.png\"," +
            "      \"name\":\"Revolution Sun Studios\"," +
            "      \"origin_country\":\"US\"" +
            "    }," +
            "    {" +
            "      \"id\":12525," +
            "      \"logo_path\":null," +
            "      \"name\":\"Television 360\"," +
            "      \"origin_country\":\"\"" +
            "    }," +
            "    {" +
            "      \"id\":5820," +
            "      \"logo_path\":null," +
            "      \"name\":\"Generator Entertainment\"," +
            "      \"origin_country\":\"\"" +
            "    }," +
            "    {" +
            "      \"id\":12526," +
            "      \"logo_path\":null," +
            "      \"name\":\"Bighead Littlehead\"," +
            "      \"origin_country\":\"\"" +
            "    }" +
            "  ]," +
            "  \"production_countries\":[" +
            "    {" +
            "      \"iso_3166_1\":\"GB\"," +
            "      \"name\":\"United Kingdom\"" +
            "    }," +
            "    {" +
            "      \"iso_3166_1\":\"US\"," +
            "      \"name\":\"United States of America\"" +
            "    }" +
            "  ]," +
            "  \"seasons\":[" +
            "    {" +
            "      \"air_date\":\"2010-12-05\"," +
            "      \"episode_count\":64," +
            "      \"id\":3627," +
            "      \"name\":\"Specials\"," +
            "      \"overview\":\"\"," +
            "      \"poster_path\":\"/kMTcwNRfFKCZ0O2OaBZS0nZ2AIe.jpg\"," +
            "      \"season_number\":0" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2011-04-17\"," +
            "      \"episode_count\":10," +
            "      \"id\":3624," +
            "      \"name\":\"Season 1\"," +
            "      \"overview\": \"Trouble is brewing in the Seven Kingdoms of Westeros.\"," +
            "      \"poster_path\":\"/zwaj4egrhnXOBIit1tyb4Sbt3KP.jpg\"," +
            "      \"season_number\":1" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2012-04-01\"," +
            "      \"episode_count\":10," +
            "      \"id\":3625," +
            "      \"name\":\"Season 2\"," +
            "      \"overview\": \"The cold winds of winter are rising in Westeros... War is coming...\"," +
            "      \"poster_path\":\"/5tuhCkqPOT20XPwwi9NhFnC1g9R.jpg\"," +
            "      \"season_number\":2" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2013-03-31\"," +
            "      \"episode_count\":10," +
            "      \"id\":3626," +
            "      \"name\":\"Season 3\"," +
            "      \"overview\": \"Duplicity and treachery... Nobility and honor... Conquest and triumph... And, of course, dragons.\"," +
            "      \"poster_path\":\"/7d3vRgbmnrRQ39Qmzd66bQyY7Is.jpg\"," +
            "      \"season_number\":3" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2014-04-06\"," +
            "      \"episode_count\":10," +
            "      \"id\":3628," +
            "      \"name\":\"Season 4\"," +
            "      \"overview\": \"The War of the Five Kings is drawing to a close, but new intrigues and plots are in motion.\"," +
            "      \"poster_path\":\"/dniQ7zw3mbLJkd1U0gdFEh4b24O.jpg\"," +
            "      \"season_number\":4" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2015-04-12\"," +
            "      \"episode_count\":10," +
            "      \"id\":62090," +
            "      \"name\":\"Season 5\"," +
            "      \"overview\": \"The War of the Five Kings, once thought to be drawing to a close.\"," +
            "      \"poster_path\":\"/527sR9hNDcgVDKNUE3QYra95vP5.jpg\"," +
            "      \"season_number\":5" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2016-04-24\"," +
            "      \"episode_count\":10," +
            "      \"id\":71881," +
            "      \"name\":\"Season 6\"," +
            "      \"overview\": \"Survivors from all parts of Westeros and Essos regroup to press forward, inexorably.\"," +
            "      \"poster_path\":\"/zvYrzLMfPIenxoq2jFY4eExbRv8.jpg\"," +
            "      \"season_number\":6" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2017-07-16\"," +
            "      \"episode_count\":7," +
            "      \"id\":81266," +
            "      \"name\":\"Season 7\"," +
            "      \"overview\": \"The long winter is here.\"," +
            "      \"poster_path\":\"/3dqzU3F3dZpAripEx9kRnijXbOj.jpg\"," +
            "      \"season_number\":7" +
            "    }," +
            "    {" +
            "      \"air_date\":\"2019-04-14\"," +
            "      \"episode_count\":6," +
            "      \"id\":107971," +
            "      \"name\":\"Season 8\"," +
            "      \"overview\": \"The Great War has come.\"," +
            "      \"poster_path\":\"/39FHkTLnNMjMVXdIDwZN8SxYqD6.jpg\"," +
            "      \"season_number\":8" +
            "    }" +
            "  ]," +
            "  \"spoken_languages\":[" +
            "    {" +
            "      \"english_name\":\"English\"," +
            "      \"iso_639_1\":\"en\"," +
            "      \"name\":\"English\"" +
            "    }" +
            "  ]," +
            "  \"status\":\"Ended\"," +
            "  \"tagline\":\"Winter Is Coming\"," +
            "  \"type\":\"Scripted\"," +
            "  \"vote_average\":8.3," +
            "  \"vote_count\":11504" +
            "}";


    @Test
    public void parse() throws JsonProcessingException {
        TmdbShow tvShow = new ObjectMapper().readValue(TMDB_SHOW_FULL_EXAMPLE, TmdbShow.class);

        assertEquals("1399", tvShow.getId());
        assertEquals("https://image.tmdb.org/t/p/w154/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg", tvShow.getImageUrl());
    }

}
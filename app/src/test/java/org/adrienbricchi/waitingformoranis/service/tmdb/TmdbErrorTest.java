/*
 * Waiting For Moranis
 * Copyright (C) 2020-2023
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

import static org.junit.Assert.*;


public class TmdbErrorTest {

    @SuppressWarnings("FieldCanBeLocal")
    private static final String TMDB_ERROR_FULL_EXAMPLE = "" +
            "{" +
            "  \"success\":false," +
            "  \"status_code\":34," +
            "  \"status_message\":\"The resource you requested could not be found.\"" +
            "}";


    @Test
    public void parse() throws JsonProcessingException {

        TmdbError error = new ObjectMapper().readValue(TMDB_ERROR_FULL_EXAMPLE, TmdbError.class);

        assertFalse(error.isSuccess());
        assertEquals(34, error.getStatusCode());
        assertEquals("The resource you requested could not be found.", error.getStatusMessage());
    }

}
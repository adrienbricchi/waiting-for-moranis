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

package org.adrienbricchi.waitingformoranis.service.google;

import android.app.Activity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class CalendarServiceTest {


    @Nested
    class InitTests {

        @Test
        public void shouldReturnEmptyOptionalWhenActivityIsNull() {

            Optional<CalendarService> result = CalendarService.init(null);

            assertFalse(result.isPresent());
            assertEquals(Optional.empty(), result);
        }


        @Test
        public void shouldReturnNonEmptyOptionalWhenActivityIsNotNull() {

            Activity mockActivity = Mockito.mock(Activity.class);

            Optional<CalendarService> result = CalendarService.init(mockActivity);

            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }


        @Test
        public void shouldReturnCalendarServiceInstanceWhenActivityIsValid() {

            Activity mockActivity = Mockito.mock(Activity.class);

            Optional<CalendarService> result = CalendarService.init(mockActivity);

            assertTrue(result.isPresent());
            assertInstanceOf(CalendarService.class, result.get());
        }
    }


}

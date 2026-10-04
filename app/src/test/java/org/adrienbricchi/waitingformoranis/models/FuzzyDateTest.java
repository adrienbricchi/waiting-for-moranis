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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.service.persistence.CustomTypeConverters;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static java.time.format.FormatStyle.FULL;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Arrays.asList;
import static java.util.Locale.FRANCE;
import static java.util.Locale.US;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;
import static org.junit.jupiter.api.Assertions.*;


public class FuzzyDateTest {


    @Nested
    class Parse {

        @Test
        public void day() {
            FuzzyDate date = FuzzyDate.parse("2027-03-04");
            assertNotNull(date);
            assertEquals(LocalDate.of(2027, 3, 4), date.getValue());
            assertEquals(DAYS, date.getPrecision());
        }


        @Test
        public void month() {
            FuzzyDate date = FuzzyDate.parse("2027-03");
            assertNotNull(date);
            assertEquals(YearMonth.of(2027, 3), date.getValue());
            assertEquals(MONTHS, date.getPrecision());
        }


        @Test
        public void year() {
            FuzzyDate date = FuzzyDate.parse("2028");
            assertNotNull(date);
            assertEquals(Year.of(2028), date.getValue());
            assertEquals(YEARS, date.getPrecision());
        }


        @Test
        public void blankOrInvalidIsNull() {
            assertNull(FuzzyDate.parse(null));
            assertNull(FuzzyDate.parse(""));
            assertNull(FuzzyDate.parse("soon"));
            assertNull(FuzzyDate.parse("2027-13"));
            assertNull(FuzzyDate.parse("2027-02-30"));
        }


        @Test
        public void roundTrips() {
            asList("2027-03-04", "2027-03", "2028").forEach(s -> assertEquals(s, String.valueOf(FuzzyDate.parse(s))));
        }

    }


    @Nested
    class Bounds {

        @Test
        public void day() {
            FuzzyDate date = FuzzyDate.of(LocalDate.of(2027, 3, 4));
            assertEquals(LocalDate.of(2027, 3, 4), date.earliest());
            assertEquals(LocalDate.of(2027, 3, 4), date.latest());
        }


        @Test
        public void month() {
            FuzzyDate date = FuzzyDate.of(YearMonth.of(2028, 2));
            assertEquals(LocalDate.of(2028, 2, 1), date.earliest());
            assertEquals(LocalDate.of(2028, 2, 29), date.latest());
        }


        @Test
        public void year() {
            FuzzyDate date = FuzzyDate.of(Year.of(2028));
            assertEquals(LocalDate.of(2028, 1, 1), date.earliest());
            assertEquals(LocalDate.of(2028, 12, 31), date.latest());
        }


        @Test
        public void epochMillisIsEarliestDayAtUtcMidnight() {
            assertEquals(1594944000000L, FuzzyDate.parse("2020-07-17").toEpochMillis());
            assertEquals(1577836800000L, FuzzyDate.parse("2020").toEpochMillis());
        }

    }


    @Test
    public void compareTo() {
        List<FuzzyDate> dates = new ArrayList<>(asList(
                FuzzyDate.parse("2029-01-01"),
                FuzzyDate.parse("2028"),
                FuzzyDate.parse("2028-12-31"),
                FuzzyDate.parse("2028-03"),
                FuzzyDate.parse("2028-03-04"),
                FuzzyDate.parse("2027-06-01")
        ));
        dates.sort(null);

        assertEquals(asList(
                FuzzyDate.parse("2027-06-01"),
                FuzzyDate.parse("2028-03-04"),
                FuzzyDate.parse("2028-03"),
                FuzzyDate.parse("2028-12-31"),
                FuzzyDate.parse("2028"),
                FuzzyDate.parse("2029-01-01")
        ), dates);
    }


    @Test
    public void format() {
        assertEquals("Thursday, March 4, 2027", FuzzyDate.parse("2027-03-04").format(FULL, US));
        assertEquals("04/03/2027", FuzzyDate.parse("2027-03-04").format(SHORT, FRANCE));
        assertEquals("March 2027", FuzzyDate.parse("2027-03").format(FULL, US));
        assertEquals("mars 2027", FuzzyDate.parse("2027-03").format(SHORT, FRANCE));
        assertEquals("2028", FuzzyDate.parse("2028").format(FULL, US));
    }


    @Nested
    class Json {

        private final ObjectMapper objectMapper = new ObjectMapper();


        @Test
        public void serializesAsIsoString() throws JsonProcessingException {
            assertEquals("\"2027-03\"", objectMapper.writeValueAsString(FuzzyDate.parse("2027-03")));
        }


        @Test
        public void releaseRoundTrips() {
            List<Release> releases = asList(
                    new Release(THEATRICAL, FuzzyDate.parse("2027-03-04"), US),
                    new Release(THEATRICAL, FuzzyDate.parse("2028"), FRANCE)
            );

            String json = new CustomTypeConverters().fromReleaseDateMap(releases);

            assertEquals(releases, new CustomTypeConverters().fromReleaseDateString(json));
        }


        @Test
        public void readsLegacyEpochMillis() throws JsonProcessingException {
            List<Release> releases = objectMapper.readValue(
                    "[{\"type\":\"THEATRICAL\",\"date\":1594944000000,\"country\":\"_US\",\"description\":null}]",
                    new TypeReference<List<Release>>() {}
            );

            assertEquals(FuzzyDate.parse("2020-07-17"), releases.get(0).getDate());
        }

    }


    @Nested
    class Converter {

        private final CustomTypeConverters converters = new CustomTypeConverters();


        @Test
        public void roundTrips() {
            assertEquals("2028", converters.toFuzzyDateString(converters.fromFuzzyDateString("2028")));
        }


        @Test
        public void nullStaysNull() {
            assertNull(converters.fromFuzzyDateString(null));
            assertNull(converters.toFuzzyDateString(null));
        }

    }

}

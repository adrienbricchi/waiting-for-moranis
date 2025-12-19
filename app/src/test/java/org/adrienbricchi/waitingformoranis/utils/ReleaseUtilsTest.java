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

package org.adrienbricchi.waitingformoranis.utils;

import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.models.Show;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Locale.*;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.*;
import static org.adrienbricchi.waitingformoranis.utils.ReleaseUtils.getRelease;
import static org.junit.jupiter.api.Assertions.*;


public class ReleaseUtilsTest {


    @Nested
    class CountryLocaleTests {

        @Test
        public void shouldCreateLocaleFromValidCountryCode() {

            assertEquals(CANADA.getCountry(), ReleaseUtils.countryLocale("CA").getCountry());
            assertEquals("CA", ReleaseUtils.countryLocale("CA").getCountry());
        }


        @Test
        public void shouldCreateLocaleFromInvalidCountryCode() {

            assertNotNull(ReleaseUtils.countryLocale("NOTEXISTINGCOUNTRY").getCountry());
            assertEquals("NOTEXISTINGCOUNTRY", ReleaseUtils.countryLocale("NOTEXISTINGCOUNTRY").getCountry());
        }


        @Test
        public void shouldCreateLocaleWithEmptyLanguage() {

            assertEquals("", ReleaseUtils.countryLocale("US").getLanguage());
            assertEquals("US", ReleaseUtils.countryLocale("US").getCountry());
        }


        @Test
        public void shouldSupportLocaleDisplayVariations() {

            // Demonstrates that created locales support standard locale display functionality
            assertEquals("United States", US.getDisplayCountry(US));
            assertEquals("États-Unis", US.getDisplayCountry(FRANCE));
        }
    }


    @Nested
    class GenerateMovieReleaseDateComparatorTests {

        @Test
        public void shouldCompareMoviesByReleaseDate() {

            Movie movie1 = new Movie();
            movie1.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie1.setReleaseDates(asList(
                    new Release(THEATRICAL, new Date(1L), US),
                    new Release(THEATRICAL, new Date(4L), FRANCE)
            ));

            Movie movie2 = new Movie();
            movie2.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie2.setReleaseDates(asList(
                    new Release(THEATRICAL, new Date(2L), US),
                    new Release(THEATRICAL, new Date(3L), FRANCE)
            ));

            assertEquals(-1L, ReleaseUtils.generateMovieReleaseDateComparator(US).compare(movie1, movie2));
            assertEquals(1L, ReleaseUtils.generateMovieReleaseDateComparator(FRANCE).compare(movie1, movie2));
            assertEquals(-1, ReleaseUtils.generateMovieReleaseDateComparator(US).compare(movie1, new Movie()));
        }


        @Test
        public void shouldSortMoviesByReleaseDateTitleAndId() {

            // Building test case

            Movie movie01 = new Movie() {{
                setId("id_01");
                setTitle("title_z1");
                setReleaseDates(singletonList(new Release(THEATRICAL, new Date(0L), CANADA_FRENCH)));
            }};

            Movie movie02 = new Movie() {{
                setId("id_02");
                setTitle("title_z2");
                setReleaseDates(singletonList(new Release(THEATRICAL, new Date(86400000L), CANADA_FRENCH)));
            }};

            Movie movie03 = new Movie() {{
                setId("id_03");
                setTitle("title_03");
                setReleaseDates(singletonList(new Release(THEATRICAL, new Date(2 * 86400000L), CANADA_FRENCH)));
            }};

            Movie movie03bis = new Movie() {{
                setId("id_03bis");
                setTitle("title_03");
                setReleaseDates(singletonList(new Release(THEATRICAL, new Date(2 * 86400000L), CANADA_FRENCH)));
            }};

            Movie movie03ter = new Movie() {{
                setId("id_03ter");
                setTitle(null);
                setReleaseDates(singletonList(new Release(THEATRICAL, new Date(2 * 86400000L), CANADA_FRENCH)));
            }};

            Movie movie04 = new Movie() {{
                setId("id_04");
                setTitle("title_04");
                setReleaseDates(emptyList());
            }};

            List<Movie> movieList = asList(movie04, movie02, movie03ter, movie01, movie03bis, movie03);

            // Testing

            movieList.sort(ReleaseUtils.generateMovieReleaseDateComparator(CANADA_FRENCH));
            movieList.forEach(System.out::println);
        }
    }


    @Nested
    class GetReleaseTests {

        @Test
        public void shouldGetReleaseForSpecificLocale() {

            Movie movie = new Movie();
            movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie.setReleaseDates(Arrays.asList(
                    new Release(TV, new Date(1L), US),
                    new Release(DIGITAL, new Date(3L), US),
                    new Release(THEATRICAL_LIMITED, new Date(1L), US),
                    new Release(DIGITAL, new Date(2L), CANADA),
                    new Release(THEATRICAL, new Date(4L), FRANCE)
            ));

            Release franceRelease = getRelease(movie, FRANCE);
            assertNotNull(franceRelease);
            assertEquals(FRANCE, franceRelease.getCountry());
            assertEquals(THEATRICAL, franceRelease.getType());

            Release ukRelease = getRelease(movie, UK);
            assertNotNull(ukRelease);
            assertEquals(CANADA, ukRelease.getCountry());
            assertEquals(DIGITAL, ukRelease.getType());
        }


        @Test
        public void shouldReturnNullWhenNoReleaseDates() {

            Movie movie = new Movie();
            movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie.setReleaseDates(emptyList());

            Release release = getRelease(movie, US);
            assertNull(release);
        }


        @Test
        public void shouldReturnNullWhenNoProductionCountriesAndNoMatchingLocale() {

            Movie movie = new Movie();
            movie.setProductionCountries(new HashSet<>());
            movie.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), FRANCE)));

            Release release = getRelease(movie, US);
            assertNull(release);
        }
    }


    @Nested
    class GetOriginalReleaseTests {

        @Test
        public void shouldGetOriginalReleaseFromProductionCountries() {

            Movie movie = new Movie();
            movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie.setReleaseDates(Arrays.asList(
                    new Release(TV, new Date(1L), US),
                    new Release(DIGITAL, new Date(3L), US),
                    new Release(THEATRICAL_LIMITED, new Date(1L), US),
                    new Release(DIGITAL, new Date(2L), CANADA),
                    new Release(THEATRICAL, new Date(4L), FRANCE)
            ));

            Release originalRelease = ReleaseUtils.getOriginalRelease(movie);
            assertNotNull(originalRelease);
            assertEquals(CANADA, originalRelease.getCountry());
            assertEquals(DIGITAL, originalRelease.getType());
        }


        @Test
        public void shouldReturnNullWhenNoReleaseDates() {

            Movie movie = new Movie();
            movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
            movie.setReleaseDates(emptyList());

            Release originalRelease = ReleaseUtils.getOriginalRelease(movie);
            assertNull(originalRelease);
        }
    }


    @Nested
    class CheckForCalendarUpgradeNeedMovieTests {

        @Test
        public void shouldReturnTrueWhenPreviousIsNull() {

            Movie recent = new Movie();
            recent.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(null, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenReleaseDatesChanged() {

            Movie previous = new Movie();
            previous.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            Movie recent = new Movie();
            recent.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(2L), US)));

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnFalseWhenReleaseDatesUnchanged() {

            Movie previous = new Movie();
            previous.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            Movie recent = new Movie();
            recent.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertFalse(needsUpgrade);
        }


        @Test
        public void shouldReturnFalseWhenBothReleaseDatesAreEmpty() {

            Movie previous = new Movie();
            previous.setReleaseDates(emptyList());

            Movie recent = new Movie();
            recent.setReleaseDates(emptyList());

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertFalse(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenReleaseDatesChangeFromEmptyToValue() {

            Movie previous = new Movie();
            previous.setReleaseDates(emptyList());

            Movie recent = new Movie();
            recent.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenReleaseDatesChangeFromValueToEmpty() {

            Movie previous = new Movie();
            previous.setReleaseDates(singletonList(new Release(THEATRICAL, new Date(1L), US)));

            Movie recent = new Movie();
            recent.setReleaseDates(emptyList());

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }
    }


    @Nested
    class CanceledMovieFirstComparatorTests {

        @Test
        public void shouldPutCanceledMovieBeforeNonCanceledMovie() {

            Movie canceledMovie = new Movie();
            canceledMovie.setProductionStatus(Movie.Status.CANCELED);

            Movie releasedMovie = new Movie();
            releasedMovie.setProductionStatus(Movie.Status.RELEASED);

            int result = ReleaseUtils.CANCELED_MOVIE_FIRST_COMPARATOR.compare(canceledMovie, releasedMovie);
            assertEquals(-1, result);
        }


        @Test
        public void shouldPutNonCanceledMovieAfterCanceledMovie() {

            Movie inProductionMovie = new Movie();
            inProductionMovie.setProductionStatus(Movie.Status.IN_PRODUCTION);

            Movie canceledMovie = new Movie();
            canceledMovie.setProductionStatus(Movie.Status.CANCELED);

            int result = ReleaseUtils.CANCELED_MOVIE_FIRST_COMPARATOR.compare(inProductionMovie, canceledMovie);
            assertEquals(1, result);
        }


        @Test
        public void shouldReturnZeroForTwoCanceledMovies() {

            Movie canceledMovie1 = new Movie();
            canceledMovie1.setProductionStatus(Movie.Status.CANCELED);

            Movie canceledMovie2 = new Movie();
            canceledMovie2.setProductionStatus(Movie.Status.CANCELED);

            int result = ReleaseUtils.CANCELED_MOVIE_FIRST_COMPARATOR.compare(canceledMovie1, canceledMovie2);
            assertEquals(0, result);
        }


        @Test
        public void shouldReturnZeroForTwoNonCanceledMovies() {

            Movie releasedMovie = new Movie();
            releasedMovie.setProductionStatus(Movie.Status.RELEASED);

            Movie inProductionMovie = new Movie();
            inProductionMovie.setProductionStatus(Movie.Status.IN_PRODUCTION);

            int result = ReleaseUtils.CANCELED_MOVIE_FIRST_COMPARATOR.compare(releasedMovie, inProductionMovie);
            assertEquals(0, result);
        }


        @Test
        public void shouldHandleNullStatuses() {

            Movie movieWithNullStatus1 = new Movie();
            movieWithNullStatus1.setProductionStatus(null);

            Movie movieWithNullStatus2 = new Movie();
            movieWithNullStatus2.setProductionStatus(null);

            int result = ReleaseUtils.CANCELED_MOVIE_FIRST_COMPARATOR.compare(movieWithNullStatus1, movieWithNullStatus2);
            assertEquals(0, result);
        }
    }


    @Nested
    class CheckForCalendarUpgradeNeedShowTests {

        @Test
        public void shouldReturnTrueWhenPreviousIsNull() {

            Show recent = new Show();
            recent.setNextEpisodeAirDate(1000L);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(null, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenAirDateChanged() {

            Show previous = new Show();
            previous.setNextEpisodeAirDate(1000L);

            Show recent = new Show();
            recent.setNextEpisodeAirDate(2000L);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnFalseWhenAirDateUnchanged() {

            Show previous = new Show();
            previous.setNextEpisodeAirDate(1000L);

            Show recent = new Show();
            recent.setNextEpisodeAirDate(1000L);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertFalse(needsUpgrade);
        }


        @Test
        public void shouldReturnFalseWhenBothAirDatesAreNull() {

            Show previous = new Show();
            previous.setNextEpisodeAirDate(null);

            Show recent = new Show();
            recent.setNextEpisodeAirDate(null);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertFalse(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenAirDateChangesFromNullToValue() {

            Show previous = new Show();
            previous.setNextEpisodeAirDate(null);

            Show recent = new Show();
            recent.setNextEpisodeAirDate(1000L);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }


        @Test
        public void shouldReturnTrueWhenAirDateChangesFromValueToNull() {

            Show previous = new Show();
            previous.setNextEpisodeAirDate(1000L);

            Show recent = new Show();
            recent.setNextEpisodeAirDate(null);

            boolean needsUpgrade = ReleaseUtils.checkForCalendarUpgradeNeed(previous, recent);
            assertTrue(needsUpgrade);
        }
    }


}
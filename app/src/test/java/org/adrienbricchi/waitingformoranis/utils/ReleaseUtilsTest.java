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
import org.junit.Test;

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
import static org.junit.Assert.*;


public class ReleaseUtilsTest {


    @Test
    public void countryLocale() {

        assertEquals(CANADA.getCountry(), ReleaseUtils.countryLocale("CA").getCountry());
        assertNotNull(ReleaseUtils.countryLocale("NOTEXISTINGCOUNTRY").getCountry());

        assertEquals("United States", US.getDisplayCountry(US));
        assertEquals("États-Unis", US.getDisplayCountry(FRANCE));
    }


    @Test
    public void compareMovieRelease() {

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
    public void getReleaseDate() {

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
    public void generateReleaseDateComparator() {

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


    @Test
    public void getOriginalReleaseDate_fullList() {

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
    public void getOriginalReleaseDate_emptyList() {

        Movie movie = new Movie();
        movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
        movie.setReleaseDates(emptyList());

        Release originalRelease = ReleaseUtils.getOriginalRelease(movie);
        assertNull(originalRelease);
    }


}
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
package org.adrienbricchi.waitingformoranis.utils;

import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Locale.*;
import static org.adrienbricchi.waitingformoranis.models.Release.Type.*;
import static org.junit.Assert.*;


public class MovieUtilsTest {


    @Test
    public void countryLocale() {

        assertEquals(CANADA.getCountry(), MovieUtils.countryLocale("CA").getCountry());
        Assert.assertNotNull(MovieUtils.countryLocale("NOTEXISTINGCOUNTRY").getCountry());

        assertEquals("United States", US.getDisplayCountry(US));
        assertEquals("Etats-Unis", US.getDisplayCountry(Locale.FRANCE));
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

        assertEquals(-1L, MovieUtils.compareMovieRelease(US, movie1, movie2));
        assertEquals(1L, MovieUtils.compareMovieRelease(FRANCE, movie1, movie2));
        assertEquals(MIN_VALUE, MovieUtils.compareMovieRelease(US, movie1, new Movie()));
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

        Release franceRelease = MovieUtils.getRelease(movie, FRANCE);
        assertNotNull(franceRelease);
        assertEquals(FRANCE, franceRelease.getCountry());
        assertEquals(THEATRICAL, franceRelease.getType());

        Release ukRelease = MovieUtils.getRelease(movie, UK);
        assertNotNull(ukRelease);
        assertEquals(CANADA, ukRelease.getCountry());
        assertEquals(DIGITAL, ukRelease.getType());
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

        Release originalRelease = MovieUtils.getOriginalRelease(movie);
        assertNotNull(originalRelease);
        assertEquals(CANADA, originalRelease.getCountry());
        assertEquals(DIGITAL, originalRelease.getType());
    }


    @Test
    public void getOriginalReleaseDate_emptyList() {

        Movie movie = new Movie();
        movie.setProductionCountries(new HashSet<>(asList(US, CANADA)));
        movie.setReleaseDates(emptyList());

        Release originalRelease = MovieUtils.getOriginalRelease(movie);
        assertNull(originalRelease);
    }


    @Test
    public void getIdFromCalendarDescription() {
        Assert.assertNull(MovieUtils.getIdFromCalendarDescription(null));
        Assert.assertNull(MovieUtils.getIdFromCalendarDescription(""));
        Assert.assertNull(MovieUtils.getIdFromCalendarDescription("123"));
        Assert.assertNull(MovieUtils.getIdFromCalendarDescription("Plop"));
        Assert.assertEquals("123456", MovieUtils.getIdFromCalendarDescription("123456"));
        Assert.assertEquals("123", MovieUtils.getIdFromCalendarDescription("[TMDB id:123]"));
        Assert.assertEquals("123", MovieUtils.getIdFromCalendarDescription("Plop [TMDB id:123] plop"));
    }

}
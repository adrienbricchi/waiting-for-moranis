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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;

import java.util.Locale;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;


public class MovieUtils {


    public static @NonNull Locale countryLocale(@NonNull String iso639) {
        return new Locale("", iso639);
    }


    public static boolean checkForCalendarUpgradeNeed(@Nullable Movie previous, @NonNull Movie recent) {
        return (previous == null) || !(previous.getReleaseDates().equals(recent.getReleaseDates()));
    }


    static int compareCountry(Locale x, Locale y) {
        return (x.hashCode() < y.hashCode()) ? -1 : ((x == y) ? 0 : 1);
    }


    public static int compareRelease(@NonNull Release o1, @NonNull Release o2) {
        return (MovieUtils.compareCountry(o1.getCountry(), o2.getCountry()) * 100)
                + (Release.Type.compare(o1.getType(), o2.getType()) * 10)
                + Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
    }


    public static int compareMovieRelease(@NonNull Locale locale, @NonNull Movie o1, @NonNull Movie o2) {

        Release o1Release = getRelease(o1, locale);
        Release o2Release = getRelease(o2, locale);

        return (o1Release == null)
               ? MAX_VALUE : (o2Release == null)
                             ? MIN_VALUE : Long.compare(o1Release.getDate().getTime(), o2Release.getDate().getTime());
    }


    public static @Nullable Release getRelease(@NonNull Movie movie, @NonNull Locale locale) {
        return movie.getReleaseDates()
                    .stream()
                    .filter(r -> r.getCountry() == locale)
                    .min(MovieUtils::compareRelease)
                    .orElse(getOriginalRelease(movie));
    }


    public static @Nullable Release getOriginalRelease(@NonNull Movie movie) {
        return movie.getReleaseDates()
                    .stream()
                    .filter(r -> movie.getProductionCountries().contains(r.getCountry()))
                    .min(MovieUtils::compareRelease)
                    .orElse(null);
    }

}
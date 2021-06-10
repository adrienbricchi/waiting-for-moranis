/*
 * Waiting For Moranis
 * Copyright (C) 2020-2021
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

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;


public class ReleaseUtils {


    private static final Function<Release, Integer> COUNTRY_HASH_EXTRACTOR = r -> Optional.ofNullable(r.getCountry())
                                                                                          .map(Locale::hashCode)
                                                                                          .orElse(null);

    private static final Comparator<Release> RELEASE_COMPARATOR =
            comparing(COUNTRY_HASH_EXTRACTOR, comparing(r -> r, nullsLast(naturalOrder())))
                    .thenComparing(Release::getType, nullsLast(naturalOrder()))
                    .thenComparing(Release::getDate, nullsLast(naturalOrder()));


    public static @NonNull Locale countryLocale(@NonNull String iso639) {
        return new Locale("", iso639);
    }


    public static boolean checkForCalendarUpgradeNeed(@Nullable Movie previous, @NonNull Movie recent) {
        return (previous == null) || !(previous.getReleaseDates().equals(recent.getReleaseDates()));
    }


    public static boolean checkForCalendarUpgradeNeed(@Nullable Show previous, @NonNull Show recent) {
        return (previous == null) || !Objects.equals(previous.getNextEpisodeAirDate(), recent.getNextEpisodeAirDate());
    }


    public static Comparator<Movie> generateMovieReleaseDateComparator(@NonNull Locale locale) {

        Function<Movie, Date> movieReleaseExtractor = movie -> Optional.ofNullable(getRelease(movie, locale))
                                                                       .map(Release::getDate)
                                                                       .orElse(null);

        return comparing(movieReleaseExtractor, comparing(d -> d, nullsLast(naturalOrder())))
                .thenComparing(Movie::getTitle, nullsLast(naturalOrder()))
                .thenComparing(Movie::getId, nullsLast(naturalOrder()));
    }


    public static Comparator<Show> generateShowDateComparator() {
        return comparing(Show::isInProduction, nullsLast(reverseOrder()))
                .thenComparing(Show::getNextEpisodeAirDate, nullsLast(naturalOrder()))
                .thenComparing(Show::getTitle, nullsLast(naturalOrder()))
                .thenComparing(Show::getId, nullsLast(naturalOrder()));
    }


    public static @Nullable Release getRelease(@NonNull Movie movie, @NonNull Locale locale) {
        return movie.getReleaseDates()
                    .stream()
                    .filter(r -> TextUtils.equals(r.getCountry().getCountry(), locale.getCountry()))
                    .min(RELEASE_COMPARATOR)
                    .orElseGet(() -> getOriginalRelease(movie));
    }


    static @Nullable Release getOriginalRelease(@NonNull Movie movie) {
        return movie.getReleaseDates()
                    .stream()
                    .filter(r -> movie.getProductionCountries().contains(r.getCountry()))
                    .min(RELEASE_COMPARATOR)
                    .orElse(null);
    }


}
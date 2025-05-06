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

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.adrienbricchi.waitingformoranis.models.Movie;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.adrienbricchi.waitingformoranis.models.Show;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static org.adrienbricchi.waitingformoranis.models.Movie.Status.CANCELED;


public class ReleaseUtils {


    private static final Function<Release, Integer> COUNTRY_HASH_EXTRACTOR = r -> Optional.of(r.getCountry())
                                                                                          .map(Locale::hashCode)
                                                                                          .orElse(null);

    private static final Comparator<Release> RELEASE_COMPARATOR =
            comparing(COUNTRY_HASH_EXTRACTOR, comparing(r -> r, nullsLast(naturalOrder())))
                    .thenComparing(Release::getType, nullsLast(naturalOrder()))
                    .thenComparing(Release::getDate, nullsLast(naturalOrder()));


    public static final Comparator<Show> SHOW_COMPARATOR =
            comparing(Show::getProductionStatus, nullsLast(naturalOrder()))
                    .thenComparing(Show::getNextEpisodeAirDate, nullsLast(naturalOrder()))
                    .thenComparing(Show::getTitle, nullsLast(naturalOrder()))
                    .thenComparing(Show::getId, nullsLast(naturalOrder()));


    public static final Comparator<Movie> CANCELED_MOVIE_FIRST_COMPARATOR = comparing(
                    Movie::getProductionStatus,
                    (status1, status2) -> (status1 == status2) ? 0 : (status1 == CANCELED) ? -1 : (status2 == CANCELED) ? 1 : 0
            );



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
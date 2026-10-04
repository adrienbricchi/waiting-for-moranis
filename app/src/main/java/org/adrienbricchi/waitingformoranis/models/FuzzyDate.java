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

import android.text.TextUtils;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.reverseOrder;


/**
 * A date known to the day, the month, or only the year, in ISO-8601 reduced precision: {@code 2027-03-04}, {@code 2027-03} or {@code 2028}.
 * Ordered by the last day it may fall on, so a known date comes before a vaguer one covering it.
 */
@Keep
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FuzzyDate implements Comparable<FuzzyDate> {

    private static final Comparator<FuzzyDate> COMPARATOR = Comparator.comparing(FuzzyDate::latest).thenComparing(FuzzyDate::earliest, reverseOrder());


    private final @NonNull Temporal value;


    public static @NonNull FuzzyDate of(@NonNull LocalDate date) {
        return new FuzzyDate(date);
    }


    public static @NonNull FuzzyDate of(@NonNull YearMonth yearMonth) {
        return new FuzzyDate(yearMonth);
    }


    public static @NonNull FuzzyDate of(@NonNull Year year) {
        return new FuzzyDate(year);
    }


    /**
     * @return null on a blank or unparseable value, as TMDB sends an empty string for unknown dates.
     */
    @JsonCreator
    public static @Nullable FuzzyDate parse(@Nullable String value) {

        if (TextUtils.isEmpty(value)) {
            return null;
        }

        String trimmed = value.trim();
        try {
            switch ((int) trimmed.chars().filter(c -> c == '-').count()) {
                case 0:
                    return of(Year.parse(trimmed));
                case 1:
                    return of(YearMonth.parse(trimmed));
                case 2:
                    return of(LocalDate.parse(trimmed));
                default:
                    return null;
            }
        }
        catch (DateTimeParseException e) {
            return null;
        }
    }


    /**
     * Release dates were serialized as epoch millis at UTC midnight before database version 16.
     */
    @JsonCreator
    public static @NonNull FuzzyDate fromLegacyEpochMillis(long epochMillis) {
        return of(Instant.ofEpochMilli(epochMillis).atZone(UTC).toLocalDate());
    }


    public @NonNull ChronoUnit getPrecision() {
        return (value instanceof LocalDate) ? ChronoUnit.DAYS : (value instanceof YearMonth) ? ChronoUnit.MONTHS : ChronoUnit.YEARS;
    }


    public @NonNull LocalDate earliest() {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return (value instanceof YearMonth) ? ((YearMonth) value).atDay(1) : ((Year) value).atDay(1);
    }


    public @NonNull LocalDate latest() {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return (value instanceof YearMonth) ? ((YearMonth) value).atEndOfMonth() : ((Year) value).atMonth(Month.DECEMBER).atEndOfMonth();
    }


    /**
     * @return the earliest day at UTC midnight, as {@link android.provider.CalendarContract} expects for all-day events.
     */
    public long toEpochMillis() {
        return earliest().atStartOfDay(UTC).toInstant().toEpochMilli();
    }


    /**
     * @param style only applies to day-precise dates, a month is always spelled out and a year is just the year.
     */
    public @NonNull String format(@NonNull FormatStyle style, @NonNull Locale locale) {
        switch (getPrecision()) {
            case DAYS:
                return DateTimeFormatter.ofLocalizedDate(style).withLocale(locale).format(value);
            case MONTHS:
                return DateTimeFormatter.ofPattern("LLLL uuuu", locale).format(value);
            default:
                return value.toString();
        }
    }


    @Override
    public int compareTo(@NonNull FuzzyDate other) {
        return COMPARATOR.compare(this, other);
    }


    @JsonValue
    @Override
    public @NonNull String toString() {
        return value.toString();
    }

}

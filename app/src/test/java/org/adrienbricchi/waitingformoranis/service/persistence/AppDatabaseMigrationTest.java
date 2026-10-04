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

package org.adrienbricchi.waitingformoranis.service.persistence;

import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import androidx.sqlite.SQLiteStatement;
import androidx.sqlite.driver.bundled.BundledSQLiteDriver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adrienbricchi.waitingformoranis.models.FuzzyDate;
import org.adrienbricchi.waitingformoranis.models.Release;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.adrienbricchi.waitingformoranis.models.Release.Type.THEATRICAL;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Runs Room's generated auto-migrations on a bundled SQLite, starting from the exported schemas.
 */
public class AppDatabaseMigrationTest {

    private static final File SCHEMA_FOLDER = new File("schemas", AppDatabase.class.getName());

    private SQLiteConnection connection;


    @BeforeEach
    public void openDatabase() {
        connection = new BundledSQLiteDriver().open(":memory:");
    }


    @AfterEach
    public void closeDatabase() {
        connection.close();
    }


    @Nested
    class From15To16 {

        @BeforeEach
        public void migrate() throws IOException {
            createSchema(15);

            String insertMovie = "INSERT INTO movie (id, productionCountries, releaseDates, releaseDate, isUpdateNeededInCalendar) VALUES (?, '[]', ?, ?, 0)";
            execute(insertMovie, "paris", "[{\"type\":\"THEATRICAL\",\"date\":1594944000000,\"country\":\"_US\",\"description\":null}]", 1594807200000L);
            execute(insertMovie, "pagoPago", "[]", 1594854000000L);
            execute(insertMovie, "unknown", "[]", null);

            String insertShow = "INSERT INTO show (id, releaseDate, nextEpisodeAirDate, isUpdateNeededInCalendar) VALUES (?, ?, ?, 0)";
            execute(insertShow, "paris", 1303034400000L, 1303034400000L);
            execute(insertShow, "unknown", null, null);

            new AppDatabase_AutoMigration_15_16_Impl().migrate(connection);
        }


        @Test
        public void matchesExportedSchema() throws IOException {
            assertSchema(16);
        }


        @Test
        public void movieReleaseDateIsIsoText() {
            assertEquals("2020-07-15", queryText("SELECT releaseDate FROM movie WHERE id = 'paris'"));
            assertEquals("text", queryText("SELECT typeof(releaseDate) FROM movie WHERE id = 'paris'"));
        }


        @Test
        public void westernmostTimeZoneKeepsItsDay() {
            assertEquals("2020-07-15", queryText("SELECT releaseDate FROM movie WHERE id = 'pagoPago'"));
        }


        @Test
        public void showReleaseDateIsIsoText() {
            assertEquals("2011-04-17", queryText("SELECT releaseDate FROM show WHERE id = 'paris'"));
        }


        @Test
        public void episodeAirDateStaysEpochMillis() {
            assertEquals("integer", queryText("SELECT typeof(nextEpisodeAirDate) FROM show WHERE id = 'paris'"));
        }


        @Test
        public void unknownReleaseDateStaysNull() {
            assertNull(queryText("SELECT releaseDate FROM movie WHERE id = 'unknown'"));
            assertNull(queryText("SELECT releaseDate FROM show WHERE id = 'unknown'"));
        }


        @Test
        public void legacyReleaseListIsStillReadable() {
            List<Release> releases = new CustomTypeConverters().fromReleaseDateString(queryText("SELECT releaseDates FROM movie WHERE id = 'paris'"));

            assertEquals(1, releases.size());
            assertEquals(THEATRICAL, releases.get(0).getType());
            assertEquals(FuzzyDate.parse("2020-07-17"), releases.get(0).getDate());
        }


        @Test
        public void readsBackThroughTypeConverter() {
            assertEquals(FuzzyDate.parse("2020-07-15"), new CustomTypeConverters().fromFuzzyDateString(queryText("SELECT releaseDate FROM movie WHERE id = 'paris'")));
        }

    }


    private static JsonNode readSchema(int version) throws IOException {
        return new ObjectMapper().readTree(new File(SCHEMA_FOLDER, version + ".json")).get("database");
    }


    private void createSchema(int version) throws IOException {
        for (JsonNode entity : readSchema(version).get("entities")) {
            String tableName = entity.get("tableName").asText();
            SQLite.execSQL(connection, entity.get("createSql").asText().replace("${TABLE_NAME}", tableName));
            for (JsonNode index : entity.get("indices")) {
                SQLite.execSQL(connection, index.get("createSql").asText().replace("${TABLE_NAME}", tableName));
            }
        }
    }


    private void assertSchema(int version) throws IOException {
        for (JsonNode entity : readSchema(version).get("entities")) {
            String tableName = entity.get("tableName").asText();

            Set<String> primaryKey = new HashSet<>();
            entity.get("primaryKey").get("columnNames").forEach(c -> primaryKey.add(c.asText()));

            Set<String> expectedColumns = new HashSet<>();
            entity.get("fields").forEach(f -> expectedColumns.add(String.join(
                    " ",
                    f.get("columnName").asText(),
                    f.get("affinity").asText(),
                    f.path("notNull").asBoolean() ? "NOT NULL" : "NULL",
                    primaryKey.contains(f.get("columnName").asText()) ? "PK" : "-"
            )));

            Set<String> actualColumns = new HashSet<>();
            try (SQLiteStatement statement = connection.prepare("PRAGMA table_info(`" + tableName + "`)")) {
                while (statement.step()) {
                    actualColumns.add(String.join(
                            " ",
                            statement.getText(1),
                            statement.getText(2),
                            (statement.getLong(3) != 0) ? "NOT NULL" : "NULL",
                            (statement.getLong(5) != 0) ? "PK" : "-"
                    ));
                }
            }
            assertEquals(expectedColumns, actualColumns, tableName);

            Set<String> expectedIndices = new HashSet<>();
            entity.get("indices").forEach(i -> expectedIndices.add(i.get("name").asText()));

            Set<String> actualIndices = new HashSet<>();
            try (SQLiteStatement statement = connection.prepare("PRAGMA index_list(`" + tableName + "`)")) {
                while (statement.step()) {
                    if (statement.getText(1).startsWith("index_")) {
                        actualIndices.add(statement.getText(1));
                    }
                }
            }
            assertEquals(expectedIndices, actualIndices, tableName);
        }
    }


    private void execute(String sql, Object... values) {
        try (SQLiteStatement statement = connection.prepare(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    statement.bindNull(i + 1);
                } else if (values[i] instanceof Long) {
                    statement.bindLong(i + 1, (Long) values[i]);
                } else {
                    statement.bindText(i + 1, (String) values[i]);
                }
            }
            statement.step();
        }
    }


    private String queryText(String sql) {
        try (SQLiteStatement statement = connection.prepare(sql)) {
            assertTrue(statement.step(), sql);
            return statement.isNull(0) ? null : statement.getText(0);
        }
    }

}

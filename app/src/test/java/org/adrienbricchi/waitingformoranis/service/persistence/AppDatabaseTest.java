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

import android.content.Context;
import androidx.room.Room;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;


public class AppDatabaseTest {


    @Nested
    class GetDatabaseTests {

        private Context mockContext;


        @BeforeEach
        public void setUp() {
            mockContext = Mockito.mock(Context.class);
            // Reset the static INSTANCE field before each test
            resetDatabaseInstance();
        }


        @AfterEach
        public void tearDown() {
            // Clean up after tests
            resetDatabaseInstance();
        }


        @Test
        public void shouldReturnNonNullDatabaseInstance() {

            AppDatabase database = AppDatabase.getDatabase(mockContext);

            assertNotNull(database);
        }


        @Test
        public void shouldReturnAppDatabaseInstance() {

            AppDatabase database = AppDatabase.getDatabase(mockContext);

            assertInstanceOf(AppDatabase.class, database);
        }


        @Test
        public void shouldReturnSameSingletonInstanceOnMultipleCalls() {

            AppDatabase database1 = AppDatabase.getDatabase(mockContext);
            AppDatabase database2 = AppDatabase.getDatabase(mockContext);

            assertSame(database1, database2);
        }


        @Test
        public void shouldHaveMovieDaoAccessor() {

            AppDatabase database = AppDatabase.getDatabase(mockContext);

            assertNotNull(database.movieDao());
        }


        @Test
        public void shouldHaveShowDaoAccessor() {

            AppDatabase database = AppDatabase.getDatabase(mockContext);

            assertNotNull(database.showDao());
        }


        /**
         * Helper method to reset the singleton INSTANCE using reflection.
         * This ensures test isolation.
         */
        private void resetDatabaseInstance() {
            try {
                Field instanceField = AppDatabase.class.getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (Exception e) {
                // If reflection fails, tests may not be properly isolated
                System.err.println("Warning: Could not reset AppDatabase.INSTANCE: " + e.getMessage());
            }
        }
    }


}

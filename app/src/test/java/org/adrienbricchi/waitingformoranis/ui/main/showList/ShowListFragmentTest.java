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

package org.adrienbricchi.waitingformoranis.ui.main.showList;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ShowListFragmentTest {


    @Nested
    class NewInstanceTests {

        @Test
        public void shouldReturnNonNullInstance() {

            ShowListFragment fragment = ShowListFragment.newInstance();

            assertNotNull(fragment);
        }


        @Test
        public void shouldReturnShowListFragmentInstance() {

            ShowListFragment fragment = ShowListFragment.newInstance();

            assertInstanceOf(ShowListFragment.class, fragment);
        }


        @Test
        public void shouldReturnNewInstanceOnEachCall() {

            ShowListFragment fragment1 = ShowListFragment.newInstance();
            ShowListFragment fragment2 = ShowListFragment.newInstance();

            assertNotSame(fragment1, fragment2);
        }
    }


}

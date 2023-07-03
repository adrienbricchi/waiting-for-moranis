/*
 * Waiting For Moranis
 * Copyright (C) 2020-2026
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

package org.adrienbricchi.waitingformoranis.models;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;


public class ReleaseTest {


    @Test
    public void noArgsConstructor() {
        new Release();
    }


    @Test
    public void typeStringResource() {
        Arrays.stream(Release.Type.values())
              .forEach(t -> assertTrue(t.getLabelStringResource() >= -1));
    }

}
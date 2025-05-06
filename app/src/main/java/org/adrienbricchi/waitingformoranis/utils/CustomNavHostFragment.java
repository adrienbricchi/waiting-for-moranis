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

import androidx.annotation.Keep;
import androidx.navigation.fragment.NavHostFragment;


/**
 * For some reasons, <a href="https://developer.android.com/guide/navigation/navigation-getting-started">this tutorial</a>
 * Crashes randomly with a {@link ClassNotFoundException(NavHostFragment)}, here :
 * <code>
 *     <androidx.fragment.app.FragmentContainerView
 *         android:id="@+id/nav_host_fragment_container_view"
 *         android:name="androidx.navigation.fragment.NavHostFragment"
 * </code>
 * <p>
 * Even without minify nor Proguard enabled.
 * Creating a custom class here fix this error.
 */
@Keep
public class CustomNavHostFragment extends NavHostFragment {

}

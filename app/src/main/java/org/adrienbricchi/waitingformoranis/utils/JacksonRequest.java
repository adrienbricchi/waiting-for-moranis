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
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;

import static com.android.volley.Request.Method.GET;


@Keep
public class JacksonRequest<T> extends Request<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<T> typeRef;
    private final Response.Listener<T> listener;


    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     */
    public JacksonRequest(String url, TypeReference<T> typeRef, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(GET, url, errorListener);
        this.typeRef = typeRef;
        this.listener = listener;
    }


    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers)
            );
            return Response.success(
                    objectMapper.readValue(json, typeRef),
                    // Temp fix of this warn : Unable to parse dateStr: 2020-02-26 04:37:00 +0000
                    // This has to be fixed on the TMDB server.

                    // HttpHeaderParser.parseCacheHeaders(response)
                    null
            );
        }
        catch (UnsupportedEncodingException | JsonProcessingException e) {
            return Response.error(new ParseError(e));
        }
    }

}

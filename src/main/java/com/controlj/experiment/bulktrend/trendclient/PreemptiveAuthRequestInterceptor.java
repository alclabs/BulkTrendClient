/*
 * Copyright 2010 Automated Logic
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.controlj.experiment.bulktrend.trendclient;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class PreemptiveAuthRequestInterceptor implements HttpRequestInterceptor {
    public void process(
            final HttpRequest request,
            final HttpContext context) throws HttpException, IOException {

        AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);

        // If no auth scheme avaialble yet, try to initialize it preemptively
        if (authState.getAuthScheme() == null) {
            AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            if (authScheme != null) {
                Credentials creds = credsProvider.getCredentials(
                        new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                if (creds == null) {
                    throw new HttpException("No credentials for preemptive authentication");
                }
                authState.setAuthScheme(authScheme);
                authState.setCredentials(creds);
            }
        }
    }
}

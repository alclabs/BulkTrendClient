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

import com.controlj.experiment.bulktrend.trendclient.handler.TrendResultHandler;
import com.controlj.experiment.bulktrend.trendclient.parser.ResponseParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrendClient {
    private Collection<String> ids;
    private String user;
    private String password;
    private String url;
    private Calendar start;
    private Calendar end;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private TrendResultHandler handler;
    private ResponseParser parser;
    private boolean zip = true;
    private InputStream altInput = null;
    private int defaultDigits = 2;
    

    TrendClient(String url, Collection<String> ids, String user, String password, String handlerClass, String responseClass) {
        this.ids = ids;
        this.user = user;
        this.password = password;
        this.url = url;

        handler = loadClass(handlerClass, TrendResultHandler.class);
        parser = loadClass(responseClass, ResponseParser.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadClass(String className, Class<T> interfaceClass) {
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);

            if (!interfaceClass.isAssignableFrom(clazz)) {
                System.err.println("Error, "+className + " does not implement "+ interfaceClass.getName());
                System.exit(-1);
            }
            return (T) clazz.newInstance();

        } catch (ClassNotFoundException e) {
            System.err.println("Error, can't find class of "+className);
            System.exit(-1);
        } catch (InstantiationException e) {
            System.err.println("Error, can't instatiate class of "+className);
            System.exit(-1);
        } catch (IllegalAccessException e) {
            System.err.println("Error, can't access class of "+className);
            System.exit(-1);
        }
        return null;
    }

    public void setStart(Date start) {
        this.start = new GregorianCalendar();
        this.start.setTime(start);
    }

    public void setEnd(Date end) {
        this.end = new GregorianCalendar();
        this.end.setTime(end);
    }

    public void setZip(boolean enable) {
        zip = enable;
    }

    public void setDefaultDigits(int digits) {
        defaultDigits = digits;
    }

    public void setAlternateInput(InputStream alt) {
        altInput = alt;
    }

    public Calendar getStart() {
        Calendar result = start;
        if (result == null) {
            result = getYesterday();
        }
        return result;
    }

    public Calendar getEnd() {
        Calendar result = end;
        if (result == null) {
            result = getYesterday();
        }
        return result;
    }

    public static Calendar getYesterday() {
        Calendar today = new GregorianCalendar();
        today.add(Calendar.DATE, -1);
        Calendar result = new GregorianCalendar(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        return result;
    }



    public void go() {
        DefaultHttpClient client = null;
        try {
            prepareForResponse();

            if (altInput == null) {
                client = new DefaultHttpClient();

                // Set up preemptive Basic Authentication
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, password);
                client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);

                BasicHttpContext localcontext = new BasicHttpContext();
                BasicScheme basicAuth = new BasicScheme();
                localcontext.setAttribute("preemptive-auth", basicAuth);
                client.addRequestInterceptor(new PreemptiveAuthRequestInterceptor(), 0);

                if (zip) {
                    client.addRequestInterceptor(new GZipRequestInterceptor());
                    client.addResponseInterceptor(new GZipResponseInterceptor());
                }



                HttpPost post = new HttpPost(url);

                try {
                    setPostData(post);
                    HttpResponse response = client.execute(post, localcontext);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        System.err.println("Error: Web Service response code of: "+ response.getStatusLine().getStatusCode());
                        return;
                    }
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        parser.parseResponse(ids.size(), entity.getContent());
                    }

                } catch (IOException e) {
                    System.err.println("IO Error reading response");
                    e.printStackTrace();
                }
            } else {  // Alternate input (typically from a file) for testing
                try {
                    parser.parseResponse(ids.size(), altInput);
                } catch (IOException e) {
                    System.err.println("IO Error reading response");
                    e.printStackTrace();
                }
            }
        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
        }
        /*
        try {
            parser.parseResponse(ids.size(), new FileInputStream(new File("response.dump")));
        } catch (IOException e) {
            e.printStackTrace(); 
        }
        */

    }

    private void prepareForResponse() {
        if (parser == null) {
            throw new IllegalStateException("Parser not set");
        }

        if (handler == null) {
            throw new IllegalStateException("Handler not set");
        }

        parser.setHandler(handler);
    }

    private void setPostData(HttpPost post) throws UnsupportedEncodingException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add( new BasicNameValuePair("start", dateFormatter.format(getStart().getTime())) );
        params.add( new BasicNameValuePair("end", dateFormatter.format(getEnd().getTime())) );
        params.add( new BasicNameValuePair("format", parser.getId()));
        params.add( new BasicNameValuePair("defaultdigits", Integer.toString(defaultDigits)));

        for (String id : ids) {
            params.add( new BasicNameValuePair("id", id) );
        }

        post.setEntity(new UrlEncodedFormEntity(params));
    }


}

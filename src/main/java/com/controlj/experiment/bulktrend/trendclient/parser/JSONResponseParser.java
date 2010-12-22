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

package com.controlj.experiment.bulktrend.trendclient.parser;

import com.controlj.experiment.bulktrend.trendclient.handler.TrendResultHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class JSONResponseParser extends BaseResponseParser implements ResponseParser {
    private TrendResultHandler handler;

    public void setHandler(TrendResultHandler handler) {
         this.handler = handler;
    }

    public String getId() {
        return "json";
    }

    public void parseResponse(int numIds, InputStream response) throws IOException {
        JSONTokener tok = new JSONTokener(new InputStreamReader(response));
        try {
            tok.next('[');

            for (int i=0; i<numIds; i++) {
                JSONObject nextSource = new JSONObject(tok);
                handler.source(nextSource.getString("id"));
                
                JSONArray data = nextSource.getJSONArray("s");
                int len = data.length();
                for (int j=0; j<len; j++) {
                    JSONObject sample = data.getJSONObject(j);
                    Date time = parseDate(sample.getString("t"));

                    if (sample.has("a")) {
                        handler.analogSample(time, parseAnalog(sample.getString("a")));
                    } else if (sample.has("d")) {
                        handler.digitalSample(time, "true".equals(sample.getString("d")));
                    }
                }

                char nextChar = tok.next();
                if (nextChar == ',') { } // just keep going
                else if (nextChar == ']' || nextChar==0) {
                    break;  // we're done
                }
            }

        } catch (JSONException e) {
            System.err.println("Error parsing results!");
            e.printStackTrace();
        }
        handler.close();

        //printResponse(response);
    }
}

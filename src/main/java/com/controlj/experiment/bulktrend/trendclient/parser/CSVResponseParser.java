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

import au.com.bytecode.opencsv.CSVReader;
import com.controlj.experiment.bulktrend.trendclient.handler.TrendResultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class CSVResponseParser extends BaseResponseParser implements ResponseParser {

    public String getId() {
        return "csv";
    }

    public void parseResponse(int numIds, InputStream response) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(response));
        try {
            String[] nextLine;


            while ((nextLine = reader.readNext()) != null){
                int len = nextLine.length;
                if (len > 0) {
                    String nextSource = nextLine[0];
                    handler.source(nextSource);
                    
                    for (int j=1; j+1<len; j+=2) {
                        Date time = parseDate(nextLine[j]);
                        String valueString = nextLine[j+1];

                        if (isDigital(valueString)) {
                            handler.digitalSample(time, "true".equals(valueString));
                        } else {
                            handler.analogSample(time, parseAnalog(valueString));
                        }
                    }
                } else {
                    // ignore line, no data here
                }
            }

        } finally {
            handler.close();
        }

        //printResponse(response);
    }

}
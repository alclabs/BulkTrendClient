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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class FileDumpParser implements ResponseParser {
    public void setHandler(TrendResultHandler handler) {
        // ignore - not used
    }

    public String getId() {
        return "csv";
    }

    public void parseResponse(int numSources, InputStream in) throws IOException {
        DecimalFormat format = new DecimalFormat("0");

        File dump = new File("response.dump");
        FileOutputStream out = new FileOutputStream(dump);
        byte buffer[] = new byte[1000];
        int bytesRead;
        long total = 0;
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
            total += bytesRead;
        }
        out.close();

        System.out.println("Wrote a total of "+ format.format(total) +" bytes");
    }
}

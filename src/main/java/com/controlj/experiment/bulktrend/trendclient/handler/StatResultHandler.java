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

package com.controlj.experiment.bulktrend.trendclient.handler;

import java.text.NumberFormat;
import java.util.Date;

public class StatResultHandler implements TrendResultHandler {
    long sources, analog, digital;
    long created;
    double analogAvg;
    NumberFormat numFormat;

    public StatResultHandler() {
        created = System.currentTimeMillis();
        numFormat = NumberFormat.getNumberInstance();
        numFormat.setMinimumFractionDigits(2);
    }

    public void source(String sourceID) {
        sources++;
    }

    public void analogSample(Date date, double value) {
        analog++;
        analogAvg += value;
    }

    public void digitalSample(Date date, boolean value) {
        digital++;
    }

    public void close() {
        long end = System.currentTimeMillis();

        long totalSamples = analog + digital;
        double totalSeconds = (end - created) / 1000.0;
        System.out.println("Retrieved a total of " + totalSamples + " samples "+
                "from " + sources + " sources "+
                "in a total of "+
        numFormat.format(totalSeconds) + " seconds");
        System.out.println("Thats " + numFormat.format( totalSamples / totalSeconds) +" samples / sec");
        System.out.println("  Analog : " + analog);
        System.out.println("  Digital: " + digital);
        System.out.println("  Analog Average: "+ (analogAvg / analog));
    }
}

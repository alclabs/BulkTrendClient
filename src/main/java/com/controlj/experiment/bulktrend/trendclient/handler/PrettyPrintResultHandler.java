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

import com.controlj.experiment.bulktrend.trendclient.handler.TrendResultHandler;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrettyPrintResultHandler implements TrendResultHandler {
    NumberFormat analogFormat;
    DateFormat dateFormat;
    PrintStream out;

    public PrettyPrintResultHandler() {
        analogFormat = NumberFormat.getNumberInstance();
        analogFormat.setMaximumFractionDigits(3);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        out = System.out;
    }

    public void setOutput(PrintStream out) {
        this.out = out;
    }

    public void source(String sourceID) {
        System.out.println(sourceID);
    }

    public void analogSample(Date date, double value) {
        out.println("    "+dateFormat.format(date)+"  --  " + analogFormat.format(value));
    }

    public void digitalSample(Date date, boolean value) {
        out.println("    "+dateFormat.format(date)+"  --  " + value);
    }

    public void close() {        
    }
}

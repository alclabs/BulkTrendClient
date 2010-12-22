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

import java.io.*;
import java.util.ArrayList;

public class IdFile {
    private File idFile;

    public IdFile(File file) {
       idFile = file;
    }

    ArrayList<String> getIds() throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader rdr = null;
        try {
            rdr = new BufferedReader(new FileReader(idFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Error, file '"+idFile+"' not found");
        }

        String nextId;
        while ((nextId = rdr.readLine()) != null) {
            result.add(nextId);
        }
        return result;
    }

}

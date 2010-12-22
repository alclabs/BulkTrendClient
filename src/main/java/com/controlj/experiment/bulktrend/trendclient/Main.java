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

import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.text.*;
import java.util.*;

/*
  USAGE:
     properties file called "trendclient.properties" uses keys:
        user          - user name
        passsword     - password
        server        - URL to server
        defaultdigits - default number of digits to the right of the decimal for analog values
        handler       - fully qualified name of handler
                        (either "com.controlj.experiment.bulktrend.trendclient.handler.StatResultHandler"
                         or "com.controlj.experiment.bulktrend.trendclient.handler.PrettyPrintResultHandler")
        parser        - fully qualified name of parser
                        (either "com.controlj.experiment.bulktrend.trendclient.parser.JSONResponseParser"
                         or "com.controlj.experiment.bulktrend.trendclient.parser.CSVResponseParser")

     ID file called "trendclient.sources" has trend ID on each line.
 */

public class Main {
    private static final String PARAM_TESTFILE =    "testfile";
    private static final String PARAM_DIR =         "dir";
    private static final String PARAM_START =       "start";
    private static final String PARAM_END =         "end";
    private static final String PARAM_NOZIP =       "nozip";

    private static final String PROP_SERVER =       "server";
    private static final String PROP_USER =         "user";
    private static final String PROP_PASSWORD =     "password";
    private static final String PROP_HANDLER =      "handler";
    private static final String PROP_PARSER =       "parser";
    private static final String PROP_DIGITS =       "defaultdigits";


    public static void main(String args[]) {

        Options options = setupCLOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Command line parsing failed: "+ e.getMessage());
            System.exit(-1);
        }

        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("trendclient", options);
            printConfigHelp();
            System.exit(0);
        }

        // dir option - read config files
        File baseDir = new File("");
        if (line.hasOption(PARAM_DIR)) {
            baseDir = new File(line.getOptionValue(PARAM_DIR));
        }
        Properties props = getProperties(baseDir);

        String server = getProperty(props, PROP_SERVER);
        String parserName = getProperty(props, PROP_PARSER);
        String handlerName = getProperty(props, PROP_HANDLER);
        String user = getProperty(props, PROP_USER);
        String pw = getProperty(props, PROP_PASSWORD);

        TrendClient tc = new TrendClient(server,
                getIDs(baseDir),
                user,
                pw,
                handlerName,
                parserName);

        String defaultDigitsString = props.getProperty(PROP_DIGITS);
        if (defaultDigitsString != null) {
            try {
                tc.setDefaultDigits(Integer.parseInt(defaultDigitsString));
            } catch (NumberFormatException e) {
                System.err.println("Invalid valid for property "+PROP_DIGITS+":"+defaultDigitsString);
            }
        }

        //testfile
        if (line.hasOption(PARAM_TESTFILE)) {
            String fileName = line.getOptionValue(PARAM_TESTFILE);
            if (fileName == null) {
                fileName = "response.dump";
            }
            try {
                tc.setAlternateInput(new FileInputStream(new File(fileName)));
                System.out.println("Reading trends from file: "+ fileName);
            } catch (FileNotFoundException e) {
                System.err.println("Error, " + PARAM_TESTFILE + " '" +fileName+"' not found");
            }
        }
        else {
            System.out.println("Reading trends from "+server);
            System.out.println("Parser="+parserName);
            System.out.println("Handler="+handlerName);
        }


        // Start/End
        Date start = parseDateOption(PARAM_START, line);
        Date end = parseDateOption(PARAM_END, line);
        if (start == null) {
            start = TrendClient.getYesterday().getTime();
        }
        if (end == null) {
            end = TrendClient.getYesterday().getTime();
        }
        tc.setStart(start);
        tc.setEnd(end);
        System.out.println("From "+start+" to "+ end);


        // nozip
        if (line.hasOption(PARAM_NOZIP)) {
            tc.setZip(false);            
        }


        tc.go();
    }


    private static ArrayList<String> getIDs(File baseDir) {
        ArrayList<String> ids = new ArrayList<String>();
        File file = new File(baseDir, "trendclient.sources");
        if (!file.exists()) {
            System.err.println("Error, file '" + file + "' missing.");
            System.exit(-1);
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String nextLine = null;

            while ((nextLine = in.readLine()) != null) {
                if (nextLine.length() > 0) {
                    ids.add(nextLine);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading id file: "+ file);
            e.printStackTrace();
            System.exit(-1);
        }

        return ids;
    }

    private static Properties getProperties(File baseDir) {
        Properties results = new Properties();
        File file = new File(baseDir, "trendclient.properties");
        if (!file.exists()) {
            System.err.println("Error, property file '" + file + "' missing.");
            System.exit(-1);
        }
        try {
            results.load(new FileInputStream(file));

        } catch (IOException e) {
            System.err.println("Error reading properties file: "+ file);
            e.printStackTrace();
            System.exit(-1);
        }

        return results;
    }


    private static Options setupCLOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder.withArgName("directory")
                .hasArg()
                .withDescription("Directory for trendclient.properties and trendclient.sources")
                .create(PARAM_DIR));

        options.addOption(OptionBuilder.withArgName("startDate")
                .hasArg()
                .withDescription("Starting date to retrieve trends (mm/dd/yyyy). " +
                        "Defaults to yesterday.")
                .create(PARAM_START));

        options.addOption(OptionBuilder.withArgName("endDate")
                .hasArg()
                .withDescription("Ending date to retrieve trends (mm/dd/yyyy). " +
                        "Defaults to yesterday.")
                .create(PARAM_END));

        options.addOption(OptionBuilder.create("help"));

        options.addOption(OptionBuilder.withDescription("Disable zip compression")
                .create(PARAM_NOZIP));

        options.addOption(OptionBuilder.withArgName("file")
                .hasOptionalArg()
                .withDescription("Read data from file instead of over HTTP")
                .create(PARAM_TESTFILE)
                );
        

        return options;
    }

    private static String getProperty(Properties props, String propName) {
        String propVal = props.getProperty(propName);
        if (propVal == null) {
            System.out.println("Property '"+propName+"' missing from trendclient.properties.");
            System.out.println();
            printConfigHelp();
            System.exit(-1);
        }
        return propVal;
    }

    private static void printConfigHelp() {
        System.out.println("Two configuration files are expected:");
        System.out.println("trendclient.sources:");
        System.out.println("   a list of trend source ids - one per line");
        System.out.println("trendclient.properties contains:");
        System.out.println("    user          - user name");
        System.out.println("    passsword     - password");
        System.out.println("    server        - URL to server");
        System.out.println("    defaultdigits - default number of digits to the right of the decimal for analog values");
        System.out.println("    handler       - fully qualified name of handler");
        System.out.println("                    (either \"com.controlj.experiment.bulktrend.trendclient.handler.StatResultHandler\"");
        System.out.println("                     or \"com.controlj.experiment.bulktrend.trendclient.handler.PrettyPrintResultHandler\")");
        System.out.println("    parser        - fully qualified name of parser");
        System.out.println("                    (either \"com.controlj.experiment.bulktrend.trendclient.parser.JSONResponseParser\"");
        System.out.println("                     or \"com.controlj.experiment.bulktrend.trendclient.parser.CSVResponseParser\")");
    }

    private static Date parseDateOption(String optionName, CommandLine cl) {
        Date result = null;
        if (cl.hasOption(optionName)) {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String dateString = cl.getOptionValue(optionName);
            try {
                result = formatter.parse(dateString);
            } catch (java.text.ParseException e) {
                System.err.println("Error: invalid format for date: '"+dateString+"'.  " +
                        "Should be in form MM/DD/YYYY.");
                System.exit(-1);
            }
        }
        return result;
    }

}

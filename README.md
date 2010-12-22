Purpose
=======

BulkTrendClient is a sample java client for the BulkTrendServer WebCTRL Add-On. It is primarily intended for reference or as a starting point for a useful java client. If you want to do something more useful with the trend samples, you may implement Please see the documentation in the BulkTrendServer add-on for more details on the BulkTrendServer web service.

Building
========
If you have the full source distribution, BulkTrendClient can be built using the [Gradle](http://gradle.org) build tool using the supplied gradlew or gradlew.bat scripts.

You must have a Java JDK in your path to build BulkTrendClient. Run the gradlew script with no parameters to build an install image. For Mac/Linux this would be "./gradlew", for Windows just "gradlew". This will build an install image in the build/distributions/exploded subdirectory. Go to that directory to execute the BulkTrendClient.

Usage
=====
The BulkTrendClient application uses a somewhat awkward mix of configuration files and command line parameters.

Configuration Files
-------------------
By default, BulkTrendClient expects the two configuration files (trendclient.properties and trenclient.sources) to be in the current working directory, but you can use the -dir command line option to specify an alternate location.

### trendclient.sources
Contains a trend id for each trend source on each line of the file. You can get trend ids using the search page of the trendserver add-on.

### trendclient.properties
This java style properties file (formatted like property=value) supports the following properties:

* `user` -- User name. Note that the specified user must have the functional privilege in WebCTRL of "Remote Data Access - SOAP".
* `password`
* `server` -- URL to server
* `defaultdigits` -- default number of digits to the right of the decimal for analog values
* `handler` -- fully qualified name of handler class:
  * `com.controlj.experiment.bulktrend.trendclient.handler.StatResultHandler`  
    Counts the number of analog and digital trend sources, samples retrieved, and total timing.
  * `com.controlj.experiment.bulktrend.trendclient.handler.PrettyPrintResultHandler`  
    Prints the retrieved trend samples
  * Make your own...  
    That's the point of a sample client.
* `parser` -- fully qualified name of parser class:
  * `com.controlj.experiment.bulktrend.trendclient.parser.JSONResponseParser`  
    Transfers results using [JSON](http://www.json.org). This is convenient if communicating directly to a web browser, 
    but has more overhead than the CSV format.
  * `com.controlj.experiment.bulktrend.trendclient.parser.CSVResponseParser`  
    The lightest weight string format - just comma separated values.

Command Line Parameters
-----------------------
* `-dir <directory>` -- Directory for trendclient.properties and trendclient.sources
* `-start <startDate>` -- Starting date to retrieve trends (mm/dd/yyyy).  
  Defaults to yesterday. Trends will be retrieved starting at midnight (0'th hour) 
  on the specified day.
* `-end <endDate>` -- Ending date to retrieve trends (mm/dd/yyyy).  
  Defaults to yesterday. Trends will be retrieved up to, but not including, midnight of the evening of the day specified.
  Specifying the same date for start and end will retrieve all the trends for that day.
* `-help` -- Displays help
* `-nozip` -- Disable zip compression
* `-testfile <file>` -- Read data from file instead of over HTTP


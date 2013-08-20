oow13-demo - UberXkcd
=================

UberXkcd is a simple REST API for querying JSON objects containing
information (including images) for XKCD comics.  This is an unsecured API
for demonstration purposes only.

Many thanks to Randall Munroe for his very entertaining comic and for making
comic metadata available in JSON format.  Visit http://xkcd.com to waste many
hours of your time if you have not done so already.

So as to limit the bandwidth of the author's that we consume, this tool
caches the data locally.

As security is the focus of this demo and this app is just a simple unsecured
payload, I will not be going into depth on the application but will provide a
brief overview of it.

Note that while most of the code follows good practices, some shortcuts were 
taken for this application (such as hard-coded configuration).  

1) This application is written using Java 7 and the code uses Java 7 specific 
features, such as try-with-resources.

2) The JAX-RS standard is used to implement the web service.  Specifically 
Apache CXF is used as the JAX-RS implementation.

JAX-RS allows us to annotate a plain object with information about how to 
expose methods as a REST API.  As a result, you can just code a normal object
and annotate it and presto - it's exposable as a web service...

3) The embedded Jetty web server is used to host the HTTP services for Apache
CXF, so no external web server is required.

4) Jackson is used for JSON data manipulation.

5) The embedded HTTP client in Java is used to make queries of the XKCD data.

6) Maven is used as the build system for this project.  The OneJar plugin is
used to combine all our dependencies into a single JAR to make for simple
deployment of the application.  

Resource URLs:

/ - Implemented to tell people they went to the wrong place
/favicon.ico - Implemented to avoid 404 logs from browsers pestering us for icons
/uberxkcd/hwm - Returns JSON of the id of the most recent XKCD
/uberxkcd/id/xxxx - Returns the JSON for a specific XKCD
/uberxkcd/random - Returns a random XKCD
/userxkcd/pullAll - Loads our cache with all missing XKCDs
/uberxkcd/shutdown - Shuts down the server

Security which needs to be provided for:

Access to unauthenticated users - Nothing for this demo
Access to all authenticated users - /uberxkcd/random
Access to admin users - All of the above

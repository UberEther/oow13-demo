package com.uberether.oow.uberxkcd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.DatatypeConverter;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
/**
 * UberXKCD - Simple Rest API for serving up XKCD images
 * Used by the 2013 OOW presentation of Matt Topper's as an unsecured API
 * 
 * The XKCD json is read and then the image mime type and data (as b64) are added
 * The loaded data is saved to disk at /tmp/uberxkcd/ and the cached copy is used
 * whenever it is available.
 * 
 * Random and pullAll ignore episode 404 as it does not exist
 * 
 * Annotated as a JAX-RS Service with endpoints:
 *      /uberxkcd/hwm - Returns the HWM currently loaded
 *      /uberxkcd/id/{id} - Returns the JSON for the specified episode
 *      /uberxkcd/random - Returns a random episode
 *      /userxkcd/pullAll - Pulls all un-cached episodes down (1 at a time) and 
 *                          returns the HWM when done (may take a while!)
 * 
 * Reminder - JAX-RS supports a mode where a separate instance of the service
 * is instantiated for each call, so to support this mode cleanly, this service
 * stores all reusable objects as statics
 * 
 * @author msamblanet
 */
public class UberXkcdService {
    // Sharable objects
    private final static Random rand = new Random();
    private final static ObjectMapper jsonMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String,Object>> TYPEREF_MAP = new TypeReference<HashMap<String,Object>>() {};

    // Make these static so we do not keep reloading them...
    private static int hwm = -1;
    private static long hwmExpiration = 0;
    
    // In real code, these should be configurable
    // Watch out in JAX-RS may creates a new service object for each request!
    private final static String currentUrl = "http://xkcd.com/info.0.json";
    private final static String episodeUri = "http://xkcd.com/${id}/info.0.json";
    private final static File cacheDir = new File("/tmp/uberxkcd/");
    
    private static boolean shutdown = false;
    
    // Synchronization object
    private final static Object lockObject = new Object();
    
    static {
        // Not best pratice to hard code and do this statically...but it provides
        // for a simple setup for this demo app
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new RuntimeException("Unable to create cache directory");
            }
        }
        if (!(cacheDir.canRead() && cacheDir.canWrite())) {
            throw new RuntimeException("Failed to initialize - Can't read and write cache dir: "+cacheDir);
        }
    }

    /**
     * Requests to shutdown the server - the shutdown itself will be asynchronous to this call
     * @return A constant string so the user is not left hanging...
     */
    @GET
    @Path("/uberxkcd/shutdown") 
    @Produces("text/plain")
    public String shutdown() {
        synchronized (lockObject) {
            shutdown = true;
            lockObject.notifyAll();
        }
        return("Server Shutting Down...");
    }

    /**
     * Handles requests for the root of the web server will some "instructional"
     * text
     * @return "Instructional text" 
     */
    @GET
    @Path("/") 
    @Produces("text/plain")
    public String getRoot() {
        return("Use the right URL silly...");
    }
    
    /**
     * Handles annoying requests for favicons from browsers
     * @return Some text telling the browser that we don't have a favicon
     */
    @GET
    @Path("/favicon.ico") 
    @Produces("text/plain")
    public String getFavIcon() {
        return("No favicon here...");
    }
    
    /**
     * Looks up the high-water-mark (highest available id)
     * @return The highest available record id
     * @throws IOException 
     */
    @GET
    @Path("/uberxkcd/hwm")
    @Produces("application/json")
    public int getHwm() throws IOException {
        if (hwmExpiration < System.currentTimeMillis()) {
            // Only reload if we expired
            if (hwmExpiration == 0) {
                // First time - check for the highest cached value we have in the cache and
                // use for up to 6 hours
                for (File f : cacheDir.listFiles()) {
                    String name = f.getName();
                    if (name.endsWith(".json")) {
                        int t = Integer.parseInt(name.substring(0, name.length() - 5));
                        if (t > hwm) {
                            hwm = t;
                        }
                    }
                }
                if (hwm > 0) {
                    hwmExpiration = System.currentTimeMillis() + 6L * 60L * 60L * 1000L;
                }
            }
            
            // Load the most recent from the website and use for up to 24 hours
            if (hwm < 0 || hwmExpiration != 0) {
                if (System.getProperty("xkcdOffline") == null) {
                    Map<String, Object> ep = loadEpisode(-1);
                    hwm = ((Number) ep.get("num")).intValue();
                }
                hwmExpiration = System.currentTimeMillis() + 24L * 60L * 60L * 1000L;
            }
        }
        return hwm;
    }
    
    /**
     * Loads a specific episode
     * @param id ID of episode to load
     * @return JSON describing the episode including a base64 encoded image
     * @throws IOException 
     */
    @GET
    @Path("/uberxkcd/id/{id}")
    @Produces("application/json")
    public Map<String, Object> getEpisode(@PathParam("id") int id) throws IOException {
        return loadEpisode(id);
    }
    
    /**
     * Loads a random episode (ignoring 404)
     * @return JSON describing the episode including a base64 encoded image
     * @throws IOException 
     */
    @GET
    @Path("/uberxkcd/random")
    @Produces("application/json")
    public Map<String, Object> getRandomEpisode() throws IOException {
        int id;
        do {
            id = rand.nextInt(getHwm()) + 1;
        } while (id == 404); // 404 does not exist...skip it

        return loadEpisode(id);
    }
 
    /**
     * Loads all episodes into the cache
     * @return High water mark
     * @throws IOException 
     */
    @GET
    @Path("/uberxkcd/pullAll")
    @Produces("application/json")
    public int pullAllEpisodes() throws IOException {
        int hwmForOperation = getHwm();
        for (int i = hwmForOperation; i > 0; i--) {
            if (i == 404) {
                // 404 does not exist...skip it
                continue;
            }
            loadEpisode(i);
        }
        return hwmForOperation;
    }
    
    /**
     * Loads a specific episode.  If ID is 0, it loads the current episode
     * 
     * If the id is not 0, the record is loaded from the cache.  If not found,
     * it is read from the author's website, modified to include the image,
     * and then injected into the cache
     * 
     * @param id ID of episode to load
     * @return JSON describing the episode including a base64 encoded image
     * @throws IOException 
     */
    private Map<String, Object> loadEpisode(int id) throws IOException {
        // Check file cache
        if (id >= 0) {
            File cacheFile = new File(cacheDir, ""+id+".json");
            if (cacheFile.canRead()) {
                try (Reader r = new BufferedReader(new FileReader(cacheFile))) {
                    return jsonMapper.readValue(r, TYPEREF_MAP);
                }
            }
        }
        
        if (System.getProperty("xkcdOffline") != null) {
            throw new IOException("Cannot load episode "+id+" because the app was started in offline mode");
        }
        
        // Load JSON
        Map<String, Object> rv;
        URL url = new URL((id < 0) ? currentUrl : episodeUri.replaceAll("\\$\\{id\\}", ""+id));
        URLConnection c = url.openConnection();
        try (InputStream in = c.getInputStream()) {
            rv = jsonMapper.readValue(in, TYPEREF_MAP);
        }
        
        // Get image URL out of object and load it
        url = new URL(rv.get("img").toString());
        c = url.openConnection();
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int nRead;
            while ((nRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, nRead);
            }
            
            // Build a sub-object with all the image data
            Map m2 = new LinkedHashMap();
            m2.put("size",Integer.valueOf(out.size()));
            m2.put("mime",c.getHeaderField("Content-Type"));
            m2.put("src",url.toString());
            m2.put("b64", DatatypeConverter.printBase64Binary(out.toByteArray()));

            rv.put("img", m2);
        }
        
        // Write to cache
        if (id < 0) {
            id = ((Number) rv.get("num")).intValue();
        }
        File cacheFile = new File(cacheDir, ""+id+".json");
        try (Writer w = new BufferedWriter(new FileWriter(cacheFile))) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(w, rv);
        }
        
        return rv;
    }
    
    /**
     * Main method to start an embedded server running the app.  Shutsdown
     * when somebody hits the shutdown API
     * 
     * Default is to listen at http://localhost:9000 but an alternate address
     * can be specified as the only command line argument
     * 
     * @param args Command line arguments
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(UberXkcdService.class);
        sf.setResourceProvider(UberXkcdService.class,
                new SingletonResourceProvider(new UberXkcdService()));        
        if (args == null || args.length == 0) {
            sf.setAddress("http://localhost:9000/");
        } else {
            sf.setAddress(args[0]);
        }
        sf.setProvider(new JacksonJsonProvider());
        Server server = sf.create();

        try {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);

            System.out.println("Server ready...waiting for shutdown request...");

            synchronized (lockObject) {
                while (!shutdown) {
                    lockObject.wait();
                }
            }
        } finally {
            System.out.println("Server exiting...");
            server.stop();
            server.destroy();
            System.exit(0);
        }
    }
}

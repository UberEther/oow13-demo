package com.uberether.oow.oag.filteradmin;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

/**
 * UberOagFilterAdmin - Simple Rest API for quering and revoking OAuth2 tokens
 * Used by the 2013 OOW presentation of Matt Topper's to demonstrate token
 * administration
 * 
 * Annotated as a JAX-RS Service with endpoints: /uberoagtokenadmin/shutdown -
 * Shuts down the server /uberoagtokenadmin/tokens - Returns a list of all
 * tokens /uberoagtokenadmin/revoke/{id} - Revokes the specified token
 * 
 * This app also presents a basic web site (located as /uberoagtokenadmin/site)
 * for viewing and revoking the tokens - it can be accessed at:
 *    /, /uberoagtokenadmin, /uberoagtokenadmin/site, or /useroagtokenadmin/site/index.html
 *
 * Reminder - JAX-RS supports a mode where a separate instance of the service is
 * instantiated for each call, so to support this mode cleanly, this service
 * stores all reusable objects as statics
 *
 * @author msamblanet
 */
public class UberOagFilterAdmin {
    /** Class Logger - SLF4J Logger Object */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(UberOagFilterAdmin.class);
    
    /** Flag indicating if the server was shutdown */
    private static boolean shutdown = false;
    /** Synchronization object */
    private final static Object lockObject = new Object();

    /**
     * Handles annoying requests for favicons from browsers
     *
     * @return Some text telling the browser that we don't have a favicon
     */
    @GET
    @Path("/favicon.ico")
    @Produces("text/plain")
    public String getFavIcon() {
        return ("No favicon here...");
    }

    /**
     * Requests to shutdown the server - the shutdown itself will be
     * asynchronous to this call
     *
     * @return A constant string so the user is not left hanging...
     */
    @GET
    @Path("/uberoagtokenadmin/shutdown")
    @Produces("text/plain")
    public String shutdown() {
        synchronized (lockObject) {
            shutdown = true;
            lockObject.notifyAll();
        }
        return ("Server Shutting Down...");
    }

    /**
     * Handles requests to the "root" pages by redirecting to the index.html for
     * the site
     *
     * @return "Instructional text"
     */
    @GET
    @Path("/{a:(uberoagtokenadmin(/site)?)?/?}")
    @Produces("text/plain")
    public Response getIndexPage() {
        return Response.status(Response.Status.SEE_OTHER)
                       .header("Location", "/uberoagtokenadmin/site/index.html")
                       .build();
    }
    
    /**
     * Serves files out of the site folder by presenting files from the 
     * JAR.  
     * @param path
     * @return
     * @throws IOException 
     */
    @GET
    @Path("/uberoagtokenadmin/site/{path:.*}")
    public Response getSiteFile(@PathParam("path") String path) throws IOException {
        
        String mimeType = null;
        // If path seems to have illegal characters that may be trying to 
        // then treat it as an unknown mime type
        if (!path.startsWith("/") && !path.startsWith("\\") && !path.contains("..")) {
            // Only process supported file extensions
            if (path.endsWith(".html")) {
                mimeType = "text/html";
            } else if (path.endsWith(".js")) {
                mimeType = "application/javascript";
            } else if (path.endsWith(".css")) {
                mimeType = "text/css";
            } else if (path.endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (path.endsWith(".png")) {
                mimeType = "image/png";
            } 
        }
        
        // Load the data if we know about the type...
        Object data = null;
        if (mimeType != null) {
            try (InputStream in = UberOagFilterAdmin.class.getResourceAsStream(path)) {
                if (in != null) {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int size;
                    while ((size = in.read(buffer)) > 0) {
                        bs.write(buffer, 0, size);
                    }
                    data = bs.toByteArray();
                }
            }
        }
                
        // And build a result
        if (data == null) {
            return Response.ok("Not found: "+path).status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(data, mimeType).build();
        }
    }

    /**
     * Queries all oauth2 tokens in the database
     *
     * @return List of all tokens
     * @throws IOException
     */
    @GET
    @Path("/uberoagtokenadmin/tokens")
    @Produces("application/json")
    public List<Token> getTokens() throws IOException, SQLException {
        try (Connection connection = getDbConnection()) {
            boolean committed = false;
            try (Statement stmt = connection.createStatement()) {
                
                // We do not have a background task deleting our tokens
                // so we will do it before each query to cleanup
                int nDeleted = stmt.executeUpdate("DELETE FROM oauth_access_token WHERE expiry_time <= NOW()");
                if (nDeleted > 0) {
                    log.info("Deleted "+nDeleted+" tokens");
                }
                
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT id, client_id, expiry_time, access_token, "
                        +      "browser, browser_ver, platform, user_auth, "
                        +      "user_name "
                        + "FROM oauth_access_token "
                        +"WHERE expiry_time > NOW()")) {
                    List<Token> rv = new LinkedList();
                    while (rs.next()) {
                        rv.add(new Token(rs));
                    }
                    
                    // Need to commit here because some databases may pull
                    // read locks in some cases...so for best interoperability
                    // all operations should commit or rollback when done
                    connection.commit();
                    
                    return rv;
                }
            } finally {
                if (!committed) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        log.error("Ignoring exception in rollback", ex);
                    }
                }
            }
        }
    }

    /**
     * Revokes a list of tokens
     *
     * @param id ID of episode to load
     * @return JSON describing the episode including a base64 encoded image
     * @throws IOException
     */
    @POST
    @Path("/uberoagtokenadmin/revoke")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public int revokeToken(Set<String> ids) throws IOException, SQLException {
        // @todo It would be better to make htis more configurable...
        try (Connection connection = getDbConnection()) {
            boolean committed = false;
            try (PreparedStatement psQuery = connection.prepareStatement(
                    "SELECT access_token FROM oauth_access_token WHERE id = ?");
                 PreparedStatement psUpdate = connection.prepareStatement(
                    "UPDATE oauth_access_token "
                    +   "SET expiry_time = DATE_SUB(NOW(),INTERVAL 1 SECOND) "
                    + "WHERE id = ?")) {
                for (String id : ids) {
                    psQuery.setString(1, id);
                    try (ResultSet rs = psQuery.executeQuery()) {
                        while (rs.next()) {
                            // Make revoke API call
                            log.info("Revoking Token for ID: "+id);
                            
                            String accessToken = rs.getString(1);

                            // @todo Should be configurable and not hardcoded
                            URLConnection httpConnection = new URL("http://127.0.0.1:8088/api/oauth/revoke").openConnection();
                            httpConnection.setDoOutput(true);
                            httpConnection.setDoInput(true);
                            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                            // @todo Should be configurable and not hardcoded
                            String authHdrValue = "Basic "+
                                    DatatypeConverter.printBase64Binary(
                                        ("SampleConfidentialApp"+
                                         ":"+
                                         "6808d4b6-ef09-4b0d-8f28-3b05da9c48ec").getBytes("UTF-8")
                                    ).replaceAll("[ \\n\\r\\t]+","");
                            
                            httpConnection.setRequestProperty("Authorization", authHdrValue);
                            String query = "token="+ URLEncoder.encode(accessToken, "UTF-8");

                            if (log.isDebugEnabled()) {
                                log.debug("   URL: "+httpConnection.getURL());
                                // Note - you can't use getRequestProperty on Authoriztion - see http://stackoverflow.com/questions/2864062/getrequestpropertyauthorization-always-returns-null
                                log.debug("   Authorization: "+authHdrValue); 
                                log.debug("   Content-Type: "+httpConnection.getRequestProperty("Content-Type"));
                                log.debug("   Query: "+query);
                            }
                            
                            // Make the request
                            try (OutputStream httpOut = httpConnection.getOutputStream()) {                                
                                httpOut.write(query.getBytes("UTF-8"));
                            }

                            // And dump the output
                            try (InputStream response = httpConnection.getInputStream();
                                 ByteArrayOutputStream bs = new ByteArrayOutputStream()) {
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = response.read(buffer)) > 0) {
                                    bs.write(buffer, 0, len);
                                }
                                if (log.isDebugEnabled()) {
                                   log.debug("Response from revoke: "+new String(bs.toByteArray(), "UTF-8"));   
                                }
                            }
                        }
                    }
                    
                    // Update DB
                    log.info("Batching exp in DB: "+id);
                    psUpdate.setString(1, id);
                    psUpdate.addBatch();
                }
                
                log.debug("Executng batch");
                int n = 0;
                for (int i : psUpdate.executeBatch()) {
                    n += i;
                }
                
                log.debug("Committing");
                connection.commit();
                committed = true;
                
                return n;
            } finally {
                if (!committed) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        log.error("Ignoring exception in rollback", ex);
                    }
                }
            }
        }
    }

    /**
     * Helper method to open a database connection
     * 
     * @todo All the information in this token is hardcoded - it really
     *       should be made configurable.  Also, ideally, JNDI or some other
     *       app-server mechanism should be used to centrally manage and pool
     *       connections.
     * 
     * @return A connection to the database
     * @throws SQLException 
     */
    protected Connection getDbConnection() throws SQLException {
        Connection rv =  DriverManager.getConnection(
                "jdbc:mysql://localhost/oauth_db", 
                "root", 
                "password");
        rv.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        rv.setAutoCommit(false);
        return rv;
    }
    
    /**
     * Main method to start an embedded server running the app. Shutsdown when
     * somebody hits the shutdown API
     *
     * Default is to listen at http://localhost:9001 but an alternate address
     * can be specified as the only command line argument
     *
     * @param args Command line arguments
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(UberOagFilterAdmin.class);
        sf.setResourceProvider(UberOagFilterAdmin.class,
                new SingletonResourceProvider(new UberOagFilterAdmin()));
        if (args == null || args.length == 0) {
            sf.setAddress("http://localhost:9001/");
        } else {
            sf.setAddress(args[0]);
        }
        sf.setProvider(new JacksonJsonProvider());
        Server server = sf.create();

        try {
            // Load JDBC Driver for MySQL
            Class.forName("com.mysql.jdbc.Driver");

            Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);
            
            Logger uberLogger = (Logger) LoggerFactory.getLogger("com.uberether");
            uberLogger.setLevel(Level.DEBUG);

            log.info("Server ready...waiting for shutdown request...");

            synchronized (lockObject) {
                while (!shutdown) {
                    lockObject.wait();
                }
            }
        } finally {
            log.info("Server exiting...");
            server.stop();
            server.destroy();
            System.exit(0);
        }
    }
}

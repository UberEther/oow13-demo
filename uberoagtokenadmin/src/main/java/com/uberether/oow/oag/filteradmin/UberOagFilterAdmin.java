package com.uberether.oow.oag.filteradmin;

import java.io.IOException;
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * UberOagFilterAdmin - Simple Rest API for quering and revoking OAuth2 tokens
 * Used by the 2013 OOW presentation of Matt Topper's as an unsecured API
 *
 *
 * Annotated as a JAX-RS Service with endpoints: /uberoagtokenadmin/shutdown -
 * Shuts down the server /uberoagtokenadmin/tokens - Returns a list of all
 * tokens /uberoagtokenadmin/revoke/{id} - Revokes the specified token
 *
 * Reminder - JAX-RS supports a mode where a separate instance of the service is
 * instantiated for each call, so to support this mode cleanly, this service
 * stores all reusable objects as statics
 *
 * @author msamblanet
 */
public class UberOagFilterAdmin {
    // Sharable objects

    private final static ObjectMapper jsonMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Object>> TYPEREF_MAP = new TypeReference<HashMap<String, Object>>() {
    };
    private static boolean shutdown = false;
    // Synchronization object
    private final static Object lockObject = new Object();

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
     * Handles requests for the root of the web server will some "instructional"
     * text
     *
     * @return "Instructional text"
     */
    @GET
    @Path("/")
    @Produces("text/plain")
    public String getRoot() {
        return ("Use the right URL silly...");
    }

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
     * Queries all oauth2 tokens in the database
     *
     * @return List of all tokens
     * @throws IOException
     */
    @GET
    @Path("/uberoagtokenadmin/tokens")
    @Produces("application/json")
    public List<Token> getTokens() throws IOException, SQLException {
        // @todo It would be better to make htis more configurable...
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/oauth_db", "root", "password")) {
            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT id, client_id, expiry_time, access_token, browser, browser_ver, platform, user_auth, user_name FROM oauth_access_token")) {
                    List<Token> rv = new LinkedList();
                    while (rs.next()) {
                        rv.add(new Token(rs));
                    }
                    return rv;
                }
            }
        }
    }

    /**
     * Revokes a single token
     *
     * @param id ID of episode to load
     * @return JSON describing the episode including a base64 encoded image
     * @throws IOException
     */
    @GET
    @Path("/uberoagtokenadmin/revoke/{id}")
    @Produces("application/json")
    public String revokeToken(@PathParam("id") String id) throws IOException, SQLException {
        // @todo It would be better to make htis more configurable...
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/oauth_db", "root", "password")) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE oauth_access_token SET expiry_time = DATE_SUB(CURDATE(),INTERVAL 1 SECOND) WHERE id = ?")) {
                ps.setString(1, id);
                int i = ps.executeUpdate();
                return "Expired "+i+" tokens";
            }
        }
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

package com.uberether.oow.oag.filteradmin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Data object to encapsulate the JSON representation of the OAG token
 * 
 * A read-only field of expiryTimeString is added with the exipry time in
 * "yyyy-MM-dd HH:mm" format.
 *
 * @author msamblanet
 */
@JsonPropertyOrder({"databaseId","clientId","expiryTime","expiryTimeString","browser","browserVer","platform","userAuth","userName"})
public class Token {
    /** 
     * Simple date format for formatting expiryTimeString - DO NOT USE DIRECTLY
     * SimpleDateFormat is NOT THREAD SHAFE - do a .clone() to clone the format
     * and use the clone for your formatting
     */
    private static SimpleDateFormat dateFormatTemplate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private String id;
    private String clientId;
    private Date expiryTime;
    private String browser;
    private String browserVer;
    private String platform;
    private String userAuth;
    private String userName;

    /**
     * Constructs a token with all values set to Java defaults
     */
    public Token() {
    }
    
    /**
     * Constructs a token, reading all the values from the specified result
     * set
     * 
     * @param rs JDBC Result Set with data
     * @throws SQLException 
     */
    public Token(ResultSet rs) throws SQLException {
        id = rs.getString("id");
        clientId = rs.getString("client_id");
        expiryTime = rs.getTimestamp("expiry_time");
        //accessToken = rs.getString("access_token");
        browser = rs.getString("browser");
        browserVer = rs.getString("browser_ver");
        platform = rs.getString("platform");
        userAuth = rs.getString("user_auth");
        userName = rs.getString("user_name");
    }
    
    /**
     * @return The expiry time, formatted into a string
     */
    public String getExpiryTimeString() {
        if (expiryTime == null) {
            return null;
        }
        return ((SimpleDateFormat)dateFormatTemplate.clone()).format(expiryTime);
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the expiryTime
     */
    public Date getExpiryTime() {
        return expiryTime;
    }

    /**
     * @param expiryTime the expiryTime to set
     */
    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }
    
    /**
     * @return the browser
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @param browser the browser to set
     */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * @return the browserVer
     */
    public String getBrowserVer() {
        return browserVer;
    }

    /**
     * @param browserVer the browserVer to set
     */
    public void setBrowserVer(String browserVer) {
        this.browserVer = browserVer;
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @param platform the platform to set
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * @return the userAuth
     */
    public String getUserAuth() {
        return userAuth;
    }

    /**
     * @param userAuth the userAuth to set
     */
    public void setUserAuth(String userAuth) {
        this.userAuth = userAuth;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}

package com.uberether.oow.oag.filters.oauth2;

import com.vordel.circuit.Circuit;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.circuit.oauth.common.OAuth2Utils;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;
import com.vordel.circuit.oauth.token.OAuth2Authentication;
import com.vordel.trace.Trace;

/**
 * Process a request by reading the accesstoken property from the message and 
 * extracting the token from it.  The database id for the token is also 
 * generated so that downstream filter can reference the token in the database.
 * 
 * input:  accesstoken (OAuth2AccessToken)
 * output: uber.oath2.access_token (String) - String form of the access token
 * output: uber.oauth2.id (String) - DB ID for the token
 * 
 * Returns true (success) if accesstoken is present AND of type OAuth2AccessToken
 * Returns false (failure) otherwise
 * 
 * @todo Would be better to make all these variable names configurable
 *
 * @author msamblanet
 */
public class UberOAuth2Processor extends MessageProcessor {
    @Override
    public boolean invoke(Circuit crct, Message msg) throws CircuitAbortException {
        Object token = msg.get("accesstoken");
        if (token != null && token instanceof OAuth2AccessToken) {
            OAuth2AccessToken accessToken = (OAuth2AccessToken) token;

            String tokenString = accessToken.getValue();
            String dbAccessTokenId = OAuth2Utils.digest(tokenString);
            
            msg.put("uber.oauth2.access_token", tokenString);
            msg.put("uber.oauth2.id", dbAccessTokenId);
            msg.put("uber.oauth2.expiryTime", accessToken.getExpiration());

            if (Trace.isDebugEnabled()) {
                Trace.debug("************************************************************");
                Trace.debug("********** Access Token: "+tokenString);
                Trace.debug("********** DB ID: "+dbAccessTokenId);
            }
            
            OAuth2Authentication authN = (OAuth2Authentication) msg.get("accesstoken.authn");
            if (authN != null && authN instanceof OAuth2Authentication) {
                OAuth2Authentication oAuth2Auth = (OAuth2Authentication) authN;
                
                String clientId = oAuth2Auth.getAuthorizationRequest().getClientId();
                String userAuth = oAuth2Auth.getUserAuthentication();
                String userName = oAuth2Auth.getPrincipal().toString();
                
                msg.put("uber.oauth2.clientId", clientId);
                msg.put("uber.oauth2.userAuth", userAuth);
                msg.put("uber.oauth2.userName", userName);
                
                if (Trace.isDebugEnabled()) {
                    Trace.debug("********** Client ID: "+clientId);
                    Trace.debug("********** UserAuth: "+userAuth);
                    Trace.debug("********** UserName: "+userName);
                }
            }

            if (Trace.isDebugEnabled()) {
                Trace.debug("************************************************************");
            }
            
            return true;
        }

        return false;
    }
}

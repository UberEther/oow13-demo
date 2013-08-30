package com.uberether.oow.oag.filters.oauth2;

import com.vordel.circuit.Circuit;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.circuit.oauth.common.OAuth2Utils;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;
import com.vordel.trace.Trace;

/**
 *
 * @author msamblanet
 */
public class UberOAuth2Processor extends MessageProcessor {
    @Override
    public boolean invoke(Circuit crct, Message msg) throws CircuitAbortException {
        OAuth2AccessToken accessToken = (OAuth2AccessToken) msg.get("accesstoken");
        if (accessToken != null) {
            String dbAccessTokenId = OAuth2Utils.digest(accessToken.getValue());
            
            msg.put("uber.oauth2.access_token", accessToken.getValue());
            msg.put("uber.oauth2.id", dbAccessTokenId);

            if (Trace.isDebugEnabled()) {
                Trace.debug("************************************************************");
                Trace.debug("********** Access Token: "+accessToken.getValue());
                Trace.debug("********** DB ID: "+dbAccessTokenId);
                Trace.debug("************************************************************");
            }
            
            return true;
        }

        return false;
    }
}

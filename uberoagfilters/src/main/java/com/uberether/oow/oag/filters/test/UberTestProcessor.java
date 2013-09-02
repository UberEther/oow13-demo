package com.uberether.oow.oag.filters.test;

import com.vordel.circuit.Circuit;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.circuit.oauth.kps.ApplicationDetails;
import com.vordel.circuit.oauth.provider.AuthorizationRequest;
import com.vordel.circuit.oauth.token.OAuth2Authentication;
import com.vordel.mime.Body;
import com.vordel.mime.HeaderSet;
import com.vordel.mime.HeaderSet.Header;
import com.vordel.mime.HeaderSet.HeaderEntry;
import com.vordel.trace.Trace;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Process a request by duping details about the message out to the trace
 * log at error level.  Intended for training purposes and debugging.
 * 
 * Always returns true (success)
 * 
 * @author msamblanet
 */
public class UberTestProcessor extends MessageProcessor {

    @Override
    public boolean invoke(Circuit crct, Message msg) throws CircuitAbortException {
        Trace.error("************** We invoked our test filter !!!!!!!!!!!!!!!");
        for (Map.Entry<String, Object> e : msg.entrySet()) {
            Object val = e.getValue();
            if (val != null && val instanceof HeaderSet) {
                Trace.error("******************* " + e.getKey() + ": Header Set");
                for (Map.Entry<String, HeaderEntry> e2 : ((HeaderSet) val).entrySet()) {
                    for (Header header : e2.getValue()) {
                        Trace.error("************************ " + e2.getKey() + ": " + header);
                    }
                }
            } else if (val != null && val instanceof AuthorizationRequest) {
                Trace.error("******************* " + e.getKey() + ": AuthorizationRequest");
                Trace.error("************************ clientId: " + ((AuthorizationRequest) val).getClientId());
                Trace.error("************************ parameters: " + ((AuthorizationRequest) val).getParameters());
                Trace.error("************************ redirectUri: " + ((AuthorizationRequest) val).getRedirectUri());
                Trace.error("************************ responseTypes: " + ((AuthorizationRequest) val).getResponseTypes());
                Trace.error("************************ scope: " + ((AuthorizationRequest) val).getScope());
                Trace.error("************************ state: " + ((AuthorizationRequest) val).getState());
                Trace.error("************************ isApproved: " + ((AuthorizationRequest) val).isApproved());
                Trace.error("************************ isDenied: " + ((AuthorizationRequest) val).isDenied());

            } else if (val != null && val instanceof OAuth2Authentication) {
                Trace.error("******************* " + e.getKey() + ": OAuth2Authentication");
                Trace.error("************************ userAuth: " + ((OAuth2Authentication) val).getUserAuthentication());
                Trace.error("************************ principal: " + ((OAuth2Authentication) val).getPrincipal());
                Trace.error("************************ isAuthenticated: " + ((OAuth2Authentication) val).isAuthenticated());
                Trace.error("************************ isClientOnly: " + ((OAuth2Authentication) val).isClientOnly());
            } else if (val != null && val instanceof Body) {
                Trace.error("******************* " + e.getKey() + ": Body");
                Trace.error("************************ contentEncoding: " + ((Body) val).getContentEncoding());
                Trace.error("************************ contentId: " + ((Body) val).getContentId());
                Trace.error("************************ contentType: " + ((Body) val).getContentType());
                Trace.error("************************ headers: " + ((Body) val).getHeaders());
                Trace.error("************************ parent: " + ((Body) val).getParent());
                Trace.error("************************ source: " + ((Body) val).getSource());
                Trace.error("************************ contentAvailable: " + ((Body) val).contentAvailable());

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ((Body) val).write(baos, 0);
                    Trace.error("************************ content: " + baos.toString("UTF-8"));
                } catch (IOException ex) {
                    Trace.error("************************ Error writing content: ", ex);
                }
            } else if (val != null && val instanceof ApplicationDetails) {
                Trace.error("******************* " + e.getKey() + ": ApplicationDetails");
                Trace.error("************************ cert: " + ((ApplicationDetails) val).getBase64EncodedCert());
                Trace.error("************************ clientId: " + ((ApplicationDetails) val).getClientID());
                Trace.error("************************ clientSecret: " + ((ApplicationDetails) val).getClientSecret());
                Trace.error("************************ clientType: " + ((ApplicationDetails) val).getClientType());
                Trace.error("************************ contactEmail: " + ((ApplicationDetails) val).getContactEmail());
                Trace.error("************************ contactPhone: " + ((ApplicationDetails) val).getContactPhone());
                Trace.error("************************ contactDescription: " + ((ApplicationDetails) val).getDescription());
                Trace.error("************************ name: " + ((ApplicationDetails) val).getName());
                Trace.error("************************ logo: " + ((ApplicationDetails) val).getLogo());
                Trace.error("************************ redirectURLs: " + ((ApplicationDetails) val).getRedirectURLs());
                Trace.error("************************ scopes: " + ((ApplicationDetails) val).getScopes());
                Trace.error("************************ enabled: " + ((ApplicationDetails) val).isEnabled());
            } else {
                Trace.error("******************* " + e.getKey() + ": " + val);
            }
        }
        return true;
    }
}

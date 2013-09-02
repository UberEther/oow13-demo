package com.uberether.oow.oag.filters.oauth2;

import com.vordel.circuit.DefaultFilter;
import com.vordel.circuit.PropDef;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;

/**
 * OAuth2 filter to extract the clear text access token and the database id for
 * the token into variables for use by downstream filters
 * 
 * See processor class for more details
 *
 * @author msamblanet
 */
public class UberOAuth2Filter extends DefaultFilter {

    @Override
    protected void setDefaultPropertyDefs() {
        super.setDefaultPropertyDefs();
        
        // Define input and output properties
        this.reqProps.add(new PropDef("accesstoken", OAuth2AccessToken.class));
        this.genProps.add(new PropDef("uber.oauth2.access_token", String.class));
        this.genProps.add(new PropDef("uber.oauth2.id", String.class));
    }
    
    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        // Load dynamically because runtime JVM may not have all the UI dependencies
        return Class.forName("com.uberether.oow.oag.filters.oauth2.UberOauth2UI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        // Load dynamically beause the designer JVM may not have all the runtime dependencies
        return Class.forName("com.uberether.oow.oag.filters.oauth2.UberOauth2Processor");
    }
}

package com.uberether.oow.oag.filters.oauth2;

import com.vordel.circuit.DefaultFilter;
import com.vordel.circuit.PropDef;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;

/**
 *
 * @author msamblanet
 */
public class UberOAuth2Filter extends DefaultFilter {

    @Override
    protected void setDefaultPropertyDefs() {
        super.setDefaultPropertyDefs();
        
        this.reqProps.add(new PropDef("accesstoken", OAuth2AccessToken.class));
        this.genProps.add(new PropDef("uber.oauth2.access_token", String.class));
        this.genProps.add(new PropDef("uber.oauth2.id", String.class));
    }
    
    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.oauth2.UberOauth2UI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.oauth2.UberOauth2Processor");
    }
}

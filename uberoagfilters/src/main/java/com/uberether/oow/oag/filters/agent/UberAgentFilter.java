package com.uberether.oow.oag.filters.agent;

import com.vordel.circuit.DefaultFilter;
import com.vordel.circuit.PropDef;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;

/**
 *
 * @author msamblanet
 */
public class UberAgentFilter extends DefaultFilter {

    @Override
    protected void setDefaultPropertyDefs() {
        super.setDefaultPropertyDefs();
        
        this.reqProps.add(new PropDef("uber.userAgent", OAuth2AccessToken.class));
        this.genProps.add(new PropDef("uber.userAgent.browser", String.class));
        this.genProps.add(new PropDef("uber.userAgent.platform", String.class));
    }
    
    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.agent.UberAgentUI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.agent.UberAgentProcessor");
    }
}

package com.uberether.oow.oag.filters.agent;

import com.vordel.circuit.DefaultFilter;
import com.vordel.circuit.PropDef;
import com.vordel.circuit.oauth.token.OAuth2AccessToken;

/**
 * Filter to parse a user-agent string into details about the browser and OS
 * of the client.
 * 
 * See processor class for more details
 *
 * @author msamblanet
 */
public class UberAgentFilter extends DefaultFilter {

    @Override
    protected void setDefaultPropertyDefs() {
        super.setDefaultPropertyDefs();
        
        // Define input and output properties
        this.reqProps.add(new PropDef("uber.userAgent", OAuth2AccessToken.class));
        this.genProps.add(new PropDef("uber.userAgent.browser", String.class));
        this.genProps.add(new PropDef("uber.userAgent.browser.ver", String.class));
        this.genProps.add(new PropDef("uber.userAgent.platform", String.class));
    }
    
    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        // Load dynamically because runtime JVM may not have all the UI dependencies
        return Class.forName("com.uberether.oow.oag.filters.agent.UberAgentUI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        // Load dynamically beause the designer JVM may not have all the runtime dependencies
        return Class.forName("com.uberether.oow.oag.filters.agent.UberAgentProcessor");
    }
}

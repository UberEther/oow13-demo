package com.uberether.oow.oag.filters.test;

import com.vordel.circuit.DefaultFilter;

/**
 * Test OAG filter definition
 * 
 * See processor class for more details
 *
 * @author msamblanet
 */
public class UberTestFilter extends DefaultFilter {

    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        // Load dynamically because runtime JVM may not have all the UI dependencies
        return Class.forName("com.uberether.oow.oag.filters.test.UberTestUI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        // Load dynamically beause the designer JVM may not have all the runtime dependencies
        return Class.forName("com.uberether.oow.oag.filters.test.UberTestProcessor");
    }
}

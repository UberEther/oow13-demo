package com.uberether.oow.oag.filters.test;

import com.vordel.circuit.DefaultFilter;

/**
 *
 * @author msamblanet
 */
public class UberTestFilter extends DefaultFilter {

    @Override
    public Class getConfigPanelClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.test.UberTestUI");
    }

    @Override
    public Class getMessageProcessorClass() throws ClassNotFoundException {
        return Class.forName("com.uberether.oow.oag.filters.test.UberTestProcessor");
    }
}

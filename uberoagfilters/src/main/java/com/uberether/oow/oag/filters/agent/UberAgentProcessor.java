package com.uberether.oow.oag.filters.agent;

import com.vordel.circuit.Circuit;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;

/**
 *
 * @author msamblanet
 */
public class UberAgentProcessor extends MessageProcessor {
    @Override
    public boolean invoke(Circuit crct, Message msg) throws CircuitAbortException {
        String agent = (String) msg.get("uber.userAgent");
        msg.put("uber.userAgent.browser", "UNKNOWN");
        msg.put("uber.userAgent.platform", "UNKNOWN");
        return true;
    }
}

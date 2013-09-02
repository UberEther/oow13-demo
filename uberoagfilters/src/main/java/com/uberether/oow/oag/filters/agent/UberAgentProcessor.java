package com.uberether.oow.oag.filters.agent;

import com.vordel.circuit.Circuit;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.trace.Trace;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Process a request by reading the user-agent value from uber.userAgent and
 * determines the browser and platform details using BitWalker user-agent-utils
 * and then packs the results into the variables:
 *    uber.userAgent.browser
 *    uber.userAgent.browser.ver
 *    uber.userAgent.platform
 * 
 * Always returns true (success)
 * 
 * @todo Would be better to make all these variable names configurable
 *
 * @author msamblanet
 */
public class UberAgentProcessor extends MessageProcessor {
    @Override
    public boolean invoke(Circuit crct, Message msg) throws CircuitAbortException {
        String agentString = (String) msg.get("uber.userAgent");
        
        String browser = "UNKNOWN";
        String browserVer = "UNKNOWN";
        String platform = "UNKNOWN";
        
        if (agentString != null) {
            UserAgent agent = UserAgent.parseUserAgentString(agentString);
            if (agent != null) {
                if (agent.getBrowser() != null) {
                    browser = agent.getBrowser().getName();
                }
                if (agent.getBrowser() != null) {
                    browserVer = agent.getBrowserVersion().getVersion();
                }
                if (agent.getOperatingSystem() != null) {
                    platform = agent.getOperatingSystem().getName();
                }
            }
        }
        
        msg.put("uber.userAgent.browser", browser);
        msg.put("uber.userAgent.browser.ver", browserVer);
        msg.put("uber.userAgent.platform", platform);

        if (Trace.isDebugEnabled()) {
            Trace.debug("************************************************************");
            Trace.debug("********** User-Agent: "+agentString);
            Trace.debug("********** Browser: "+browser);
            Trace.debug("********** Browser Version: "+browserVer);
            Trace.debug("********** Platform: "+platform);
            Trace.debug("************************************************************");
        }

        return true;
    }
}

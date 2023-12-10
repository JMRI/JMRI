package jmri.jmrit.z21server;


import jmri.DccThrottle;
import jmri.Throttle;

import java.net.InetAddress;
import java.util.HashMap;

public class AppClient {

    private InetAddress address;
    private HashMap<Integer, DccThrottle> throttles;


    public AppClient(InetAddress address) {
        this.address = address;
        throttles = new HashMap<>();
    }

    public void addThrottle(int locoAddress, DccThrottle throttle) {
        if (!throttles.containsKey(locoAddress)) {
            throttles.put(locoAddress, throttle);
        }
    }

    public DccThrottle getThrottleFromLocoAddress(int locoAddress) {
        if (throttles.containsKey(locoAddress)) {
            return throttles.get(locoAddress);
        } else {
            return null;
        }
    }


}

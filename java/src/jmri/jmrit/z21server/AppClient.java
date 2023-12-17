package jmri.jmrit.z21server;


import jmri.DccThrottle;
import jmri.Throttle;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;

public class AppClient {

    private InetAddress address;
    private HashMap<Integer, DccThrottle> throttles;

    private Date timestamp;


    public AppClient(InetAddress address) {
        this.address = address;
        throttles = new HashMap<>();
        heartbeat();
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

    public InetAddress getAddress() {
        return address;
    }

    public void heartbeat() {
        timestamp = new Date();
    }

    public boolean isTimestampExpired() {
        Duration duration = Duration.between(timestamp.toInstant(), new Date().toInstant());
        return (duration.toMinutes() >= 60);
        /* Per Z21 Spec, clients are deemed lost after one minute of inactivity. */
    }


}

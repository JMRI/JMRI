package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

/**
 * Manager for {@link jmri.server.json.throttle.JsonThrottle} objects. A manager
 * is needed since multiple JsonThrottle objects may be controlling the same
 * {@link jmri.DccLocoAddress}.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonThrottleManager implements InstanceManagerAutoDefault {

    private final HashMap<DccLocoAddress, JsonThrottle> throttles = new HashMap<>();
    private final HashMap<JsonThrottle, ArrayList<JsonThrottleSocketService>> services = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonThrottleManager() {
        // do nothing
    }

    // Synchronized throughout: notifyThrottleFound() is now deferred to the
    // EDT (see AbstractThrottleManager), so that deferred callback and a
    // thread handling a fresh JSON request can touch these maps at the
    // same time. getThrottles() returns a copy since callers iterate it.
    public synchronized Collection<JsonThrottle> getThrottles() {
        return new ArrayList<>(this.throttles.values());
    }

    public synchronized void put(DccLocoAddress address, JsonThrottle throttle) {
        this.throttles.put(address, throttle);
    }

    public synchronized void put(JsonThrottle throttle, JsonThrottleSocketService service) {
        this.services.computeIfAbsent(throttle, v -> new ArrayList<>()).add(service);
    }

    public synchronized boolean containsKey(DccLocoAddress address) {
        return this.throttles.containsKey(address);
    }

    public synchronized JsonThrottle get(DccLocoAddress address) {
        return this.throttles.get(address);
    }

    public synchronized void remove(DccLocoAddress address) {
        this.throttles.remove(address);
    }

    public synchronized List<JsonThrottleSocketService> getServers(JsonThrottle throttle) {
        return this.services.computeIfAbsent(throttle, v -> new ArrayList<>());
    }

    public synchronized void remove(JsonThrottle throttle, JsonThrottleSocketService server) {
        this.getServers(throttle).remove(server);
    }

    public ObjectMapper getObjectMapper() {
        return this.mapper;
    }

    public boolean canBeLongAddress(int asInt) {
        return InstanceManager.getDefault(ThrottleManager.class).canBeLongAddress(asInt);
    }

    public boolean canBeShortAddress(int asInt) {
        return InstanceManager.getDefault(ThrottleManager.class).canBeShortAddress(asInt);
    }

    public boolean requestThrottle(DccLocoAddress address, ThrottleListener listener) {
        return InstanceManager.getDefault(ThrottleManager.class).requestThrottle(address, listener, false);
    }

    public boolean requestThrottle(jmri.BasicRosterEntry rosterEntry, ThrottleListener listener) {
        return InstanceManager.getDefault(ThrottleManager.class).requestThrottle(rosterEntry, listener, false);
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonThrottleManager.class);
}

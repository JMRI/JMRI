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

    /**
     *
     * @return the default JsonThrottleManager
     * @deprecated since 4.11.4; use
     * {@link InstanceManager#getDefault(java.lang.Class)} directly
     */
    @Deprecated
    public static JsonThrottleManager getDefault() {
        jmri.util.Log4JUtil.deprecationWarning(log, "getDefault");        
        return InstanceManager.getDefault(JsonThrottleManager.class);
    }

    public Collection<JsonThrottle> getThrottles() {
        return this.throttles.values();
    }

    public void put(DccLocoAddress address, JsonThrottle throttle) {
        this.throttles.put(address, throttle);
    }

    public void put(JsonThrottle throttle, JsonThrottleSocketService service) {
        this.services.computeIfAbsent(throttle, v -> new ArrayList<>()).add(service);
    }

    public boolean containsKey(DccLocoAddress address) {
        return this.throttles.containsKey(address);
    }

    public JsonThrottle get(DccLocoAddress address) {
        return this.throttles.get(address);
    }

    public void remove(DccLocoAddress address) {
        this.throttles.remove(address);
    }

    public List<JsonThrottleSocketService> getServers(JsonThrottle throttle) {
        return this.services.computeIfAbsent(throttle, v -> new ArrayList<>());
    }

    public void remove(JsonThrottle throttle, JsonThrottleSocketService server) {
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

    public void attachListener(DccLocoAddress address, JsonThrottle throttle) {
        InstanceManager.getDefault(ThrottleManager.class).attachListener(address, throttle);
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonThrottleManager.class);
}

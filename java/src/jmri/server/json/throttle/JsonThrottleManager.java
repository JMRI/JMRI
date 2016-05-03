package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

/**
 * Manager for {@link jmri.server.json.throttle.JsonThrottle} objects. A manager
 * is needed since multiple JsonThrottle objects may be controlling the same
 * {@link jmri.DccLocoAddress}.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonThrottleManager {

    private final HashMap<DccLocoAddress, JsonThrottle> throttles = new HashMap<>();
    private final HashMap<JsonThrottle, ArrayList<JsonThrottleSocketService>> services = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonThrottleManager() {
        // do nothing
    }

    public static JsonThrottleManager getDefault() {
        if (InstanceManager.getDefault(JsonThrottleManager.class) == null) {
            InstanceManager.setDefault(JsonThrottleManager.class, new JsonThrottleManager());
        }
        return InstanceManager.getDefault(JsonThrottleManager.class);
    }

    public Collection<JsonThrottle> getThrottles() {
        return this.throttles.values();
    }

    public void put(DccLocoAddress address, JsonThrottle throttle) {
        this.throttles.put(address, throttle);
    }

    public void put(JsonThrottle throttle, JsonThrottleSocketService service) {
        if (this.services.get(throttle) == null) {
            this.services.put(throttle, new ArrayList<>());
        }
        this.services.get(throttle).add(service);
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
        if (this.services.get(throttle) == null) {
            this.services.put(throttle, new ArrayList<>());
        }
        return this.services.get(throttle);
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
        return InstanceManager.getDefault(ThrottleManager.class).requestThrottle(address, listener);
    }

    public void attachListener(DccLocoAddress address, JsonThrottle throttle) {
        InstanceManager.getDefault(ThrottleManager.class).attachListener(address, throttle);
    }
}

package jmri.server.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import jmri.InstanceManagerAutoDefault;
import jmri.server.json.JSON;
import jmri.server.json.JsonHttpService;
import jmri.spi.JsonServiceFactory;

/**
 * Cache for mapping {@link jmri.server.json.JsonHttpService}s to types for
 * getting schemas.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCache implements InstanceManagerAutoDefault {

    private HashMap<String, Set<JsonHttpService>> services = null;
    private final ObjectMapper mapper = new ObjectMapper();

    public synchronized Set<JsonHttpService> getServices(String type) {
        this.cacheServices();
        return services.get(type);
    }

    public synchronized Set<String> getTypes() {
        this.cacheServices();
        return services.keySet();
    }

    private void cacheServices() {
        if (services == null) {
            services = new HashMap<>();
            for (JsonServiceFactory<?, ?> factory : ServiceLoader.load(JsonServiceFactory.class)) {
                JsonHttpService service = factory.getHttpService(this.mapper);
                for (String type : factory.getTypes()) {
                    if (!type.equals(JSON.JSON)) {
                        Set<JsonHttpService> set = this.services.get(type);
                        if (set == null) {
                            this.services.put(type, new HashSet<>());
                            set = this.services.get(type);
                        }
                        set.add(service);
                    }
                }
                for (String type : factory.getSentTypes()) {
                    if (!type.equals(JSON.JSON)) {
                        Set<JsonHttpService> set = this.services.get(type);
                        if (set == null) {
                            this.services.put(type, new HashSet<>());
                            set = this.services.get(type);
                        }
                        set.add(service);
                    }
                }
                for (String type : factory.getReceivedTypes()) {
                    if (!type.equals(JSON.JSON)) {
                        Set<JsonHttpService> set = this.services.get(type);
                        if (set == null) {
                            this.services.put(type, new HashSet<>());
                            set = this.services.get(type);
                        }
                        set.add(service);
                    }
                }
            }
        }
    }
}

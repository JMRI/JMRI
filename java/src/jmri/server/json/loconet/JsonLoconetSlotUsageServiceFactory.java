package jmri.server.json.loconet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Registers the "loconetSlotUsage" JSON type.
 *
 * @author Andrew Deak Copyright (C) 2026
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonLoconetSlotUsageServiceFactory
        implements JsonServiceFactory<JsonLoconetSlotUsageHttpService, JsonLoconetSlotUsageSocketService> {

    @Override
    public String[] getTypes(String version) {
        return new String[]{JsonLoconetSlotUsage.LOCONET_SLOT_USAGE};
    }

    @Override
    public JsonLoconetSlotUsageSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonLoconetSlotUsageSocketService(connection);
    }

    @Override
    public JsonLoconetSlotUsageHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonLoconetSlotUsageHttpService(mapper);
    }

}

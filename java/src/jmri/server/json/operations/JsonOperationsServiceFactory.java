package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service factory for the JSON Operations services.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonOperationsServiceFactory implements JsonServiceFactory<JsonOperationsHttpService, JsonOperationsSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{CARS, ENGINES, LOCATIONS, TRAIN, TRAINS};
    }

    @Override
    public JsonOperationsSocketService getSocketService(JsonConnection connection) {
        return new JsonOperationsSocketService(connection);
    }

    @Override
    public JsonOperationsHttpService getHttpService(ObjectMapper mapper) {
        return new JsonOperationsHttpService(mapper);
    }

}

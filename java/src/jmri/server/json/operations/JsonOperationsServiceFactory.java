package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.KERNEL;
import static jmri.server.json.JSON.LOCATION;
import static jmri.server.json.JSON.LOCATIONS;
import static jmri.server.json.JSON.TRACK;
import static jmri.server.json.operations.JsonOperations.*;

import org.openide.util.lookup.ServiceProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

/**
 * Service factory for the JSON Operations services.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonOperationsServiceFactory implements JsonServiceFactory<JsonOperationsHttpService, JsonOperationsSocketService> {

    @Override
    public String[] getTypes(String version) {
        return new String[]{CAR, CARS, CAR_TYPE, ENGINE, ENGINES, KERNEL, LOCATION, LOCATIONS, ROLLING_STOCK, TRACK, TRAIN, TRAINS};
    }

    @Override
    public JsonOperationsSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonOperationsSocketService(connection);
    }

    @Override
    public JsonOperationsHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonOperationsHttpService(mapper);
    }

}

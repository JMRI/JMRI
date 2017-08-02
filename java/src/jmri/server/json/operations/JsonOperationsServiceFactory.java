package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service factory for the JSON Operations services.
 * 
 * @author Randall Wood (c) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonOperationsServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{CARS, ENGINES, LOCATIONS, TRAIN, TRAINS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonOperationsSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonOperationsHttpService(mapper);
    }
    
}

package jmri.server.json.signalmast;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import static jmri.server.json.signalmast.JsonSignalMast.SIGNAL_MAST;
import static jmri.server.json.signalmast.JsonSignalMast.SIGNAL_MASTS;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonSignalMastServiceFactory implements JsonServiceFactory<JsonSignalMastHttpService, JsonSignalMastSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{SIGNAL_MAST, SIGNAL_MASTS};
    }

    @Override
    public JsonSignalMastSocketService getSocketService(JsonConnection connection) {
        return new JsonSignalMastSocketService(connection);
    }

    @Override
    public JsonSignalMastHttpService getHttpService(ObjectMapper mapper) {
        return new JsonSignalMastHttpService(mapper);
    }

}

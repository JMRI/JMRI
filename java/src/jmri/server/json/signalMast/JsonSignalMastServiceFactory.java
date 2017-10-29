package jmri.server.json.signalMast;

import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MAST;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MASTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonSignalMastServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{SIGNAL_MAST, SIGNAL_MASTS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonSignalMastSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonSignalMastHttpService(mapper);
    }

}

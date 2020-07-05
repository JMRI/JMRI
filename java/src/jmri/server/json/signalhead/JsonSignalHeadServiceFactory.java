package jmri.server.json.signalhead;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEADS;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
@API(status = EXPERIMENTAL)
public class JsonSignalHeadServiceFactory implements JsonServiceFactory<JsonSignalHeadHttpService, JsonSignalHeadSocketService> {

    @Override
    public String[] getTypes(String version) {
        return new String[]{SIGNAL_HEAD, SIGNAL_HEADS};
    }

    @Override
    public JsonSignalHeadSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonSignalHeadSocketService(connection);
    }

    @Override
    public JsonSignalHeadHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonSignalHeadHttpService(mapper);
    }

}

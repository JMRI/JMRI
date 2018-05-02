package jmri.server.json.signalHead;

import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEADS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonSignalHeadServiceFactory implements JsonServiceFactory<JsonSignalHeadHttpService, JsonSignalHeadSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{SIGNAL_HEAD, SIGNAL_HEADS};
    }

    @Override
    public JsonSignalHeadSocketService getSocketService(JsonConnection connection) {
        return new JsonSignalHeadSocketService(connection);
    }

    @Override
    public JsonSignalHeadHttpService getHttpService(ObjectMapper mapper) {
        return new JsonSignalHeadHttpService(mapper);
    }

}

package jmri.server.json.logs;

import static jmri.server.json.logs.JsonLogs.LOGS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create loggers for use with the JSON services.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonLogsServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{LOGS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonLogsSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return null;
    }

}

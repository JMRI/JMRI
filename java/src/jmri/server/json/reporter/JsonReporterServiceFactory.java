package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.REPORTER;
import static jmri.server.json.reporter.JsonReporter.REPORTERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonReporterServiceFactory implements JsonServiceFactory<JsonReporterHttpService, JsonReporterSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{REPORTER, REPORTERS};
    }

    @Override
    public JsonReporterSocketService getSocketService(JsonConnection connection) {
        return new JsonReporterSocketService(connection);
    }

    @Override
    public JsonReporterHttpService getHttpService(ObjectMapper mapper) {
        return new JsonReporterHttpService(mapper);
    }

}

package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.REPORTER;
import static jmri.server.json.reporter.JsonReporter.REPORTERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonReporterServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{REPORTER, REPORTERS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonReporterSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonReporterHttpService(mapper);
    }

}

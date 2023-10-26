package jmri.server.json.logixngicon;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON service providers for handling {@link jmri.jmrit.display.LogixNGIcon}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonLogixNGIconServiceFactory implements JsonServiceFactory<JsonLogixNGIconHttpService, JsonLogixNGIconSocketService> {

    public static final String LOGIXNG_ICON = "logixngicon"; // NOI18N

    @Override
    public String[] getTypes(String version) {
        return new String[]{LOGIXNG_ICON};
    }

    @Override
    public JsonLogixNGIconSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonLogixNGIconSocketService(connection);
    }

    @Override
    public JsonLogixNGIconHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonLogixNGIconHttpService(mapper);
    }

}

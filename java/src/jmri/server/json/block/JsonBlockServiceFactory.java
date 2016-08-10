package jmri.server.json.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemoryServiceFactory)
 * @author Randall Wood
 */
public class JsonBlockServiceFactory implements JsonServiceFactory {

    public static final String BLOCK = "block"; // NOI18N
    public static final String BLOCKS = "blocks"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{BLOCK, BLOCKS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonBlockSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonBlockHttpService(mapper);
    }

}

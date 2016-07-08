package jmri.server.json.layoutblock;

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
public class JsonLayoutBlockServiceFactory implements JsonServiceFactory {

    public static final String LAYOUTBLOCK = "layoutblock"; // NOI18N
    public static final String LAYOUTBLOCKS = "layoutblocks"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{LAYOUTBLOCK, LAYOUTBLOCKS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonLayoutBlockSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonLayoutBlockHttpService(mapper);
    }

}

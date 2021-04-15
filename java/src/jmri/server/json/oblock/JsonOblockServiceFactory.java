package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

import static jmri.server.json.oblock.JsonOblock.OBLOCK;
import static jmri.server.json.oblock.JsonOblock.OBLOCKS;

/**
 * Copied from jmri/server/json/blocks.java
 *
 * @author mstevetodd Copyright (C) 2018
 * @author Randall Wood Copyright 2018
 * @author Egbert Broerse Copyright 2020
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonOblockServiceFactory implements JsonServiceFactory<JsonOblockHttpService, JsonOblockSocketService> {


    @Override
    public String[] getTypes(String version) {
        return new String[]{OBLOCK, OBLOCKS};
    }

    @Override
    public JsonOblockSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonOblockSocketService(connection);
    }

    @Override
    public JsonOblockHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonOblockHttpService(mapper);
    }

}

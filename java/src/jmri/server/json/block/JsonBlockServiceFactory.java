package jmri.server.json.block;

import static jmri.server.json.block.JsonBlock.BLOCK;
import static jmri.server.json.block.JsonBlock.BLOCKS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mstevetodd Copyright (C) 2018
 * @author Randall Wood Copyright 2018
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonBlockServiceFactory implements JsonServiceFactory<JsonBlockHttpService, JsonBlockSocketService> {


    @Override
    public String[] getTypes(String version) {
        return new String[]{BLOCK, BLOCKS};
    }

    @Override
    public JsonBlockSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonBlockSocketService(connection);
    }

    @Override
    public JsonBlockHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonBlockHttpService(mapper);
    }

}

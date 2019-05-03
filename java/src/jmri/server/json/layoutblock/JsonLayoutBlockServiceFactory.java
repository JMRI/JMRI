package jmri.server.json.layoutblock;

import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCKS;

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
public class JsonLayoutBlockServiceFactory implements JsonServiceFactory<JsonLayoutBlockHttpService, JsonLayoutBlockSocketService> {


    @Override
    public String[] getTypes() {
        return new String[]{LAYOUTBLOCK, LAYOUTBLOCKS};
    }

    @Override
    public JsonLayoutBlockSocketService getSocketService(JsonConnection connection) {
        return new JsonLayoutBlockSocketService(connection);
    }

    @Override
    public JsonLayoutBlockHttpService getHttpService(ObjectMapper mapper) {
        return new JsonLayoutBlockHttpService(mapper);
    }

}

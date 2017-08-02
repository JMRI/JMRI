package jmri.server.json.block;

import static jmri.server.json.block.JsonBlock.BLOCK;
import static jmri.server.json.block.JsonBlock.BLOCKS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemoryServiceFactory)
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonBlockServiceFactory implements JsonServiceFactory {


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

package jmri.server.json.layoutblock;

import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCKS;

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
public class JsonLayoutBlockServiceFactory implements JsonServiceFactory {


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

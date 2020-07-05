package jmri.server.json.block;

import jmri.Block;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author mstevetodd Copyright (C) 2018 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public class JsonBlockSocketService extends JsonNamedBeanSocketService<Block, JsonBlockHttpService> {

    public JsonBlockSocketService(JsonConnection connection) {
        super(connection, new JsonBlockHttpService(connection.getObjectMapper()));
    }
}

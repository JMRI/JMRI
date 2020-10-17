package jmri.server.json.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 *
 * @author mstevetodd Copyright (C) 2018 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
public class JsonOblockSocketService extends JsonNamedBeanSocketService<OBlock, JsonOblockHttpService> {

    public JsonOblockSocketService(JsonConnection connection) {
        super(connection, new JsonOblockHttpService(connection.getObjectMapper()));
    }
}

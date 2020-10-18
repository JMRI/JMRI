package jmri.server.json.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 * Copied from jmri/server/json/blocks.java

 * @author mstevetodd Copyright (C) 2018
 * @author Randall Wood
 */
public class JsonOblockSocketService extends JsonNamedBeanSocketService<OBlock, JsonOblockHttpService> {

    public JsonOblockSocketService(JsonConnection connection) {
        super(connection, new JsonOblockHttpService(connection.getObjectMapper()));
    }

}

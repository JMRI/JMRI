package jmri.server.json.idtag;

import jmri.IdTag;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood Copyright 2019
 */
@API(status = EXPERIMENTAL)
public class JsonIdTagSocketService extends JsonNamedBeanSocketService<IdTag, JsonIdTagHttpService> {

    public JsonIdTagSocketService(JsonConnection connection) {
        super(connection, new JsonIdTagHttpService(connection.getObjectMapper()));
    }
}

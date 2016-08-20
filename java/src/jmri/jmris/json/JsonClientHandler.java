package jmri.jmris.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;

/**
 * 
 * @author Randall Wood
 * @deprecated Use {@link jmri.server.json.JsonClientHandler} instead.
 */
@Deprecated
public class JsonClientHandler extends jmri.server.json.JsonClientHandler {

    @Deprecated
    public JsonClientHandler(JsonConnection connection, ObjectMapper mapper) {
        super(connection);
    }
}

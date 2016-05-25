package jmri.jmris.json;

import java.io.DataOutputStream;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author Randall Wood
 * @deprecated Use {@link jmri.server.json.JsonConnection} instead.
 */
@Deprecated
public class JsonConnection extends jmri.server.json.JsonConnection {
    
    public JsonConnection(Session connection) {
        super(connection);
    }
    
    public JsonConnection(DataOutputStream output) {
        super(output);
    }
}

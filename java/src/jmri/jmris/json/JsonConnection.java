package jmri.jmris.json;

import java.io.DataOutputStream;
import jmri.jmris.JmriConnection;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author Randall Wood
 */
public class JsonConnection extends JmriConnection {

    public JsonConnection(Session connection) {
        super(connection);
    }
    
    public JsonConnection(DataOutputStream output) {
        super(output);
    }
}

//JmriSRCPProgrammerServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmris.AbstractProgrammerServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP interface between the JMRI service mode programmer and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2012
 * @version $Revision: 21286 $
 */
public class JsonProgrammerServer extends AbstractProgrammerServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonProgrammerServer.class.getName());

    public JsonProgrammerServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int CV, int value, int status) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("sendStatus called for CV " + CV + " with value " + value + " and status " + status);
        }
        if (status == ProgListener.OK) {
            ObjectNode root = this.mapper.createObjectNode();
            root.put(TYPE, PROGRAMMER);
            ObjectNode data = root.putObject(DATA);
            data.put(NODE_CV, CV);
            data.put(VALUE, value);
            data.put(STATE, status);
            this.connection.sendMessage(this.mapper.writeValueAsString(root));
        } else {
            this.sendError(416, Bundle.getMessage("ErrorProgrammer416"));
        }
    }

    @Override
    public void sendNotAvailableStatus() throws IOException {
        this.sendError(499, Bundle.getMessage("ErrorProgrammer499"));
    }

    @Override
    public void parseRequest(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        int mode = data.path(MODE).asInt(Programmer.REGISTERMODE);
        int CV = data.path(NODE_CV).asInt();
        int value = data.path(VALUE).asInt();
        if (WRITE.equals(data.path(OP).asText())) {
            this.writeCV(mode, CV, value);
        } else if (READ.equals(data.path(OP).asText())) {
            this.readCV(mode, CV);
        }
    }

    protected void sendError(int code, String message) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(TYPE, PROGRAMMER);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }
}

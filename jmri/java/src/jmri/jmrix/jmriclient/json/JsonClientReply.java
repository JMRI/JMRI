package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jmri.jmris.json.JSON;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonClientReply extends AbstractMRReply {

    private JsonNode message = null;
    private final static Logger log = LoggerFactory.getLogger(JsonClientReply.class);

    public JsonClientReply() {
        super();
    }

    public JsonClientReply(String s) {
        super(s);
        try {
            this.setMessage((new ObjectMapper()).readTree(s));
        } catch (IOException ex) {
            log.error("Unable to process reply \"{}\": {}", s, ex.getMessage());
        }
    }

    public JsonClientReply(JsonNode node) {
        super();
        this.setMessage(node);
    }

    public JsonClientReply(JsonClientReply reply) {
        super(reply);
        this.setMessage(reply.message);
    }

    @Override
    protected int skipPrefix(int index) {
        // may need to modify message->data->name
        return 0;
    }

    /**
     * @return the message
     */
    public JsonNode getMessage() {
        return message;
    }

    public JsonNode getData() {
        return message.path(JSON.DATA);
    }

    public boolean isResponseOK() {
        return (this.message != null);
    }

    public String getResponseCode() {
        return this.message.toString();
    }

    /**
     * Extracts Read-CV returned value from a message.
     *
     * JSON protocol does not handle CVs, so this always returns -1.
     *
     * @return -1
     */
    @Override
    public int value() {
        return -1;
    }

    @Override
    public boolean isUnsolicited() {
        return (this.isResponseOK())
                ? this.message.path(JSON.TYPE).asText().equals(JSON.HELLO)
                : false;
    }

    final void setMessage(JsonNode node) {
        this.message = node;
        log.debug("Reply {}", this.toString());
    }

}

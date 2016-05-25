package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import jmri.jmrix.AbstractMRMessage;

/**
 * Encodes a message to an JMRI JSON server. The {@link JsonClientReply} class
 * handles the response from the server.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 * @author Randall Wood 2014
 *
 * @see jmri.jmris.json.JsonServer
 * @see jmri.jmris.json.JSON
 */
/*
 * This class may turn out to be a simple carrier for a JsonNode, but leave it
 * in place should there come a need to massage the message before sending it
 * later.
 */
public class JsonClientMessage extends AbstractMRMessage {

    private JsonNode message;

    public JsonClientMessage(int i) {
        throw new UnsupportedOperationException("JsonClientMessages can only be created with a JsonNode.");
    }

    public JsonClientMessage(JsonNode node) {
        this.message = node;
    }

    /* The Json client should be asynchronous */
    @Override
    public boolean replyExpected() {
        return false;
    }

    /* if JsonClientTrafficController overrides AbstractMRTrafficController.transmitLoop(), this method is OBE */
    @Override
    public int getNumDataElements() {
        return message.size();
    }

    /* if JsonClientTrafficController overrides AbstractMRTrafficController.transmitLoop(), this method is OBE */
    @Override
    public int getElement(int n) {
        return message.toString().charAt(n);
    }

    @Override
    public String toString() {
        return message.toString();
    }

    public JsonNode getMessage() {
        return message;
    }
}

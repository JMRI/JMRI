package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import jmri.jmrix.AbstractMRListener;

/**
 *
 * @author Randall Wood 2014
 */
public interface JsonClientListener extends AbstractMRListener {

    public void message(JsonClientMessage message);

    public void message(JsonNode message);

    public void reply(JsonClientReply m);

    public void reply(JsonNode reply);

}

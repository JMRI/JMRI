package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Randall Wood
 */
public interface JsonAsyncHttpService {

    /**
     * Create a listener that can be disposed of.
     *
     * @param type The type of object to listen for changes to
     * @param name The name of the object to listen for changes to
     * @param data The current start of the object to listen to
     * @return
     */
    public JsonAsyncHttpListener getListener(String type, String name, JsonNode data);

}

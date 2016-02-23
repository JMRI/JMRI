package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import javax.servlet.AsyncContext;

/**
 *
 * @author Randall Wood
 */
public interface JsonAsyncHttpService {

    public void doAsyncGet(String type, String name, JsonNode data, AsyncContext context);
    
}

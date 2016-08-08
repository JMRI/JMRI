package jmri.server.json.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonUtilServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{JSON.GOODBYE,
            JSON.HELLO,
            JSON.LOCALE,
            JSON.METADATA,
            JSON.NETWORK_SERVICES,
            JSON.NODE,
            JSON.PING,
            JSON.RAILROAD,
            JSON.SYSTEM_CONNECTIONS};
    }

    @Override
    public JsonUtilSocketService getSocketService(JsonConnection connection) {
        return new JsonUtilSocketService(connection);
    }

    @Override
    public JsonUtilHttpService getHttpService(ObjectMapper mapper) {
        return new JsonUtilHttpService(mapper);
    }

}

package jmri.server.json.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonUtilServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{JSON.GOODBYE,
            JSON.HELLO,
            JSON.LOCALE,
            JSON.METADATA,
            JSON.NETWORK_SERVICES,
            JSON.NODE,
            JSON.PANELS,
            JSON.PING,
            JSON.RAILROAD,
            JSON.SYSTEM_CONNECTIONS,
            JSON.CONFIG_PROFILES};
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

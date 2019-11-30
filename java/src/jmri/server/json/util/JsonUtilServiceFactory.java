package jmri.server.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonUtilServiceFactory implements JsonServiceFactory<JsonUtilHttpService, JsonUtilSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{JSON.GOODBYE,
                JSON.HELLO,
                JSON.METADATA,
                JSON.NETWORK_SERVICE,
                JSON.NETWORK_SERVICES,
                JSON.NODE,
                JSON.PANEL,
                JSON.PANELS,
                JSON.RAILROAD,
                JSON.SYSTEM_CONNECTION,
                JSON.SYSTEM_CONNECTIONS,
                JSON.CONFIG_PROFILE,
                JSON.CONFIG_PROFILES};
    }

    @Override
    public String[] getSentTypes() {
        // retain ERROR on behalf of JsonException for schema handling
        // retain LIST on behalf of JSON servers for schema handling
        // retain PONG on behalf of JSON servers for schema handling
        return new String[]{JsonException.ERROR,
            JSON.CONFIG_PROFILE,
            JSON.LIST,
            JSON.NETWORK_SERVICE,
            JSON.PANEL,
            JSON.PONG,
            JSON.SYSTEM_CONNECTION};
    }

    @Override
    public String[] getReceivedTypes() {
        // retain LOCALE on behalf of JSON servers for schema handling
        // retain PING on behalf of JSON servers for schema handling
        return new String[]{JSON.LOCALE,
            JSON.PING};
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

package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTurnoutServiceFactory implements JsonServiceFactory<JsonTurnoutHttpService, JsonTurnoutSocketService> {

    /**
     * @deprecated since 4.19.4; use {@link JsonTurnout#TURNOUT} instead
     */
    @Deprecated
    public static final String TURNOUT = JsonTurnout.TURNOUT; // NOI18N
    /**
     * @deprecated since 4.19.4; use {@link JsonTurnout#TURNOUTS} instead
     */
    @Deprecated
    public static final String TURNOUTS = JsonTurnout.TURNOUTS; // NOI18N

    @Override
    public String[] getTypes(String version) {
        return new String[]{JsonTurnout.TURNOUT, JsonTurnout.TURNOUTS};
    }

    @Override
    public JsonTurnoutSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonTurnoutSocketService(connection);
    }

    @Override
    public JsonTurnoutHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonTurnoutHttpService(mapper);
    }

}

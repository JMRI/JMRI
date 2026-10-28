package jmri.server.json.panel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = JsonServiceFactory.class)
public class JsonPanelServiceFactory implements JsonServiceFactory<JsonPanelHttpService, JsonPanelSocketService> {

    public static final String PANEL_DISPLAY_OPTIONS = "panelDisplayOptions";

    @Override
    public String[] getTypes(String version) {
        return new String[]{PANEL_DISPLAY_OPTIONS};
    }

    @Override
    public JsonPanelSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonPanelSocketService(connection);
    }

    @Override
    public JsonPanelHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonPanelHttpService(mapper);
    }
}

package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.layoutblock.JsonLayoutBlockServiceFactory.LAYOUTBLOCK;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
public class JsonLayoutBlockSocketService extends JsonSocketService {

    private final JsonLayoutBlockHttpService service;
    private final HashMap<String, LayoutBlockListener> layoutblocks = new HashMap<>();
    private Locale locale;

    public JsonLayoutBlockSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonLayoutBlockHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.layoutblocks.containsKey(name)) {
            LayoutBlock layoutblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
            LayoutBlockListener listener = new LayoutBlockListener(layoutblock);
            layoutblock.addPropertyChangeListener(listener);
            this.layoutblocks.put(name, listener);
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        layoutblocks.values().stream().forEach((layoutblock) -> {
            layoutblock.layoutblock.removePropertyChangeListener(layoutblock);
        });
        layoutblocks.clear();
    }

    private class LayoutBlockListener implements PropertyChangeListener {

        protected final LayoutBlock layoutblock;

        public LayoutBlockListener(LayoutBlock layoutblock) {
            this.layoutblock = layoutblock;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("state") ||
                e.getPropertyName().equals("routing")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(LAYOUTBLOCK, this.layoutblock.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    layoutblock.removePropertyChangeListener(this);
                    layoutblocks.remove(this.layoutblock.getSystemName());
                }
            }
        }
    }

}

package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
public class JsonLayoutBlockSocketService extends JsonSocketService<JsonLayoutBlockHttpService> {

    private final HashMap<String, LayoutBlockListener> layoutBlocks = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonLayoutBlockServiceFactory.class);

    public JsonLayoutBlockSocketService(JsonConnection connection) {
        super(connection, new JsonLayoutBlockHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String name = data.path(NAME).asText();
        if (method.equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.layoutBlocks.containsKey(name)) {
            LayoutBlock layoutblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
            if (layoutblock != null) {
                LayoutBlockListener listener = new LayoutBlockListener(layoutblock);
                layoutblock.addPropertyChangeListener(listener);
                this.layoutBlocks.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        layoutBlocks.values().stream().forEach((layoutblock) -> {
            layoutblock.layoutBlock.removePropertyChangeListener(layoutblock);
        });
        layoutBlocks.clear();
    }

    private class LayoutBlockListener implements PropertyChangeListener {

        protected final LayoutBlock layoutBlock;

        public LayoutBlockListener(LayoutBlock layoutblock) {
            this.layoutBlock = layoutblock;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("redraw")) {
                log.debug("{} property '{}' changed from '{}' to '{}'", this.layoutBlock.getUserName(),
                        e.getPropertyName(), e.getOldValue(), e.getNewValue());
                try {
                    try {
                        connection.sendMessage(service.doGet(LAYOUTBLOCK, this.layoutBlock.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    layoutBlock.removePropertyChangeListener(this);
                    layoutBlocks.remove(this.layoutBlock.getSystemName());
                }
            }
        }
    }

}

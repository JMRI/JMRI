package jmri.server.json.panel;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/** WebSocket notifications for panel display-option changes. */
public class JsonPanelSocketService extends JsonSocketService<JsonPanelHttpService> {

    private static final long DISPLAY_UPDATE_DELAY_MS = 200;
    private static final ScheduledExecutorService UPDATE_SCHEDULER = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable task) {
                    Thread thread = new Thread(task, "JMRI web panel updates");
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private Editor editor;
    private String panelName;
    private ScheduledFuture<?> pendingUpdate;
    private boolean closed;
    private final PropertyChangeListener listener = this::propertyChange;

    public JsonPanelSocketService(JsonConnection connection) {
        super(connection, new JsonPanelHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        String name = data.path("name").asText();
        Editor requested = InstanceManager.getDefault(EditorManager.class).getAll().stream()
                .filter(candidate -> candidate instanceof PanelEditor || candidate instanceof ControlPanelEditor)
                .filter(candidate -> candidate.getTargetFrame() != null
                        && candidate.getTargetFrame().getTitle().equals(name))
                .findFirst().orElse(null);
        if (requested != editor) {
            removeEditorListeners();
            editor = requested;
            if (editor != null) {
                editor.addPropertyChangeListener("webPanelDisplayOptions", listener);
            }
        }
        panelName = name;
    }

    private synchronized void propertyChange(PropertyChangeEvent event) {
        String property = event.getPropertyName();
        if (closed || editor == null || !"webPanelDisplayOptions".equals(property)) {
            return;
        }
        if (pendingUpdate != null) {
            pendingUpdate.cancel(false);
        }
        pendingUpdate = UPDATE_SCHEDULER.schedule(this::sendDisplayOptionsChanged,
                DISPLAY_UPDATE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void sendDisplayOptionsChanged() {
        synchronized (this) {
            if (closed || editor == null || panelName == null) {
                return;
            }
            pendingUpdate = null;
        }
        try {
            com.fasterxml.jackson.databind.node.ObjectNode message = connection.getObjectMapper().createObjectNode()
                    .put("type", JsonPanelServiceFactory.PANEL_DISPLAY_OPTIONS);
            message.putObject("data").put("name", panelName).put("value", "displayOptionsChanged");
            connection.sendMessage(message, 0);
        } catch (IOException ignored) {
            // The connection is closing; no further update can be delivered.
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) {
        // Panel notifications are subscriptions, not list requests.
    }

    @Override
    public synchronized void onClose() {
        closed = true;
        if (pendingUpdate != null) {
            pendingUpdate.cancel(false);
            pendingUpdate = null;
        }
        removeEditorListeners();
        panelName = null;
    }

    private void removeEditorListeners() {
        if (editor != null) {
            editor.removePropertyChangeListener("webPanelDisplayOptions", listener);
            editor = null;
        }
    }
}

package jmri.server.json.signalhead;

import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEADS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Randall Wood (C) 2016
 */
public class JsonSignalHeadSocketService extends JsonSocketService<JsonSignalHeadHttpService> {

    private final HashMap<String, SignalHeadListener> beanListeners = new HashMap<>();
    private SignalHeadsListener managerListener = null;
    private static final Logger log = LoggerFactory.getLogger(JsonSignalHeadSocketService.class);

    public JsonSignalHeadSocketService(JsonConnection connection) {
        this(connection, new JsonSignalHeadHttpService(connection.getObjectMapper()));
    }

    // package protected
    public JsonSignalHeadSocketService(JsonConnection connection, JsonSignalHeadHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        String name = data.path(NAME).asText();
        if (request.method.equals(PUT)) {
            connection.sendMessage(service.doPut(type, name, data, request), request.id);
        } else {
            connection.sendMessage(service.doPost(type, name, data, request), request.id);
        }
        if (!beanListeners.containsKey(name)) {
            SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
            if (signalHead != null) {
                SignalHeadListener listener = new SignalHeadListener(signalHead);
                signalHead.addPropertyChangeListener(listener);
                beanListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
        log.debug("adding SignalHeadsListener");
        if (managerListener == null) {
            managerListener = new SignalHeadsListener();
            InstanceManager.getDefault(SignalHeadManager.class).addPropertyChangeListener(managerListener);
        }
    }

    @Override
    public void onClose() {
        beanListeners.values().stream()
                .forEach(listener -> listener.signalHead.removePropertyChangeListener(listener));
        beanListeners.clear();
        InstanceManager.getDefault(SignalHeadManager.class).removePropertyChangeListener(managerListener);
        managerListener = null;
    }

    private class SignalHeadListener implements PropertyChangeListener {

        protected final SignalHead signalHead;

        public SignalHeadListener(SignalHead signalHead) {
            this.signalHead = signalHead;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            try {
                try {
                    connection.sendMessage(service.doGet(SIGNAL_HEAD, signalHead.getSystemName(),
                            connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), GET, 0)), 0);
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ie) {
                // if we get an error, de-register
                signalHead.removePropertyChangeListener(this);
                beanListeners.remove(signalHead.getSystemName());
            }
        }
    }

    private class SignalHeadsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SignalHeadsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());

            try {
                try {
                    // send the new list
                    connection.sendMessage(service.doGetList(SIGNAL_HEADS, service.getObjectMapper().createObjectNode(),
                            new JsonRequest(getLocale(), getVersion(), GET, 0)), 0);
                    // child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        removeListenersFromRemovedBeans();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending SignalHeads: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // do nothing; the listeners will be removed as the connection
                // gets closed
            }
        }

        private void removeListenersFromRemovedBeans() {
            SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
            for (String name : new HashSet<>(beanListeners.keySet())) {
                if (manager.getBySystemName(name) == null) {
                    beanListeners.remove(name);
                }
            }
        }
    }

}

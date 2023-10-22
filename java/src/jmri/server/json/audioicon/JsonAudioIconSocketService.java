package jmri.server.json.audioicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import jmri.JmriException;
import jmri.jmrit.display.AudioIcon;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.audioicon.JsonAudioIconServiceFactory.AUDIO_ICON;

/**
 * JSON socket service provider for managing {@link jmri.jmrit.display.AudioIcon}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonAudioIconSocketService extends JsonSocketService<JsonAudioIconHttpService> {

    private final HashMap<AudioIcon, AudioIconListener> beanListeners = new HashMap<>();

    public JsonAudioIconSocketService(JsonConnection connection) {
        super(connection, new JsonAudioIconHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetListNotAllowed", type), request.id);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        if (request.method.equals(JSON.GET)) {
            connection.sendMessage(service.doGet(type, "audioicon", data, request), request.id);
        }
        AudioIcon audioIcon = AudioIcon.IDENTITY_MANAGER.getAudioIcon(data.get("identity").asInt());
        log.error("onMessage: {}, {}, {}, {}", data.get("identity").asText(), data.get("identity").asInt(), audioIcon, beanListeners.containsKey(audioIcon));
        if (!beanListeners.containsKey(audioIcon)) {
            log.error("onMessage: add listener: {}", beanListeners.containsKey(audioIcon));
            addListenerToBean(audioIcon);
        }
    }

    @Override
    public void onClose() {
        beanListeners.values().stream().forEach(listener -> listener._audioIcon.removePropertyChangeListener(listener));
        beanListeners.clear();
    }

    protected void addListenerToBean(AudioIcon audioIcon) {
        log.error("addListenerToBean: {}", audioIcon);
        if (audioIcon != null) {
            AudioIconListener listener = new AudioIconListener(audioIcon);
            audioIcon.addPropertyChangeListener(listener);
            this.beanListeners.put(audioIcon, listener);
        }
    }

    private class AudioIconListener implements PropertyChangeListener {

        public final AudioIcon _audioIcon;

        public AudioIconListener(AudioIcon audioIcon) {
            this._audioIcon = audioIcon;
            log.error("AudioIconListener ctor(): {}", audioIcon.getIdentity());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.error("AudioIconListener.propertyChange(): {}, {}", evt.getPropertyName(), AudioIcon.PROPERTY_COMMAND);

            // Do nothing if unknown property command
            if (!evt.getPropertyName().equals(AudioIcon.PROPERTY_COMMAND)) return;

            try {
                log.error("AudioIconListener.propertyChange() try");
                String command;
                switch (evt.getNewValue().toString()) {
                    case AudioIcon.PROPERTY_COMMAND_PLAY:
                        command = JSON.AUDIO_COMMAND_PLAY;
                        log.error("AudioIconListener.propertyChange(): play");
                        break;
                    case AudioIcon.PROPERTY_COMMAND_STOP:
                        command = JSON.AUDIO_COMMAND_STOP;
                        log.error("AudioIconListener.propertyChange(): stop");
                        break;
                    default:
                        // Do nothing if unknown property command
                        log.error("AudioIconListener.propertyChange(): other: {}", evt.getNewValue());
                        return;
                }

                JsonRequest request = new JsonRequest(getLocale(), getVersion(), JSON.GET, 0);

                ObjectNode root = connection.getObjectMapper().createObjectNode();
                root.put(JSON.TYPE, AUDIO_ICON);
                root.put(JSON.METHOD, JSON.GET);
                root.put(JSON.ID, request.id);

                ObjectNode data = root.with(JSON.DATA);
                data.put(JSON.AUDIO_ICON_IDENTITY, _audioIcon.getIdentity());
                data.put(JSON.AUDIO_COMMAND, command);
                log.error("AudioIconListener.propertyChange(): sendMessage: {}", command);
                connection.sendMessage(root, 0);
            } catch (IOException ex) {
                // if we get an error, unregister as listener
                this._audioIcon.removePropertyChangeListener(this);
                beanListeners.remove(this._audioIcon);
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AudioIcon.class);
}

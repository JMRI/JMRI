package jmri.server.json.audio;

import jmri.Audio;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 * JSON Socket service for {@link jmri.Audio}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonAudioSocketService extends JsonNamedBeanSocketService<Audio, JsonAudioHttpService> {

    public JsonAudioSocketService(JsonConnection connection) {
        super(connection, new JsonAudioHttpService(connection.getObjectMapper()));
    }
}

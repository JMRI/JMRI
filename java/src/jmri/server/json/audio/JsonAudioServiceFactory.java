package jmri.server.json.audio;

import static jmri.server.json.audio.JsonAudio.AUDIO;
import static jmri.server.json.audio.JsonAudio.AUDIOS;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON services for {@link jmri.Audio}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonAudioServiceFactory implements JsonServiceFactory<JsonAudioHttpService, JsonAudioSocketService> {

    @Override
    public String[] getTypes(String version) {
        return new String[]{AUDIO, AUDIOS};
    }

    @Override
    public JsonAudioSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonAudioSocketService(connection);
    }

    @Override
    public JsonAudioHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonAudioHttpService(mapper);
    }

}

package jmri.server.json.audioicon;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON service providers for handling {@link jmri.jmrit.display.AudioIcon}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonAudioIconServiceFactory implements JsonServiceFactory<JsonAudioIconHttpService, JsonAudioIconSocketService> {

    public static final String AUDIO_ICON = "audioicon"; // NOI18N

    @Override
    public String[] getTypes(String version) {
        return new String[]{AUDIO_ICON};
    }

    @Override
    public JsonAudioIconSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonAudioIconSocketService(connection);
    }

    @Override
    public JsonAudioIconHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonAudioIconHttpService(mapper);
    }

}

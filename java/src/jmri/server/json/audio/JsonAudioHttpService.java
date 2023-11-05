package jmri.server.json.audio;

import static jmri.server.json.audio.JsonAudio.AUDIO;
import static jmri.server.json.audio.JsonAudio.AUDIOS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.ProvidingManager;
import jmri.Audio;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioSource;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;

/**
 * JSON HTTP Service for {@link jmri.Audio}s.
 *
 * @author Randall Wood      Copyright 2016, 2018
 * @author Daniel Bergqvist  Copyright 2023
 */
public class JsonAudioHttpService extends JsonNamedBeanHttpService<Audio> {

    public JsonAudioHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Audio audio, String name, String type, JsonRequest request) throws JsonException {
        if (audio.getSubType() != Audio.SOURCE || (!(audio instanceof AudioSource))) {
            throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorAudioNotSource", AUDIO, audio.getSubType()), request.id);
        }
        AudioSource audioSource = (AudioSource) audio;
        ObjectNode root = this.getNamedBean(audio, name, getType(), request); // throws JsonException if audio == null
        ObjectNode data = root.with(JSON.DATA);
        switch (audio.getState()) {
            case Audio.STATE_PLAYING:
                data.put(JSON.STATE, JSON.AUDIO_PLAYING);
                data.put(JSON.AUDIO_COMMAND_PLAY_NUM_LOOPS, audioSource.getLastNumLoops());
                break;
            case Audio.STATE_STOPPED:
                data.put(JSON.STATE, JSON.AUDIO_STOPPED);
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, "ErrorInternal", type), request.id); // NOI18N
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Audio audio, String name, String type, JsonNode data, JsonRequest request) throws JsonException {

        if (audio.getSubType() != Audio.SOURCE || (!(audio instanceof AudioSource))) {
            throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorAudioNotSource", AUDIO, audio.getSubType()), request.id);
        }

        AudioSource audioSource = (AudioSource) audio;
        String command = data.path(JSON.AUDIO_COMMAND).asText();
        switch (command) {
            case JSON.AUDIO_COMMAND_PLAY:
                audioSource.play();
                break;
            case JSON.AUDIO_COMMAND_STOP:
                audioSource.stop();
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorAudioUnknownCommand", command), request.id);
        }
        return this.doGet(audio, name, type, request);
    }

    @Override
    protected void doDelete(Audio bean, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        deleteBean(bean, name, type, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case AUDIO:
            case AUDIOS:
                return doSchema(type,
                        server,
                        "jmri/server/json/audio/audio-server.json",
                        "jmri/server/json/audio/audio-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return AUDIO;
    }

    @Override
    protected Manager<Audio> getManager() {
        return InstanceManager.getDefault(AudioManager.class);
    }

    @Override
    protected ProvidingManager<Audio> getProvidingManager() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported since AudioManager is not a ProvidingManager");
    }

}

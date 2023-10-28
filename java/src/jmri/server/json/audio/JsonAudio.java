package jmri.server.json.audio;

/**
 * Tokens used by the JMRI JSON Audio service.
 *
 * @author Randall Wood     (C) 2016
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonAudio {

    public static final String AUDIO = "audio"; // NOI18N
    public static final String AUDIOS = "audios"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonAudio() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

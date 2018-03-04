package jmri.server.json.consist;

/**
 * Constants used by the internal JMRI JSON Consist service.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonConsist {

    /**
     * {@value #CONSIST}
     */
    public static final String CONSIST = "consist"; // NOI18N
    /**
     * {@value #CONSISTS}
     */
    public static final String CONSISTS = "consists"; // NOI18N
    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonConsist() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }

}

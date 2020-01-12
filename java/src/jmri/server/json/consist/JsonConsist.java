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
     * {@value #ERROR_NO_CONSIST_MANAGER}, a key for localized error messages
     * indicating that no consist manager is available.
     */
    public static final String ERROR_NO_CONSIST_MANAGER = "ErrorNoConsistManager"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonConsist() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }

}

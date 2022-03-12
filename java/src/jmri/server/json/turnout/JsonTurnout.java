package jmri.server.json.turnout;

/**
 * Tokens used by the JMRI JSON Turnout service.
 *
 * @author Randall Wood (C) 2020
 * @since 4.19.4
 */
public class JsonTurnout {

    public static final String TURNOUT = "turnout"; // NOI18N
    public static final String TURNOUTS = "turnouts"; // NOI18N

    /**
     * {@value #FEEDBACK_MODE}
     * <p>
     * The feedback mode for this turnout. One of {@value #FEEDBACK_MODES} 
     */
    public static final String FEEDBACK_MODE = "feedbackMode"; // NOI18N
    /**
     * {@value #FEEDBACK_MODES}
     * <p>
     * The list of possible feedback modes for this turnout.
     */
    public static final String FEEDBACK_MODES = "feedbackModes"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonTurnout() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

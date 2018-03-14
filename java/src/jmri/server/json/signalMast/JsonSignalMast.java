package jmri.server.json.signalMast;

/**
 * Constants used by the {@link jmri.server.json.signalMast} package.
 *
 * @author Randall Wood 2016
 */
public class JsonSignalMast {

    /**
     * {@value #SIGNAL_MAST}
     */
    public static final String SIGNAL_MAST = "signalMast"; // NOI18N
    /**
     * {@value #SIGNAL_MASTS}
     */
    public static final String SIGNAL_MASTS = "signalMasts"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonSignalMast() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

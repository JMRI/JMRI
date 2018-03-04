package jmri.server.json.reporter;

/**
 * Constants used by the {@link jmri.server.json.reporter} package.
 *
 * @author Randall Wood 2016
 */
public class JsonReporter {

    /**
     * {@value #REPORTER}
     */
    public static final String REPORTER = "reporter"; // NOI18N
    /**
     * {@value #REPORTERS}
     */
    public static final String REPORTERS = "reporters"; // NOI18N
    /**
     * {@value #REPORT}
     */
    public static final String REPORT = "report"; // NOI18N
    /**
     * {@value #LAST_REPORT}
     */
    public static final String LAST_REPORT = "lastReport"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonReporter() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

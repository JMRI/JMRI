package jmri.server.json.roster;

/**
 * Constants for JSON handling of the JMRI Roster.
 *
 * @author Randall Wood
 */
public class JsonRoster {

    /**
     * {@value #ROSTER}
     */
    public static final String ROSTER = "roster"; // NOI18N
    /**
     * {@value #ROSTER_ENTRY}
     */
    public static final String ROSTER_ENTRY = "rosterEntry"; // NOI18N
    /**
     * {@value #ROSTER_GROUP}
     */
    public static final String ROSTER_GROUP = "rosterGroup"; // NOI18N
    /**
     * {@value #ROSTER_GROUPS}
     */
    public static final String ROSTER_GROUPS = "rosterGroups"; // NOI18N
    /**
     * {@value #ATTRIBUTES}
     */
    public static final String ATTRIBUTES = "attributes"; // NOI18N
    /**
     * {@value #DATE_MODIFIED}
     */
    public static final String DATE_MODIFIED = "dateModified"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonRoster() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

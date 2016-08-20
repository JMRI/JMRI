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
     * Unusable constructor, made private to avoid public documentation.
     */
    private JsonRoster() {
    }
}

package jmri.jmrit.roster;

import jmri.beans.ArbitraryBean;

/**
 * Provide a common class for Roster entries and groups to inherit from. This
 * supports building tree-like user interfaces for Roster entries and groups.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public abstract class RosterObject extends ArbitraryBean {

    /**
     * Get the formatted single-line String for displaying the object.
     *
     * @return a formatted name
     */
    abstract public String getDisplayName();

}

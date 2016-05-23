package jmri.jmrit.roster;

import jmri.beans.BeanInterface;

/**
 * Provide a common class for Roster entries and groups to inherit from. This
 * supports building tree-like user interfaces for Roster entries and groups.
 *
 * @author Randall Wood {@literal <randall.h.wood@alexandriasoftware.com>}
 */
public interface RosterObject extends BeanInterface {

    /**
     * Get the formatted single-line String for displaying the object.
     *
     * @return a formatted name
     */
    abstract public String getDisplayName();

}

package jmri.jmrit.roster;

import jmri.beans.BeanInterface;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide a common class for Roster entries and groups to inherit from. This
 * supports building tree-like user interfaces for Roster entries and groups.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
@API(status = MAINTAINED)
public interface RosterObject extends BeanInterface {

    /**
     * Get the formatted single-line String for displaying the object.
     *
     * @return a formatted name
     */
    abstract public String getDisplayName();

}

package jmri.jmrit.roster.rostergroup;

import java.beans.PropertyChangeListener;

/**
 * The getter method for a roster group selection.
 * <p>
 * Objects that implement this interface will be able to provide a source for
 * getting a roster group to other objects that manipulate roster groups.
 *
 * @author Randall Wood
 */
public interface RosterGroupSelector {

    /**
     * Key for the property "selectedRosterGroup"
     */
    String SELECTED_ROSTER_GROUP = "selectedRosterGroup";

    String getSelectedRosterGroup();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}

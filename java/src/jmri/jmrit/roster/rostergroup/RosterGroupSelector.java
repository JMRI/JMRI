package jmri.jmrit.roster.rostergroup;

import java.beans.PropertyChangeListener;

/**
 * The getter method for a roster group selection.
 * <p>
 * Objects that implement this interface will be able to provide a source for
 * getting a roster group to other objects that manipulate roster groups.
 *
 * @author rhwood
 */
public interface RosterGroupSelector {

    /**
     * Key for the property "selectedRosterGroup"
     */
    public final static String SELECTED_ROSTER_GROUP = "selectedRosterGroup";

    public String getSelectedRosterGroup();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}

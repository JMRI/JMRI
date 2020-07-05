package jmri.jmrit.roster.rostergroup;

import java.beans.PropertyChangeListener;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * The getter method for a roster group selection.
 * <p>
 * Objects that implement this interface will be able to provide a source for
 * getting a roster group to other objects that manipulate roster groups.
 *
 * @author Randall Wood
 */
@API(status = MAINTAINED)
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

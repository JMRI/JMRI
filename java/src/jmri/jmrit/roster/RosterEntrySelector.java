package jmri.jmrit.roster;

import java.beans.PropertyChangeListener;

/**
 * The getter method for a roster entry selection.
 * <p>
 * Classes that implement this interface will be able to provide a source for
 * getting a roster entry or entries to other objects that manipulate roster
 * entries.
 * <p>
 * <b>Note:</b> Classes implementing this interface must fire a
 * propertyChangeEvent for the <i>selectedRosterEntries</i> property whenever
 * the selected roster entries change.
 *
 * @author rhwood
 */
public interface RosterEntrySelector {

    static final String SELECTED_ROSTER_ENTRIES = "selectedRosterEntries";

    /**
     * Get the currently selected roster entries. Since the selection could
     * contain multiple roster entries, this returns an array of RosterEntry
     * instead of a single RosterEntry.
     *
     * @return an array of RosterEntries
     */
    public RosterEntry[] getSelectedRosterEntries();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}

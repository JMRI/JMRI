// RosterGroupComboBox.java
package jmri.jmrit.roster.swing;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

/**
 * A JComboBox of Roster Groups.
 *
 * @author  Randall Wood Copyright (C) 2011
 * @version	$Revision: $
 * @see         jmri.jmrit.roster.Roster
 */
public class RosterGroupComboBox extends JComboBox implements RosterGroupSelector {

    private Roster _roster;

    /**
     * Create a RosterGroupComboBox with an arbitrary Roster instead of the
     * default Roster instance.
     *
     * @param roster
     */
    // needed for unit tests
    public RosterGroupComboBox(Roster roster) {
        this(roster, Roster.getRosterGroupName());
    }

    /**
     * Create a RosterGroupComboBox with an arbitrary selection instead of the
     * active roster group.
     *
     * @param selection
     */
    // plan for roster selections that may not mirror the active roster group
    public RosterGroupComboBox(String selection) {
        this(Roster.instance(), selection);
    }

    /**
     * Create a RosterGroupComboBox with arbitrary selection and Roster.
     * 
     * @param roster
     * @param selection 
     */
    public RosterGroupComboBox(Roster roster, String selection) {
        super();
        _roster = roster;
        update(selection);
    }

    /**
     * Create a RosterGroupComboBox with the default Roster instance and the
     * active roster group.
     */
    public RosterGroupComboBox() {
        this(Roster.instance(), Roster.getRosterGroupName());
    }

    /**
     * Update the combo box and select the active roster group.
     */
    public final void update() {
        update(Roster.getRosterGroupName());
    }

    /**
     * Update the combo box and select given String.
     *
     * @param selection
     */
    public final void update(String selection) {
        removeAllItems();
        ArrayList<String> l = _roster.getRosterGroupList();
        Collections.sort(l);
        for (String g : l) {
            addItem(g);
        }
        insertItemAt(Roster.ALLENTRIES, 0);
        setSelectedItem(selection);
    }

    public String getSelectedRosterGroup() {
        if (getSelectedItem().equals(Roster.ALLENTRIES)) {
            return null;
        } else {
            return getSelectedItem().toString();
        }
    }
}

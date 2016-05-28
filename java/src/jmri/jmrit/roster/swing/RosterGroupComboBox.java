package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

/**
 * A JComboBox of Roster Groups.
 *
 * @author Randall Wood Copyright (C) 2011, 2014
 * @see jmri.jmrit.roster.Roster
 */
public class RosterGroupComboBox extends JComboBox<String> implements RosterGroupSelector {

    private Roster _roster;
    private boolean allEntriesEnabled = true;

    /**
     * Create a RosterGroupComboBox with an arbitrary Roster instead of the
     * default Roster instance.
     *
     */
    // needed for unit tests
    public RosterGroupComboBox(Roster roster) {
        this(roster, roster.getDefaultRosterGroup());
    }

    /**
     * Create a RosterGroupComboBox with an arbitrary selection.
     *
     */
    public RosterGroupComboBox(String selection) {
        this(Roster.instance(), selection);
    }

    /**
     * Create a RosterGroupComboBox with arbitrary selection and Roster.
     *
     */
    public RosterGroupComboBox(Roster roster, String selection) {
        super();
        _roster = roster;
        update(selection);
        roster.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals("RosterGroupAdded")) {
                    update();
                } else if (pce.getPropertyName().equals("RosterGroupRemoved")
                        || pce.getPropertyName().equals("RosterGroupRenamed")) {
                    if (getSelectedItem().equals(pce.getOldValue())) {
                        update((String) pce.getNewValue());
                    } else {
                        update();
                    }
                }
            }
        });
    }

    /**
     * Create a RosterGroupComboBox with the default Roster instance and the
     * default roster group.
     */
    public RosterGroupComboBox() {
        this(Roster.instance(), Roster.instance().getDefaultRosterGroup());
    }

    /**
     * Update the combo box and reselect the current selection.
     */
    public final void update() {
        update((String) this.getSelectedItem());
    }

    /**
     * Update the combo box and select given String.
     *
     */
    public final void update(String selection) {
        removeAllItems();
        ArrayList<String> l = _roster.getRosterGroupList();
        Collections.sort(l);
        for (String g : l) {
            addItem(g);
        }
        if (allEntriesEnabled) {
            insertItemAt(Roster.AllEntries(Locale.getDefault()), 0);
            if (selection == null) {
                selection = Roster.ALLENTRIES;
            }
            this.setToolTipText(null);
        } else {
            if (this.getItemCount() == 0) {
                this.addItem(Bundle.getMessage("RosterGroupComboBoxNoGroups"));
                this.setToolTipText(Bundle.getMessage("RosterGroupComboBoxNoGroupsToolTip"));
            } else {
                this.setToolTipText(null);
            }
        }
        setSelectedItem(selection);
        if (this.getItemCount() == 1) {
            this.setSelectedIndex(0);
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    }

    @Override
    public String getSelectedRosterGroup() {
        if (getSelectedItem() == null) {
            return null;
        } else if (getSelectedItem().equals(Roster.ALLENTRIES)
                || getSelectedItem().equals(Roster.AllEntries(Locale.getDefault()))) {
            return null;
        } else {
            return getSelectedItem().toString();
        }
    }

    /**
     * @return the allEntriesEnabled
     */
    public boolean isAllEntriesEnabled() {
        return allEntriesEnabled;
    }

    /**
     * @param allEntriesEnabled the allEntriesEnabled to set
     */
    public void setAllEntriesEnabled(boolean allEntriesEnabled) {
        this.allEntriesEnabled = allEntriesEnabled;
        this.update();
    }
}

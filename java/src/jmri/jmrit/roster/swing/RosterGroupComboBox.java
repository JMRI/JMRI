package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeEvent;
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
     * @param roster the Roster to show the groups of
     */
    // needed for unit tests
    public RosterGroupComboBox(Roster roster) {
        this(roster, roster.getDefaultRosterGroup());
    }

    /**
     * Create a RosterGroupComboBox with an arbitrary selection.
     *
     * @param selection the initial roster group selection
     */
    public RosterGroupComboBox(String selection) {
        this(Roster.getDefault(), selection);
    }

    /**
     * Create a RosterGroupComboBox with arbitrary selection and Roster.
     *
     * @param roster    the Roster to show the groups of
     * @param selection the initial roster group selection
     */
    public RosterGroupComboBox(Roster roster, String selection) {
        super();
        _roster = roster;
        update(selection);
        roster.addPropertyChangeListener((PropertyChangeEvent pce) -> {
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
        });
    }

    /**
     * Create a RosterGroupComboBox with the default Roster instance and the
     * default roster group.
     */
    public RosterGroupComboBox() {
        this(Roster.getDefault(), Roster.getDefault().getDefaultRosterGroup());
    }

    /**
     * Update the combo box and reselect the current selection.
     */
    public final void update() {
        update(this.getSelectedItem());
    }

    /**
     * Update the combo box and select given String.
     * <p>
     * @param selection the selection to update to
     */
    public final void update(String selection) {
        removeAllItems();
        ArrayList<String> l = _roster.getRosterGroupList();
        Collections.sort(l);
        l.forEach((g) -> {
            addItem(g);
        });
        if (allEntriesEnabled) {
            insertItemAt(Roster.allEntries(Locale.getDefault()), 0);
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
                || getSelectedItem().equals(Roster.allEntries(Locale.getDefault()))) {
            return null;
        } else {
            return getSelectedItem();
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

    @Override
    public String getSelectedItem() {

        Object item = super.getSelectedItem();
        return item != null ? item.toString() : null;
    }
}

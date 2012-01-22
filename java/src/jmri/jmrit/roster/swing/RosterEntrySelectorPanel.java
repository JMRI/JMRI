// RosterEntrySelectorPanel.java

package jmri.jmrit.roster.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

/**
 *
 * @author rhwood
 */
public class RosterEntrySelectorPanel extends JPanel implements RosterEntrySelector, RosterGroupSelector {

    private RosterEntryComboBox entryCombo;
    private RosterGroupComboBox groupCombo;

    public RosterEntrySelectorPanel() {
        this(null, null);
    }

    public RosterEntrySelectorPanel(RosterEntry re, String rg) {
        super();
        this.setLayout(new FlowLayout());
        this.entryCombo = new RosterEntryComboBox(rg);
        if (re != null) {
            this.entryCombo.setSelectedItem(re.titleString());
        }
        this.groupCombo = new RosterGroupComboBox(rg);
        this.add(groupCombo);
        this.add(entryCombo);

        this.groupCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                entryCombo.update(getSelectedRosterGroup());
            }

        });

        this.entryCombo.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals("selectedRosterEntries")) {
                    fireSelectedRosterEntriesPropertyChange(pce.getOldValue(), pce.getNewValue());
                }
            }

        });
    }

    protected void fireSelectedRosterEntriesPropertyChange(Object oldValue, Object newValue) {
        this.firePropertyChange("selectedRosterEntries", oldValue, newValue);
    }

    @Override
    public RosterEntry[] getSelectedRosterEntries() {
        return entryCombo.getSelectedRosterEntries();
    }

    public void setSelectedRosterEntry(RosterEntry re) {
        entryCombo.setSelectedItem((re != null) ? re.getId() : null);
    }

    public void setSelectedRosterEntryAndGroup(RosterEntry re, String rg) {
        this.setSelectedRosterEntry(re);
        this.setSelectedRosterGroup(rg);
    }

    @Override
    public String getSelectedRosterGroup() {
        return groupCombo.getSelectedRosterGroup();
    }

    public void setSelectedRosterGroup(String rg) {
        groupCombo.update(rg);
    }

    public String getEmptyEntry() {
        return entryCombo.getNonSelectedItem();
    }

    public void setEmptyEntry(String emptyEntry) {
        entryCombo.setNonSelectedItem(emptyEntry);
    }

    public JComboBox getRosterEntryComboBox() {
        return entryCombo;
    }

    public JComboBox getRosterGroupComboBox() {
        return groupCombo;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.entryCombo.setEnabled(enabled);
        this.groupCombo.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return this.entryCombo.isEnabled();
    }
}

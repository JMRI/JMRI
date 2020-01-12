package jmri.jmrit.roster.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JComboBox containing roster entries or a string indicating that no roster
 * entry is selected.
 * <p>
 * This is a JComboBox&lt;Object&gt; so that it can represent both.
 * <p>
 * This class has a self contained data model, and will automatically update the
 * display if a RosterEntry is added, removed, or changes.
 *
 * @author Randall Wood Copyright (C) 2011
 * @see jmri.jmrit.roster.Roster
 * @see jmri.jmrit.roster.RosterEntry
 * @see javax.swing.JComboBox
 */
public class RosterEntryComboBox extends JComboBox<Object> implements RosterEntrySelector {

    protected Roster _roster;
    protected String _group;
    protected String _roadName;
    protected String _roadNumber;
    protected String _dccAddress;
    protected String _mfg;
    protected String _decoderMfgID;
    protected String _decoderVersionID;
    protected String _id;
    protected String _nonSelectedItem = Bundle.getMessage("RosterEntryComboBoxNoSelection");
    protected RosterEntry[] _currentSelection = null;

    private final static Logger log = LoggerFactory.getLogger(RosterEntryComboBox.class);

    /**
     * Create a combo box with the default Roster and all entries in the active
     * roster group.
     */
    public RosterEntryComboBox() {
        this(Roster.getDefault(), Roster.getDefault().getDefaultRosterGroup(), null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with an arbitrary Roster and all entries in the active
     * roster group.
     *
     */
    public RosterEntryComboBox(Roster roster) {
        this(roster, Roster.getDefault().getDefaultRosterGroup(), null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with the default Roster and all entries in an
     * arbitrary roster group.
     *
     */
    public RosterEntryComboBox(String rosterGroup) {
        this(Roster.getDefault(), rosterGroup, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with an arbitrary Roster and all entries in an
     * arbitrary roster group.
     *
     */
    public RosterEntryComboBox(Roster roster, String rosterGroup) {
        this(roster, rosterGroup, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with the default Roster and entries in the active
     * roster group matching the specified attributes. Attributes with a null
     * value will not be considered when filtering the roster entries.
     *
     */
    public RosterEntryComboBox(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        this(Roster.getDefault(),
                Roster.getDefault().getDefaultRosterGroup(),
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }

    /**
     * Create a combo box with an arbitrary Roster and entries in the active
     * roster group matching the specified attributes. Attributes with a null
     * value will not be considered when filtering the roster entries.
     *
     */
    public RosterEntryComboBox(Roster roster,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        this(roster,
                Roster.getDefault().getDefaultRosterGroup(),
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);

    }

    /**
     * Create a combo box with the default Roster and entries in an arbitrary
     * roster group matching the specified attributes. Attributes with a null
     * value will not be considered when filtering the roster entries.
     *
     */
    public RosterEntryComboBox(String rosterGroup,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        this(Roster.getDefault(),
                rosterGroup,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }

    /**
     * Create a combo box with an arbitrary Roster and entries in an arbitrary
     * roster group matching the specified attributes. Attributes with a null
     * value will not be considered when filtering the roster entries.
     * <p>
     * All attributes used to filter roster entries are retained and reused when
     * updating the combo box unless new attributes are specified when calling
     * update.
     * <p>
     * All other constructors call this constructor with various default
     * parameters.
     *
     */
    public RosterEntryComboBox(Roster roster,
            String rosterGroup,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        super();
        setRenderer(new jmri.jmrit.roster.swing.RosterEntryListCellRenderer());
        _roster = roster;
        _group = rosterGroup;
        update(rosterGroup,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);

        _roster.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals("add")
                        || pce.getPropertyName().equals("remove")
                        || pce.getPropertyName().equals("change")) {
                    update();
                }
            }
        });

        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                fireSelectedRosterEntriesPropertyChange();
            }
        });

        _nonSelectedItem = Bundle.getMessage("RosterEntryComboBoxNoSelection");
    }

    /**
     * Update the combo box with the currently selected roster group, using the
     * same roster entry attributes specified in a prior call to update or when
     * creating the combo box.
     */
    public void update() {
        update(this._group,
                _roadName,
                _roadNumber,
                _dccAddress,
                _mfg,
                _decoderMfgID,
                _decoderVersionID,
                _id);
    }

    /**
     * Update the combo box with an arbitrary roster group, using the same
     * roster entry attributes specified in a prior call to update or when
     * creating the combo box.
     *
     */
    public final void update(String rosterGroup) {
        update(rosterGroup,
                _roadName,
                _roadNumber,
                _dccAddress,
                _mfg,
                _decoderMfgID,
                _decoderVersionID,
                _id);
    }

    /**
     * Update the combo box with the currently selected roster group, using new
     * roster entry attributes.
     *
     */
    public void update(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        update(this._group,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }

    /**
     * Update the combo box with an arbitrary roster group, using new roster
     * entry attributes.
     */
    public final void update(String rosterGroup,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        Object selection = this.getSelectedItem();
        if (log.isDebugEnabled()) {
            log.debug("Old selection: " + selection);
            log.debug("Old group: " + _group);
        }
        ActionListener[] ALs = this.getActionListeners();
        for (ActionListener al : ALs) {
            this.removeActionListener(al);
        }
        this.setSelectedItem(null);
        List<RosterEntry> l = _roster.matchingList(roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
        _group = rosterGroup;
        _roadName = roadName;
        _roadNumber = roadNumber;
        _dccAddress = dccAddress;
        _mfg = mfg;
        _decoderMfgID = decoderMfgID;
        _decoderVersionID = decoderVersionID;
        _id = id;
        removeAllItems();
        if (_nonSelectedItem != null) {
            insertItemAt(_nonSelectedItem, 0);
            setSelectedItem(_nonSelectedItem);
        }
        for (RosterEntry r : l) {
            if (rosterGroup != null && !rosterGroup.equals(Roster.ALLENTRIES)) {
                if (r.getAttribute(Roster.getRosterGroupProperty(rosterGroup)) != null
                        && r.getAttribute(Roster.getRosterGroupProperty(rosterGroup)).equals("yes")) {
                    addItem(r);
                }
            } else {
                addItem(r);
            }
            if (r.equals(selection)) {
                this.setSelectedItem(r);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("New selection: " + this.getSelectedItem());
            log.debug("New group: " + _group);
        }
        for (ActionListener al : ALs) {
            this.addActionListener(al);
        }
        // fire the action event only if selection is not in the updated combobox
        // don't use equals() since selection or getSelectedItem could be null
        if (this.getSelectedItem() != selection) {
            this.fireActionEvent();
            // this is part of the RosterEntrySelector contract
            this.fireSelectedRosterEntriesPropertyChange();
        }
    }

    /**
     * Set the text of the item that visually indicates that no roster entry is
     * selected in the comboBox.
     *
     */
    public void setNonSelectedItem(String itemText) {
        _nonSelectedItem = itemText;
        update(_group);
    }

    /**
     * Get the text of the item that visually indicates that no roster entry is
     * selected in the comboBox.
     *
     * If this returns null, it indicates that the comboBox has no special item
     * to indicate an empty selection.
     *
     * @return The text or null
     */
    public String getNonSelectedItem() {
        return _nonSelectedItem;
    }

    @Override
    public RosterEntry[] getSelectedRosterEntries() {
        return getSelectedRosterEntries(false);
    }

    // internally, we sometimes want to be able to force the reconstruction of
    // the cached value returned by getSelectedRosterEntries
    protected RosterEntry[] getSelectedRosterEntries(boolean force) {
        if (_currentSelection == null || force) {
            if (this.getSelectedItem() != null && !this.getSelectedItem().equals(_nonSelectedItem)) {
                _currentSelection = new RosterEntry[1];
                _currentSelection[0] = (RosterEntry) this.getSelectedItem();
            } else {
                _currentSelection = new RosterEntry[0];
            }
        }
        return _currentSelection;
    }

    // this method allows anonymous listeners to fire the "selectedRosterEntries" property change
    protected void fireSelectedRosterEntriesPropertyChange() {
        this.firePropertyChange(RosterEntrySelector.SELECTED_ROSTER_ENTRIES,
                _currentSelection,
                this.getSelectedRosterEntries(true));
    }

}

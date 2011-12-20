// RosterEntryComboBox.java
package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * A JComboBox containing roster entries
 * <p>
 * This class has a self contained data model, and will automatically update the
 * display if a RosterEntry is added, removed, or changes.
 * <p>
 * This class <em>does not</em> update if the active roster group changes.
 * Implementors must include that behavior if it is desired.
 *
 * @author Randall Wood Copyright (C) 2011
 * @version $Revision: $
 * @see jmri.jmrit.roster.Roster
 * @see jmri.jmrit.roster.RosterEntry
 * @see javax.swing.JComboBox
 */
public class RosterEntryComboBox extends JComboBox {

    protected Roster _roster;
    protected String _roadName;
    protected String _roadNumber;
    protected String _dccAddress;
    protected String _mfg;
    protected String _decoderMfgID;
    protected String _decoderVersionID;
    protected String _id;

    /**
     * Create a combo box with the default Roster and all entries in the active
     * roster group.
     */
    public RosterEntryComboBox() {
        this(Roster.instance(), Roster.getRosterGroupName(), null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with an arbitrary Roster and all entries in the active
     * roster group.
     *
     * @param roster
     */
    public RosterEntryComboBox(Roster roster) {
        this(roster, Roster.getRosterGroupName(), null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with the default Roster and all entries in an arbitrary
     * roster group.
     *
     * @param rosterGroup
     */
    public RosterEntryComboBox(String rosterGroup) {
        this(Roster.instance(), rosterGroup, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with an arbitrary Roster and all entries in an arbitrary
     * roster group.
     *
     * @param roster
     * @param rosterGroup
     */
    public RosterEntryComboBox(Roster roster, String rosterGroup) {
        this(roster, rosterGroup, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with the default Roster and entries in the active roster
     * group matching the specified attributes. Attributes with a null value will
     * not be considered when filtering the roster entries.
     *
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
     */
    public RosterEntryComboBox(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        this(Roster.instance(),
                Roster.getRosterGroupName(),
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }

    /**
     * Create a combo box with an arbitrary Roster and entries in the active roster
     * group matching the specified attributes. Attributes with a null value will
     * not be considered when filtering the roster entries.
     *
     * @param roster
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
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
                Roster.getRosterGroupName(),
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);

    }

    /**
     * Create a combo box with the default Roster and entries in an arbitrary roster
     * group matching the specified attributes. Attributes with a null value will
     * not be considered when filtering the roster entries.
     *
     * @param rosterGroup
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
     */
    public RosterEntryComboBox(String rosterGroup,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        this(Roster.instance(),
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
     * Create a combo box with an arbitrary Roster and entries in an arbitrary roster
     * group matching the specified attributes. Attributes with a null value will
     * not be considered when filtering the roster entries.
     * <p>
     * All attributes used to filter roster entries are retained and reused when
     * updating the combo box unless new attributes are specified when calling update.
     * <p>
     * All other constructors call this constructor with various default parameters.
     *
     * @param roster
     * @param rosterGroup
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
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
        _roster = roster;
        update(rosterGroup,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
        
        _roster.addPropertyChangeListener(rosterListener);
        
        setRenderer(new jmri.jmrit.roster.swing.RosterEntryListCellRenderer());
    }
    
    PropertyChangeListener rosterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals("add")
                        || pce.getPropertyName().equals("remove")
                        || pce.getPropertyName().equals("change")) {
                    update();
                }
            }
        };
    
    public void setRosterListenerEnabled(boolean boo){
        if(boo)
            _roster.addPropertyChangeListener(rosterListener);
        else
            _roster.removePropertyChangeListener(rosterListener);
    }

    /**
     * Update the combo box with the active roster group, using the same roster
     * entry attributes specified in a prior call to update or when creating the
     * combo box.
     */
    public void update() {
        update(Roster.getRosterGroupName(),
                _roadName,
                _roadNumber,
                _dccAddress,
                _mfg,
                _decoderMfgID,
                _decoderVersionID,
                _id);
    }

    /**
     * Update the combo box with an arbitrary roster group, using the same roster
     * entry attributes specified in a prior call to update or when creating the
     * combo box.
     *
     * @param rosterGroup
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
     * Update the combo box with the active roster group, using new roster
     * entry attributes.
     *
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
     */
    public void update(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        update(Roster.getRosterGroupName(),
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
     *
     * @param rosterGroup
     * @param roadName
     * @param roadNumber
     * @param dccAddress
     * @param mfg
     * @param decoderMfgID
     * @param decoderVersionID
     * @param id
     */
    public final void update(String rosterGroup,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        List<RosterEntry> l = _roster.matchingList(roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
        _roadName = roadName;
        _roadNumber = roadNumber;
        _dccAddress = dccAddress;
        _mfg = mfg;
        _decoderMfgID = decoderMfgID;
        _decoderVersionID = decoderVersionID;
        _id = id;
        removeAllItems();
        for (RosterEntry r : l) {
            if (!rosterGroup.equals(Roster.ALLENTRIES)) {
                if (r.getAttribute(Roster.getRosterGroupProperty(rosterGroup)) != null
                        && r.getAttribute(Roster.getRosterGroupProperty(rosterGroup)).equals("yes")) {
                    addItem(r.titleString());
                }
            } else {
                addItem(r.titleString());
            }
        }
    }
}

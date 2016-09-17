package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.Roster;

/**
 * A {@link RosterEntryComboBox} that lists all roster entries in the Roster
 * without respect to a roster group.
 *
 * @author Randall Wood Copyright (C) 2011
 * @see RosterEntryComboBox
 */
public class GlobalRosterEntryComboBox extends RosterEntryComboBox {

    /**
     * Create a combo box with all roster entries in the default Roster.
     */
    public GlobalRosterEntryComboBox() {
        super(Roster.getDefault(), Roster.ALLENTRIES, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with all roster entries in an arbitrary Roster.
     *
     */
    public GlobalRosterEntryComboBox(Roster roster) {
        super(roster, Roster.ALLENTRIES, null, null, null, null, null, null, null);
    }

    /**
     * Create a combo box with roster entries in the default Roster matching the
     * specified attributes.
     *
     */
    public GlobalRosterEntryComboBox(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        super(Roster.getDefault(),
                Roster.ALLENTRIES,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }

    /**
     * Create a combo box with roster entries in an arbitrary Roster matching
     * the specified attributes.
     *
     */
    public GlobalRosterEntryComboBox(Roster roster,
            String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        super(roster,
                Roster.ALLENTRIES,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);

    }

    @Override
    public void update() {
        update(Roster.ALLENTRIES,
                _roadName,
                _roadNumber,
                _dccAddress,
                _mfg,
                _decoderMfgID,
                _decoderVersionID,
                _id);
    }

    @Override
    public void update(String roadName,
            String roadNumber,
            String dccAddress,
            String mfg,
            String decoderMfgID,
            String decoderVersionID,
            String id) {
        update(Roster.ALLENTRIES,
                roadName,
                roadNumber,
                dccAddress,
                mfg,
                decoderMfgID,
                decoderVersionID,
                id);
    }
}

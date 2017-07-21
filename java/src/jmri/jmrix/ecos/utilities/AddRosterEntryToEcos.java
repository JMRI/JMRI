package jmri.jmrix.ecos.utilities;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add a Roster Entry to the Ecos
 *
<<<<<<< HEAD
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Kevin Dickerson Copyright (C) 2009
  */
=======
 * @author Kevin Dickerson Copyright (C) 2009
 */
>>>>>>> JMRI/master
public class AddRosterEntryToEcos extends AbstractAction {

    private EcosLocoAddressManager objEcosLocoManager;

    /**
     * @param s Name of this action, e.g. in menus
     */
    public AddRosterEntryToEcos(String s, EcosSystemConnectionMemo memo) {
        super(s);
        adaptermemo = memo;
        objEcosLocoManager = adaptermemo.getLocoAddressManager();
    }

    EcosSystemConnectionMemo adaptermemo;
    JComboBox<String> rosterEntry = new JComboBox<String>();
    JComboBox<String> selections;
    Roster roster;

    @Override
    public void actionPerformed(ActionEvent event) {

        roster = Roster.getDefault();

        rosterEntryUpdate();

        int retval = JOptionPane.showOptionDialog(null,
                Bundle.getMessage("AddToEcosDialog"),
                Bundle.getMessage("AddToEcosTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), rosterEntry}, null);
        log.debug("Dialog value " + retval + " selected, "
                + rosterEntry.getSelectedIndex() + ":" + rosterEntry.getSelectedItem());
        if (retval != 1 || rosterEntry.getItemCount() == 0) {
            return;
        }

        String selEntry = (String) rosterEntry.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        log.debug("Add " + re.getId() + " to ECoS");
        RosterToEcos rosterToEcos = new RosterToEcos();
        rosterToEcos.createEcosLoco(re, adaptermemo);
    }

    void rosterEntryUpdate() {
        if (rosterEntry != null) {
            rosterEntry.removeAllItems();
        }
        for (RosterEntry r : roster.getAllEntries()) {
            // Add only those locos to the drop-down list that are in the JMRI Roster but not in the ECoS
            String DccAddress = r.getDccAddress();
            EcosLocoAddress EcosAddress = null;
            if (DccAddress != null) {
                log.debug("DccAddress=" + DccAddress);
                try {
                    EcosAddress = objEcosLocoManager.getByDccAddress(Integer.parseInt(DccAddress));
                } catch (NullPointerException npe) {
                    log.warn("Could not connect to ECoS roster via objEcosLocoManager to loop up Loco {}", DccAddress);
                    return;
                }
            }
            if (EcosAddress == null) {
                // It is not possible to create MFX locomotives in the ECoS. They are auto-discovered.
                if (r.getProtocol() != jmri.LocoAddress.Protocol.MFX) {
                    rosterEntry.addItem(r.titleString());
                }
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AddRosterEntryToEcos.class.getName());

}

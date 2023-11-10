package jmri.jmrix.ecos.utilities;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.swing.JmriJOptionPane;

/**
 * Add a Roster Entry to the Ecos
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class AddRosterEntryToEcos extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus.
     * @param memo system connection.
     */
    public AddRosterEntryToEcos(String s, EcosSystemConnectionMemo memo) {
        super(s);
        adaptermemo = memo;
        objEcosLocoManager = adaptermemo.getLocoAddressManager();
    }

    private final EcosSystemConnectionMemo adaptermemo;
    private final EcosLocoAddressManager objEcosLocoManager;
    private final JComboBox<String> rosterEntry = new JComboBox<>();
    private Roster roster;

    @Override
    public void actionPerformed(ActionEvent event) {

        roster = Roster.getDefault();

        rosterEntryUpdate();

        int retval = JmriJOptionPane.showConfirmDialog(null,
                new Object[]{Bundle.getMessage("AddToEcosDialog"), rosterEntry},
                Bundle.getMessage("AddToEcosTitle"),
                JmriJOptionPane.OK_CANCEL_OPTION );
        log.debug("Dialog value {} selected, {}:{}", retval, rosterEntry.getSelectedIndex(), rosterEntry.getSelectedItem());
        if (retval != JmriJOptionPane.OK_OPTION || rosterEntry.getItemCount() == 0) {
            return;
        }

        String selEntry = (String) rosterEntry.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        log.debug("Add {} to ECoS", re.getId());
        RosterToEcos rosterToEcos = new RosterToEcos(adaptermemo);
        rosterToEcos.createEcosLoco(re);
    }

    void rosterEntryUpdate() {
        rosterEntry.removeAllItems();
        for (RosterEntry r : roster.getAllEntries()) {
            // Add only those locos to the drop-down list that are in the JMRI Roster but not in the ECoS
            String dccAddress = r.getDccAddress();
            EcosLocoAddress ecosAddress = null;
            if (dccAddress != null) {
                log.debug("DccAddress={}", dccAddress);
                try {
                    ecosAddress = objEcosLocoManager.getByDccAddress(Integer.parseInt(dccAddress));
                } catch (NullPointerException npe) {
                    log.warn("Could not connect to ECoS roster via objEcosLocoManager to loop up Loco {}", dccAddress);
                    return;
                }
            }
            if ( ecosAddress == null && r.getProtocol() != jmri.LocoAddress.Protocol.MFX ) {
                // It is not possible to create MFX locomotives in the ECoS. They are auto-discovered.
                rosterEntry.addItem(r.titleString());
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddRosterEntryToEcos.class);

}

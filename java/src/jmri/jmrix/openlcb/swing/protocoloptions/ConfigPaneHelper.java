package jmri.jmrix.openlcb.swing.protocoloptions;

import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;

import jmri.jmrix.AbstractConnectionConfig;
import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.configurexml.ConnectionConfigXml;

/**
 * Helper class for implementing OpenLCB compatible port adapters. Common code for those adapters
 * is factored out into functions in this class.
 *
 * @author Balazs Racz, (C) 2018.
 */

public class ConfigPaneHelper {
    /**
     * Adds a button to the connection config setup panel that opens the openLCB protocol options.
     * Does nothing if the adapter is not being used in OpenLCB mode.
     *
     * Helper function in the ConnectionConfig.loadDetails() function for OpenLCB compatible
     * adapter configs. The loadDetails() function needs to be overridden with the following
     * code:
     * <code>
     *     public void loadDetails(JPanel details) {
     *         setInstance();
     *         ConfigPaneHelper.maybeAddOpenLCBProtocolOptionsButton(this, additionalItems);
     *         super.loadDetails(details);
     *     }
     * </code>
     *
     * @param connection the ConnectionConfig for the particular adapter calling.
     * @param additionalItems reference to the inherited additionalItems list from the
     *                        AbstractConnectionConfig. The helper funciton will modify it in
     *                        place.
     */
    public static void maybeAddOpenLCBProtocolOptionsButton(AbstractConnectionConfig connection, Collection<JComponent> additionalItems) {
        // Checks if we are using OpenLCB protocol.
        CanSystemConnectionMemo sc = ConnectionConfigXml.isOpenLCBProtocol(connection.getAdapter());
        if (sc == null) return;
        // Checks if the button already exists in the panel.
        String label = Bundle.getMessage("ProtocolOptionsButton");
        for (JComponent c : additionalItems) {
            if (!(c instanceof JButton)) {
                continue;
            }
            if (((JButton) c).getText().equals(label)) {
                return; // button already exists
            }
        }
        // Creates and adds the button.
        JButton b = new JButton(label);
        b.addActionListener(new ProtocolOptionsAction(sc));
        additionalItems.add(b);
    }
}

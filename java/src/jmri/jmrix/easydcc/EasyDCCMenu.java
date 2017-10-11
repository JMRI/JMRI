package jmri.jmrix.easydcc;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri EasyDCC-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class EasyDCCMenu extends JMenu {

    public EasyDCCMenu(String name) {

        this();
        setText(name);
    }

    public EasyDCCMenu() {

        super();
        setText(Bundle.getMessage("MenuEasyDCC"));

        add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction(Bundle.getMessage("MonitorXTitle", "EasyDCC")));
        add(new jmri.jmrix.easydcc.packetgen.EasyDccPacketGenAction(Bundle.getMessage("MenuItemSendCommand")));
    }

}

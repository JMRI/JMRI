package jmri.jmrix.xpa.swing;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri XPA-specific tools
 *
 * @author	Paul Bender Copyright 2004
 */
public class XpaMenu extends JMenu {

    public XpaMenu(String name,jmri.jmrix.xpa.XpaSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public XpaMenu(jmri.jmrix.xpa.XpaSystemConnectionMemo memo) {

        super();

        if (memo != null) {
           setText(memo.getUserName());
        } else {
           setText(Bundle.getMessage("MenuXpa"));
        }

        add(new jmri.jmrix.xpa.swing.xpamon.XpaMonAction());
        if (memo != null) {
            add(new jmri.jmrix.xpa.swing.packetgen.XpaPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.xpa.swing.xpaconfig.XpaConfigureAction(Bundle.getMessage("MenuItemXpaConfigTool"), memo));
        }
    }

}

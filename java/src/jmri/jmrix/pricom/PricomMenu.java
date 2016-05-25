// PricomMenu.java
package jmri.jmrix.pricom;

import javax.swing.JMenu;

/**
 * Create a "Pricom" menu containing the Jmri Pricom-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2005
 * @version $Revision$
 */
public class PricomMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 4327120523599989299L;

    public PricomMenu(String name) {
        this();
        setText(name);
    }

    public PricomMenu() {

        super();

        setText("PRICOM");  // Company name, not translated.

        add(new jmri.jmrix.pricom.pockettester.PocketTesterMenu());
        add(new jmri.jmrix.pricom.downloader.LoaderPanelAction());

    }

}

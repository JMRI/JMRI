package jmri.jmrix.pricom;

import javax.swing.JMenu;

/**
 * Create a "Pricom" menu containing the Jmri Pricom-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2005
 */
public class PricomMenu extends JMenu {

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

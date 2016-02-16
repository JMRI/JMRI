// LocoTools.java
package apps.LocoTools;

import apps.Apps;
import java.text.MessageFormat;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI program of LocoNet tools.
 * <P>
 * If an argument is provided at startup, it will be used as the name of the
 * configuration file. Note that this is just the name, not the path; the file
 * is searched for in the usual way, first in the preferences tree and then in
 * xml/
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
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
public class LocoTools extends Apps {

    /**
     *
     */
    private static final long serialVersionUID = -8658565137365487220L;

    LocoTools(JFrame p) {
        super(p);
    }

    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        // separate LocoNet menu
        menuBar.add(new jmri.jmrix.loconet.swing.LnComponentFactory(
                new jmri.jmrix.loconet.LocoNetSystemConnectionMemo(
                        jmri.jmrix.loconet.LnTrafficController.instance(),
                        new jmri.jmrix.loconet.SlotManager(jmri.jmrix.loconet.LnTrafficController.instance())
                )).getMenu());
    }

    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("LocoToolsVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.org/LocoTools ";
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.setStartupInfo("LocoTools");

        setConfigFilename("LocoToolsConfig2.xml", args);
        JFrame f = new JFrame("LocoTools");
        createFrame(new LocoTools(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    private final static Logger log = LoggerFactory.getLogger(LocoTools.class.getName());
}

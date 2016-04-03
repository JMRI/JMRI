/**
 * PacketPro.java
 */
package apps.PacketPro;

import apps.Apps;
import java.text.MessageFormat;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main program for the PacketProprogram based on JMRI.
 * <P>
 * PacketPro is a tool for sending NMRA DCC packets, intended to make testing of
 * DCC components easier. It's similar in intent to the NMRA PacketScript
 * program, though it uses a different scripting language.
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
 * @author	Bob Jacobsen Copyright 2002
 * @version $Revision$
 */
public class PacketPro extends Apps {

    PacketPro(JFrame p) {
        super(p);
    }

    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("PacketProVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.setStartupInfo("PacketPro");

        setConfigFilename("PacketProConfig2.xml", args);
        JFrame f = new JFrame("PacketPro");
        createFrame(new PacketPro(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    private final static Logger log = LoggerFactory.getLogger(PacketPro.class.getName());
}

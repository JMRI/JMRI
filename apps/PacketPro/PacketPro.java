/**
 * PacketPro.java
 */

package apps.PacketPro;

import apps.Apps;

import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * Main program for the PacketProprogram based on JMRI.
 * <P>
 * PacketPro is a tool for sending NMRA DCC packets, intended to make
 * testing of DCC components easier.  It's similar in intent to the
 * NMRA PacketScript program, though it uses a different scripting language.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2002
 * @version     $Revision: 1.2 $
 */
public class PacketPro extends Apps {

    PacketPro(JFrame p) {
        super(p);
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("PacketProVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("PacketPro"));

        setConfigFilename("PacketProConfig2.xml", args);
        JFrame f = new JFrame("PacketPro");
        createFrame(new PacketPro(f), f);

        log.info("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PacketPro.class.getName());
}



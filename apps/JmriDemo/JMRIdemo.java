// JMRIdemo.java

package apps.JmriDemo;

import apps.Apps;

import java.text.MessageFormat;

import javax.swing.JFrame;

/**
 * The JMRI demo program.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.61 $
 */
public class JMRIdemo extends Apps {

    JMRIdemo(JFrame p) {
        super(p);
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("JmriDemoVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("JMRIdemo"));

        setConfigFilename("JmriDemoConfig2.xml", args);
        JFrame f = new JFrame("JmriDemo");
        createFrame(new JMRIdemo(f), f);

        log.info("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}



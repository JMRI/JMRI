// JMRIdemo.java

package apps.JmriDemo;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import apps.*;
import javax.swing.*;

/**
 * The JMRI demo program
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.55 $
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
        SplashWindow sp = new SplashWindow();

        initLog4J();
        log.info("program starts");
        setConfigFilename("JmriDemoConfig.xml", args);
        JFrame f = new JFrame("JmriDemo");
        createFrame(new JMRIdemo(f), f);

        log.info("main initialization done");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}



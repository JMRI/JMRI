// PanelPro.java

package apps.PanelPro;

import apps.Apps;
import apps.SplashWindow;
import jmri.util.JmriJFrame;

import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * The JMRI program for creating control panels.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.15 $
 */
public class PanelPro extends Apps {

    PanelPro(JFrame p) {
        super(p);
    }

    protected String logo() {
        return "resources/PanelPro.gif";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("PanelProVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.sf.net/PanelPro.html ";
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("PanelPro"));

        setConfigFilename("PanelProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("PanelPro");
        createFrame(new PanelPro(f), f);

        log.info("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelPro.class.getName());
}



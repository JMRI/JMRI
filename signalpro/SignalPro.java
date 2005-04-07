// SignalPro.java

package signalpro;

import apps.Apps;
import apps.SplashWindow;

import signalpro.entrytable.EntryTableAction;

import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import java.util.ResourceBundle;

/**
 * The main SignalPro program
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003, 2004
 * @version     $Revision: 1.1.1.1 $
 */
public class SignalPro extends Apps {

    SignalPro(JFrame p) {
        super(p);
    }

    protected String logo() {
        return "resources/Digitrax.gif";
    }

    protected void setResourceBundle() {
        rb = ResourceBundle.getBundle("SignalPro.AppsBundle");
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("SignalProVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://www.digitrax.com";
    }

    protected void toolsMenu(JMenuBar menuBar, JFrame frame) {
        JMenu tools = new jmri.jmrit.ToolsMenu(rb.getString("MenuTools"));
        tools.add(new EntryTableAction("SE8f table"));
        tools.add(new jmri.jmrix.loconet.almbrowser.AlmBrowserAction("ALM Browser"));
        tools.add(new signalpro.mangler.ManglerPanelAction("Mangler"));
        menuBar.add(tools);
    }

    protected void setButtonSpace() {
        super.setButtonSpace();
        // add our favorite buttons
        buttonSpace().add(new JButton(new EntryTableAction("SE8f table")));
        buttonSpace().add(new JButton(new jmri.jmrix.loconet.almbrowser.AlmBrowserAction("ALM Browser")));
        buttonSpace().add(new JButton(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor")));
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("SignalPro"));

        setConfigFilename("SignalPro.xml", args);
        JFrame f = new JFrame("SignalPro");
        createFrame(new SignalPro(f), f);

        log.info("main initialization done");
        
        // now create the dummy for signals
        //new jmri.jmrix.loconet.Se8AlmImplementation(2, false); // false means implements on LocoNet

        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalPro.class.getName());
}



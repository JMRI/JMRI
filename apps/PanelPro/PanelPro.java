// PanelPro.java

package apps.PanelPro;

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
 * The JMRI program for creating control panels
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.4 $
 */
public class PanelPro extends JPanel {
    public PanelPro(JFrame frame) {

        super(true);
        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

	// create basic GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a menu bar
        menuBar = new JMenuBar();

        // load preference file
        if (configFilename != null) {
            log.debug("configure from specified file "+configFilename);
        } else {
            configFilename = "jmriprefs.xml";
            log.debug("configure from default file "+configFilename);
        }
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(XmlFile.prefsDir()+configFilename);
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
        boolean configOK = InstanceManager.configureManagerInstance().load(file);

	// populate GUI

        // Create menu categories and add to the menu bar, add actions to menus
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(frame));
        fileMenu.add(new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        prefsFrame = new JFrame(rb.getString("MenuItemPreferences"));
        prefsFrame.getContentPane().setLayout(new BoxLayout(prefsFrame.getContentPane(), BoxLayout.X_AXIS));
        PanelProConfigPanel prefs = new PanelProConfigPanel(configFilename);
        prefsFrame.getContentPane().add(prefs);
        prefsFrame.pack();
        AbstractAction prefsAction = new AbstractAction(rb.getString("MenuItemPreferences")) {
            public void actionPerformed(ActionEvent e) {
                prefsFrame.show();
            }
        };

        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(prefsAction); // argument is filename, not action name

        // if the configuration didn't complete OK, pop the prefs frame
        log.debug("Config go OK? "+configOK);
        if (!configOK) prefsAction.actionPerformed(null);

        menuBar.add(new jmri.jmrit.ToolsMenu(rb.getString("MenuTools")));

        menuBar.add(new jmri.jmrit.roster.RosterMenu(rb.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        menuBar.add(new jmri.jmrit.display.PanelMenu());

        menuBar.add(new jmri.jmrix.SystemsMenu());

        menuBar.add(new jmri.jmrit.DebugMenu(this));

        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction( "Sample automaton 1"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
        devMenu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython automaton"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));

         // Label & text
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/logo.gif"),"JMRI logo"), JLabel.LEFT));

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("PanelProVersionCredit"),
                                new String[]{jmri.Version.name()}
                            )));
        pane2.add(new JLabel("http://jmri.sf.net/PanelPro "));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection1(), prefs.getPort1()}
                            )));

        if (!prefs.getConnection2().equals("(none)")) {
            pane2.add(new JLabel(" "));
            pane2.add(new JLabel(MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection2(), prefs.getPort2()}
                            )));
        }

        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("JavaVersionCredit"),
                                new String[]{System.getProperty("java.version","<unknown>"),
                                            Locale.getDefault().toString()}
                            )));
        pane1.add(pane2);
        add(pane1);

    }

    boolean configOK;
    JFrame prefsFrame;

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        SplashWindow sp = new SplashWindow();

        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("program starts");

        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFilename = args[0];
            log.debug("Config file was specified as: "+configFilename);
        }

    	// create the demo frame and menus
        JFrame frame = new JFrame("PanelPro main panel");
        PanelPro containedPane = new PanelPro(frame);
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);

        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
        frame.setVisible(true);

        log.info("main initialization done");
    }

    static String configFilename = null;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelPro.class.getName());
}



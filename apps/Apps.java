// Apps.java

package apps;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Base class for Jmri Apps
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.4 $
 */
public class Apps extends JPanel {

    protected void fileMenu(JMenuBar menuBar, JFrame frame) {
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(frame));
        fileMenu.add(new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
    }

    protected AppConfigPanel newPrefs() {
        return new AppConfigPanel(configFilename, 2);
    }

    protected void editMenu(JMenuBar menuBar, JFrame frame) {
        prefsFrame = new JFrame(rb.getString("MenuItemPreferences"));
        prefsFrame.getContentPane().setLayout(new BoxLayout(prefsFrame.getContentPane(), BoxLayout.X_AXIS));
        prefs = newPrefs();
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
    }

    protected void toolsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.ToolsMenu(rb.getString("MenuTools")));
    }

    protected void rosterMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rb.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, this));
    }

    protected void panelMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.display.PanelMenu());
    }

    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrix.SystemsMenu());
    }

    protected void debugMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.DebugMenu(this));
    }

    protected void developmentMenu(JMenuBar menuBar, JFrame frame) {
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
    }

    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        rosterMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        debugMenu(menuBar, frame);
        developmentMenu(menuBar, frame);
    }

    public Apps(JFrame frame) {

        super(true);
        rb = ResourceBundle.getBundle("apps.AppsBundle");

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
        configOK = InstanceManager.configureManagerInstance().load(file);

	// populate GUI

        // Create menu categories and add to the menu bar, add actions to menus
        createMenus(menuBar, frame);

        add(statusPanel());

    }

    protected String line1() {
        return MessageFormat.format(rb.getString("DefaultVersionCredit"),
                                new String[]{jmri.Version.name()});
    }
    protected String line2() {
        return "http://jmri.sf.net/ ";
    }
    protected String line3() {
        return " ";
    }
    protected String line4() {
        return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection1(), prefs.getPort1()});
    }
    protected String line6() {
        return " ";
    }
    protected String line7() {
        return MessageFormat.format(rb.getString("JavaVersionCredit"),
                                new String[]{System.getProperty("java.version","<unknown>"),
                                            Locale.getDefault().toString()});
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    /**
     * Fill in the logo and status panel
     * @return Properly-filled out JPanel
     */
    protected JPanel statusPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource(logo()),"JMRI logo"), JLabel.LEFT));

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(line1()));
        pane2.add(new JLabel(line2()));
        pane2.add(new JLabel(line3()));
        pane2.add(new JLabel(line4()));

        if (!prefs.getConnection2().equals("(none)")) {
            pane2.add(new JLabel(" "));
            pane2.add(new JLabel(MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection2(), prefs.getPort2()}
                            )));
        }

        pane2.add(new JLabel(line6()));
        pane2.add(new JLabel(line7()));
        pane1.add(pane2);
        return pane1;
    }

    protected JFrame prefsFrame;
    protected ResourceBundle rb;
    protected AppConfigPanel prefs;

    static SplashWindow sp = null;
    static protected void splash(boolean show) {
        if (sp==null) sp = new SplashWindow();
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
            sp = null;
        }
    }

    static protected void initLog4J() {
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
    }

    static protected void setConfigFilename(String def, String args[]) {
        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFilename = args[0];
            log.debug("Config file was specified as: "+configFilename);
        } else{
            configFilename = def;
        }
    }


    static protected void createFrame(Apps containedPane, JFrame frame) {
    	// create the main frame and menus
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());

        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);

        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
        frame.setVisible(true);
    }

    static protected void loadFile(String name){
        File pFile = InstanceManager.configureManagerInstance().find(name);
        if (pFile!=null)
            InstanceManager.configureManagerInstance().load(pFile);
        else
            log.warn("Could not find "+name+" config file");

    }

    static protected String configFilename = null;
    static protected boolean configOK;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Apps.class.getName());
}



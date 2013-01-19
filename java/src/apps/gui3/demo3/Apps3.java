// Apps3.java

package apps.gui3.demo3;

import jmri.*;
import jmri.util.JmriJFrame;

import apps.GuiLafConfigPane;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import jmri.util.FileUtil;

/**
 * Base class for GUI3 JMRI applications.
 * <p>
 * This is a complete re-implementation of the 
 * apps.Apps support for JMRI applications.
 * <p>
 * Each using application provides it's own main() method.
 * See e.g. apps.gui3.demo3.Demo3 for an example.
 * <p>
 * There are a large number of missing features marked with TODO in comments
 * including code from the earlier implementation.
 * <P>
 * @author	Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class Apps3 {

    /**
     * Initial actions before 
     * frame is created.
     */
    static public void preInit() {
        // TODO Launch splash screen: splash(true)

        jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo("Demo3"));

        // TODO setConfigFilename("Demo3Config3.xml", args)
    }

    protected String configFilename = null;
    boolean configOK;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3() {
        // pre-GUI work
        installConfigurationManager();
        log.info("need to set up the new load-file error handling");
        try {
            loadPreferenceFile();
        } catch (JmriException e) {
            log.error("Error loading preferences: "+e);
        }
        installShutDownManager();
        addDefaultShutDownTasks();
        
        // create GUI
        createMainFrame();
        addMainToolBar();
        addMainMenuBar();
        addASampleFrame();
        displayMainFrame();
    }
    
    
    
    JmriJFrame      mainFrame;
    JDesktopPane    desktop;
    
    protected void createMainFrame() {
        mainFrame = new JmriJFrame("dummy name");
        desktop = new JDesktopPane();
        
        mainFrame.getContentPane().add(desktop, BorderLayout.CENTER);
    }
    
    protected void addMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // add some samples to show
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createTablesMenu());
        menuBar.add(createToolsMenu());
        menuBar.add(createHardwareMenu());
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
        
        // TODO: needs the Mac code from Apps.createMenus()
        
        mainFrame.setJMenuBar(menuBar);
    }
    
    protected JMenu createFileMenu() {
        JMenu r = new JMenu("File");
        r.add(new JMenuItem("New ..."));
        r.add(new JMenuItem("Open ..."));
        r.add(new JMenuItem("Loco Roster ..."));
        r.add(new JMenuItem("Close"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Save"));
        r.add(new JMenuItem("Save as ..."));
        r.add(new JMenuItem("Export ..."));
        r.add(new JMenuItem("Import ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Run Script ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Print Setup ..."));
        r.add(new JMenuItem("Print Preview ..."));
        r.add(new JMenuItem("Print ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Exit"));
        return r;
    }
    
    protected JMenu createEditMenu() {
        JMenu r = new JMenu("Edit");
        r.add(new JMenuItem("Undo"));
        r.add(new JMenuItem("Repeat"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Cut"));
        r.add(new JMenuItem("Copy"));
        r.add(new JMenuItem("Paste"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Preferences ..."));
        return r;
    }
    
    protected JMenu createViewMenu() {
        JMenu r = new JMenu("View");
        r.add(new JMenuItem("Clock"));
        r.add(new JMenuItem("Command Toolbar"));
        r.add(new JMenuItem("Startup Panel"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Simple/adv Roster"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Script Input"));
        r.add(new JMenuItem("Script Output"));
        return r;
    }
    
    protected JMenu createTablesMenu() {
        JMenu r = new JMenu("Tables");
        r.add(new JMenuItem("Turnouts"));
        r.add(new JMenuItem("Sensors"));
        r.add(new JMenuItem("Lights"));
        r.add(new JMenuItem("Signal Heads"));
        r.add(new JMenuItem("Reporters"));
        r.add(new JMenuItem("Memory Variables"));
        r.add(new JMenuItem("Routes"));
        r.add(new JMenuItem("LRoutes"));
        r.add(new JMenuItem("Logix"));
        r.add(new JMenuItem("Blocks"));
        r.add(new JMenuItem("Sections"));
        r.add(new JMenuItem("Transits"));
        r.add(new JMenuItem("Audio"));
        return r;
    }
    
    protected JMenu createToolsMenu() {
        JMenu r = new JMenu("Tools");
        r.add(new JMenuItem("Single CV Programmer"));
        r.add(new JMenuItem("Power Control"));
        r.add(new JMenuItem("Turnout Control"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Simple Signal Logic ..."));
        r.add(new JMenuItem("Sensor Groups ..."));
        r.add(new JMenuItem("Speedometer ..."));
        r.add(new JMenuItem("Light Control"));
        r.add(new JMenuItem("Dispatcher"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Send DCC Packet"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Operations ..."));
        r.add(new JMenuItem("USS CTC Tools ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Preferences ..."));
        return r;
    }
    
    protected JMenu createHardwareMenu() {
        JMenu r = new JMenu("Hardware");
        return r;
    }
    
    protected JMenu createWindowMenu() {
        JMenu r = new JMenu("Window");
        return r;
    }
    
    protected JMenu createHelpMenu() {
        JMenu r = new JMenu("Help");
        return r;
    }
    
    protected void installConfigurationManager() {
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
        log.debug("start load config file");
    }

    protected void loadPreferenceFile() throws JmriException {
        if (configFilename != null) {
            log.debug("configure from specified file "+configFilename);
        } else {
            configFilename = "jmriprefs3.xml";
            log.debug("configure from default file "+configFilename);
        }
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        File file = new File(configFilename);
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath()+configFilename);
        }
        try {
            configOK = InstanceManager.configureManagerInstance().load(file);
        } catch (jmri.JmriException e) {
            configOK = false;
            throw e;
        }
        log.debug("end load config file, OK="+configOK);
    }
    
    protected void installShutDownManager() {
        InstanceManager.setShutDownManager(
                new jmri.managers.DefaultShutDownManager());
    }

    protected void addDefaultShutDownTasks() {
        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
            register(new jmri.implementation.AbstractShutDownTask("Writing Blocks"){
                public boolean execute() {
                    // Save block values prior to exit, if necessary
                    log.debug("Start writing block info");
                    try {
                        new jmri.jmrit.display.layoutEditor.BlockValueFile().writeBlockValues();
                    } 
                    //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }                           
                    catch (java.io.IOException ioe) { log.error("Exception writing blocks: "+ioe); }   
                    
                    // continue shutdown   
                    return true;
                }
            });
    }
    
    
    protected void addMainToolBar() {
        JToolBar toolBar = new JToolBar("(Close to dock)", JToolBar.VERTICAL);
        
        toolBar.add(new JButton(new AbstractAction("Preferences"){
                public void actionPerformed(ActionEvent e) {
                                        addPreferencesFrame();
                }
            }));

        toolBar.add(new JButton(new AbstractAction("sample button 2"){
                public void actionPerformed(ActionEvent e) {
                                        addASampleFrame();
                }
            }));

        toolBar.add(new JButton(new AbstractAction("sample button 3"){
                public void actionPerformed(ActionEvent e) {
                                        addASampleFrame();
                }
            }));
        
        // this takes up space down the left side until made floating
        mainFrame.getContentPane().add(toolBar, BorderLayout.EAST);
        //desktop.add(toolBar);
    }
    
    /**
     * Set a toolbar to be initially floating.
     * This doesn't quite work right.
     */
    protected void setFloating(JToolBar toolBar) {
        //((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloatingLocation(100,100);
        ((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloating(true, new Point(500,500));
    }
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    protected void addPreferencesFrame() {
        JInternalFrame frame = new JInternalFrame("Preferences",
                                            true, //resizable
                                            true, //closable
                                            true, //maximizable
                                            true);//iconifiable
        frame.setLocation(200+50*count,200+50*count);
        count++;
        
        // content
        JTabbedPane p = new JTabbedPane();
        
        p.add("Connection 1", jmri.jmrix.JmrixConfigPane.instance(1));
        p.add("GUI", guiPrefs = new GuiLafConfigPane());
        p.add("Programmer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p.add("Actions", new apps.PerformActionPanel());
        p.add("Buttons", new apps.CreateButtonPanel());
        p.add("Files", new apps.PerformFilePanel());
        p.add("Scripts", new apps.PerformScriptPanel());
        p.add("Roster", new jmri.jmrit.roster.RosterConfigPane());

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(p);
        JButton save = new JButton("Save");
        p2.add(save);
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    preferencesSavePressed();
                }
            });

        frame.add(p2);
        frame.pack();
        
        // show
        frame.setVisible(true);
        desktop.add(frame);

        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}

        frame.moveToFront();
    }
    
    GuiLafConfigPane guiPrefs;
    
    void preferencesSavePressed() {
        saveContents();
    }
    
    void saveContents(){
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        InstanceManager.configureManagerInstance().registerPref(guiPrefs);

        // write file
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        // decide whether name is absolute or relative
        File file = new File(configFilename);
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath()+configFilename);
        }

        InstanceManager.configureManagerInstance().storePrefs(file);
    }
    
    static int count = 0;
    protected void addASampleFrame() {
        JInternalFrame sample = new JInternalFrame("sample internal frame",
                                            true, //resizable
                                            true, //closable
                                            true, //maximizable
                                            true);//iconifiable
        sample.setLocation(200+50*count,200+50*count);
        count++;

        // content
        sample.setSize(300,200);

        // show
        sample.setVisible(true);
        desktop.add(sample);

        try {
            sample.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}

        sample.moveToFront();
    }
    
    protected void displayMainFrame() {
        mainFrame.setSize(mainFrame.getMaximumSize());
        mainFrame.setVisible(true);
    }
    
    /**
     * Final actions before releasing control of app to user
     */
    protected void postInit() {
        log.debug("main initialization done");
        // TODO: splash(false);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Apps3.class.getName());
    
}



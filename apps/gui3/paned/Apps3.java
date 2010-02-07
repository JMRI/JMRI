// Apps3.java

package apps.gui3.paned;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;
import jmri.util.swing.multipane.MultiPaneWindow;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

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
 * @version $Revision: 1.6 $
 */
public class Apps3 {

    /**
     * Initial actions before 
     * frame is created.
     */
    static public void preInit() {
        // TODO Launch splash screen: splash(true)

        jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo("Gui3IDE"));

        // TODO setConfigFilename("Demo3Config3.xml", args)
    }

    protected String nameString = "JMRI GUI3 Demo";
    protected String configFilename = "jmriprefs3.xml";  // this appears multiple places, needs rationalization
    boolean configOK;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3() {
        // pre-GUI work
        installConfigurationManager();
        installShutDownManager();
        addDefaultShutDownTasks();
        installManagers();
        initializeHelpSystem();
        loadPreferenceFile();
        
        // create test dummy objects
        createDemoScaffolding();
        
        // create GUI
        createMainFrame();
        
        // set to min size for demo
        displayMainFrame( new Dimension(800, 600));  // or mainFrame.getMaximumSize()
    }
        
    MultiPaneWindow mainFrame;
    
    protected void initializeHelpSystem() {
        try {

            // initialize system and check for success
            boolean ok = jmri.util.HelpUtil.initOK();
            
            // tell help to use default browser for external types
            javax.help.SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
    
            // help items are set in the various Tree/Menu/Toolbar constructors        
        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }
    }
    
    protected void createDemoScaffolding() {
        jmri.managers.InternalSensorManager m = new jmri.managers.InternalSensorManager();
        InstanceManager.setSensorManager(m);
        InstanceManager.sensorManagerInstance().provideSensor("IS1");
        InstanceManager.sensorManagerInstance().provideSensor("IS2");
        InstanceManager.sensorManagerInstance().provideSensor("IS3");
    }

    protected JComponent getSensorTableDemo() {
        // put a table in rightTop
        jmri.jmrit.beantable.BeanTableDataModel dataModel = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
        jmri.util.com.sun.TableSorter sorter = new jmri.util.com.sun.TableSorter(dataModel);
    	JTable dataTable = new JTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());        
        JScrollPane dataScroll	= new JScrollPane(dataTable);
        return dataScroll;
    }

    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new MultiPaneWindow(nameString, "apps/demo");
    }
    
    protected void installConfigurationManager() {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        InstanceManager.setConfigureManager(cm);
        log.debug("config manager installed");
        // Install Config Manager error handler
        cm.setErrorHandler(new jmri.configurexml.swing.DialogErrorHandler());

    }
    
    protected void installManagers() {
        // Install a history manager
        jmri.InstanceManager.store(new jmri.jmrit.revhistory.FileHistory(), jmri.jmrit.revhistory.FileHistory.class);
        // record startup
        jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory.class).addOperation("app", nameString, null);
        
        // Install a user preferences manager
        jmri.InstanceManager.store(new jmri.managers.DefaultUserMessagePreferences(), jmri.UserPreferencesManager.class);        
    }

    protected void loadPreferenceFile() {
        if (configFilename != null) {
            log.debug("configure from specified file "+configFilename);
        } else {
            configFilename = "jmriprefs3.xml";
            log.debug("configure from default file "+configFilename);
        }
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(configFilename);
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }
        try {
            configOK = InstanceManager.configureManagerInstance().load(file);
            log.debug("end load config file, OK="+configOK);
        } catch (Exception e) {
            configOK = false;
        }
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
        JPanel frame = new JPanel();
        frame.setLocation(200,200);
        
        // content
        JTabbedPane p = new JTabbedPane();
        
        p.add("Connection 1", jmri.jmrix.JmrixConfigPane.instance(1));
        p.add("GUI", guiPrefs = new jmri.GuiLafConfigPane());
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
        
        // show
        frame.setVisible(true);
    }
    
    jmri.GuiLafConfigPane guiPrefs;
    
    void preferencesSavePressed() {
        saveContents();
    }
    
    void saveContents(){
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        InstanceManager.configureManagerInstance().registerPref(guiPrefs);

        // write file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        // decide whether name is absolute or relative
        File file = new File(configFilename);
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }

        InstanceManager.configureManagerInstance().storePrefs();
    }
    
    
    protected void displayMainFrame(Dimension d) {
        mainFrame.setSize(d);
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



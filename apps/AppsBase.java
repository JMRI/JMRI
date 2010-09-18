// AppsBase.java

package apps;

import jmri.*;
import jmri.jmrit.XmlFile;

import java.io.File;
import javax.swing.*;

/**
 * Base class for the core of JMRI applications.
 * <p>
 * This provides a non-GUI base for applications.
 * Below this is the {@link apps.gui3.Apps3} class
 * which provides basic Swing GUI support.
 * <p>
 * For an example of using this, see
 * {@link apps.FacelessApp} and comments therein.
 * <p>
 * There are a series of steps in the configuration:
 * <dl>
 * <dt>preInit<dd>Initialize log4j, invoked from the main()
 * <dt>ctor<dd>
 * </dl>
 * <P>
 * @author	Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision: 1.7 $
 */
public abstract class AppsBase {

    /**
     * Initial actions before 
     * frame is created, invoked in the 
     * applications main() routine.
     */
    static public void preInit() {
        // TODO Launch splash screen: splash(true)

        jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo("Gui3IDE"));

    }

    protected static String nameString = "JMRI Base";
    protected static final String configFilename = "JmriConfig3.xml";
    boolean configOK;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public AppsBase() {

        installConfigurationManager();

        installShutDownManager();

        addDefaultShutDownTasks();

        installManagers();

        setAndLoadPreferenceFile();

    }
        
    protected void createDemoScaffolding() {
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
    
    protected void installConfigurationManager() {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        InstanceManager.setConfigureManager(cm);
        log.debug("config manager installed");
        // Install Config Manager error handler
        jmri.configurexml.ConfigXmlManager.setErrorHandler(new jmri.configurexml.swing.DialogErrorHandler());

    }
    
    protected void installManagers() {
        // Install a history manager
        jmri.InstanceManager.store(new jmri.jmrit.revhistory.FileHistory(), jmri.jmrit.revhistory.FileHistory.class);
        // record startup
        jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory.class).addOperation("app", nameString, null);
        
        // Install a user preferences manager
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);        
    }

    protected void setAndLoadPreferenceFile() {
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(configFilename);
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            configOK = false;
            log.info("No pre-existing preferences settings");
            return;
        }
        try {
            ((jmri.configurexml.ConfigXmlManager)InstanceManager.configureManagerInstance())
                                .setPrefsLocation(file);
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
     * Final actions before releasing control of app to user,
     * invoked explicitly after object has been constructed,
     * e.g. in main().
     */
    protected void postInit() {
        log.debug("main initialization done");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppsBase.class.getName());
}


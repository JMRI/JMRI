// AppsBase.java

package apps;

import jmri.*;
import jmri.jmrit.XmlFile;

import java.io.File;
import javax.swing.*;
import java.util.Enumeration;

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
 * @version $Revision$
 */
public abstract class AppsBase {

    /**
     * Initial actions before 
     * frame is created, invoked in the 
     * applications main() routine.
     */
    static public void preInit() {
        if (!log4JSetUp) initLog4J();
        
        //jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo("Gui3IDE"));
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_PKGPROTECT",
                                    justification="not a library pattern")
    protected final static String nameString = "JMRI Base";

    private final static String configFilename = "/JmriConfig3.xml";
    boolean configOK;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public AppsBase() {
        
        if (!log4JSetUp) initLog4J();
        
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
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        InstanceManager.setConfigureManager(cm);
        cm.setPrefsLocation(new File(getConfigFileName()));
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
        File file = new File(getConfigFileName());
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+ getConfigFileName());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            configOK = false;
            log.info("No pre-existing preferences settings");
            ((jmri.configurexml.ConfigXmlManager)InstanceManager.configureManagerInstance())
                    .setPrefsLocation(file);
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
    
    /**
     * Set up the configuration file name at startup.
     * <P>
     * The Configuration File name variable holds the name 
     * used to load the configuration file during later startup
     * processing.  Applications invoke this method 
     * to handle the usual startup hierarchy:
     *<UL>
     *<LI>If an absolute filename was provided on the command line, use it
     *<LI>If a filename was provided that's not absolute, consider it to
     *    be in the preferences directory
     *<LI>If no filename provided, use a default name (that's application
     *    specific)
     *</UL>
     *This name will be used for reading and writing the preferences. It
     * need not exist when the program first starts up.
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String args[]) {
        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            setJmriSystemProperty("configFilename", args[0]);
            log.debug("Config file was specified as: "+args[0]);
        } else{
            setJmriSystemProperty("configFilename", def);
        }
    }
    
    // We will use the value stored in the system property 
    static public String getConfigFileName(){
        if(System.getProperty("org.jmri.Apps.configFilename")!=null)
            return System.getProperty("org.jmri.Apps.configFilename");
        return configFilename;
    }
    
    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps."+key);
            if ( current == null){
                System.setProperty("org.jmri.Apps."+key, value);
            }else if (!current.equals(value))
                log.warn("JMRI property "+key+" already set to "+current+
                        ", skipping reset to "+value);
        } catch (Exception e) {
            log.error("Unable to set JMRI property "+key+" to "+value+
                        "due to exception: "+e);
        }
    }
    
    static boolean log4JSetUp = false;
    
    static protected void initLog4J() {
    	if (log4JSetUp){
    		log.debug("initLog4J already initialized!");
    		return;
    	}
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set-up and usable by the ConsoleAppender
        SystemConsole.init();

        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
            }
        }
        catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
    
        // first log entry
    	log.info(jmriLog);

        // now indicate logging locations
        @SuppressWarnings("unchecked")
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
       
        while ( e.hasMoreElements() ) {
            org.apache.log4j.Appender a = (org.apache.log4j.Appender)e.nextElement();
            if ( a instanceof org.apache.log4j.RollingFileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.RollingFileAppender)a).getFile());
            }
            else if ( a instanceof org.apache.log4j.FileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.FileAppender)a).getFile());
            }
        }
    }
    
    private static final String jmriLog ="****** JMRI log *******";
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppsBase.class.getName());
}


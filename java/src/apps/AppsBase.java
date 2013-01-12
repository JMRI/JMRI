// AppsBase.java
package apps;

import apps.gui3.TabbedPreferences;
import java.io.File;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandleManager;
import jmri.UserPreferencesManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.XmlFile;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultShutDownManager;
import jmri.managers.DefaultUserMessagePreferences;
import jmri.util.FileUtil;
import jmri.util.Log4JUtil;
import jmri.util.PythonInterp;
import jmri.util.exceptionhandler.AwtHandler;
import jmri.util.exceptionhandler.UncaughtExceptionHandler;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;

/**
 * Base class for the core of JMRI applications. <p> This provides a non-GUI
 * base for applications. Below this is the {@link apps.gui3.Apps3} class which
 * provides basic Swing GUI support. <p> For an example of using this, see
 * {@link apps.FacelessApp} and comments therein. <p> There are a series of
 * steps in the configuration: <dl> <dt>preInit<dd>Initialize log4j, invoked
 * from the main() <dt>ctor<dd> </dl> <P>
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 * @version $Revision$
 */
public abstract class AppsBase {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_PKGPROTECT",
    justification = "not a library pattern")
    private final static String configFilename = "/JmriConfig3.xml";
    protected boolean configOK;
    protected boolean configDeferredLoadOK;
    protected boolean preferenceFileExists;
    static boolean log4JSetUp = false;
    static boolean preInit = false;
    static Logger log = Logger.getLogger(AppsBase.class.getName());

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
     */
    static public void preInit(String applicationName) {
        if (!log4JSetUp) {
            initLog4J();
        }

        try {
            Application.setApplicationName(applicationName);
        } catch (IllegalAccessException ex) {
            log.error("Unable to set application name");
        } catch (IllegalArgumentException ex) {
            log.error("Unable to set application name");
        }

        //jmri.util.Log4JUtil.initLog4J();
        log.info(Log4JUtil.startupInfo(applicationName));

        preInit = true;
    }

    /**
     * Create and initialize the application object.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SC_START_IN_CTOR",
    justification = "The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public AppsBase(String applicationName, String configFileDef, String[] args) {

        if (!preInit) {
            preInit(applicationName);
            setConfigFilename(configFileDef, args);
        }
        
        if (!log4JSetUp) {
            initLog4J();
        }

        installConfigurationManager();

        installShutDownManager();

        addDefaultShutDownTasks();

        installManagers();

        setAndLoadPreferenceFile();

        FileUtil.logFilePaths();
        
        Runnable r;
        /*
         * Once all the preferences have been loaded we can initial the
         * preferences doing it in a thread at this stage means we can let it
         * work in the background if the file doesn't exist then we do not
         * initialize it
         */
        if (preferenceFileExists) {
            r = new Runnable() {

                public void run() {
                    try {
                        InstanceManager.tabbedPreferencesInstance().init();
                    } catch (Exception ex) {
                        log.error(ex.toString());
                    }
                }
            };
            Thread thr = new Thread(r);
            thr.start();
        }

        r = new Runnable() {

            public void run() {
                try {
                    PythonInterp.getPythonInterpreter();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize python interpreter " + ex.toString());
                }
            }
        };
        Thread thr2 = new Thread(r, "initialize python interpreter");
        thr2.start();
    }

    protected void installConfigurationManager() {
        ConfigXmlManager cm = new ConfigXmlManager();
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        InstanceManager.setConfigureManager(cm);
        cm.setPrefsLocation(new File(getConfigFileName()));
        log.debug("config manager installed");
        // Install Config Manager error handler
        ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
    }

    protected void installManagers() {
        // Install a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", Application.getApplicationName(), null);

        // Install a user preferences manager
        InstanceManager.store(DefaultUserMessagePreferences.getInstance(), UserPreferencesManager.class);

        // install the abstract action model that allows items to be added to the, both 
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);

        // install preference manager
        InstanceManager.setTabbedPreferences(new TabbedPreferences());
        
        // install the named bean handler
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);

        // Install an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);

        //Install Entry Exit Pairs Manager
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);

    }

    protected void setAndLoadPreferenceFile() {
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        final File file;
        // decide whether name is absolute or relative
        if (!new File(getConfigFileName()).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir() + getConfigFileName());
        } else {
            file = new File(getConfigFileName());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            preferenceFileExists = false;
            configOK = false;
            log.info("No pre-existing config file found, searched for '" + file.getPath() + "'");
            ((ConfigXmlManager) InstanceManager.configureManagerInstance()).setPrefsLocation(file);
            return;
        }
        preferenceFileExists = true;
        try {
            ((ConfigXmlManager) InstanceManager.configureManagerInstance()).setPrefsLocation(file);
            configOK = InstanceManager.configureManagerInstance().load(file);
            if (log.isDebugEnabled()) log.debug("end load config file "+ file.getName() +", OK=" + configOK);
        } catch (Exception e) {
            configOK = false;
        }

        // To avoid possible locks, deferred load should be
        // performed on the Swing thread
        if (SwingUtilities.isEventDispatchThread()) {
            configDeferredLoadOK = doDeferredLoad(file);
        } else {
            try {
                // Use invokeAndWait method as we don't want to
                // return until deferred load is completed
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        configDeferredLoadOK = doDeferredLoad(file);
                    }
                });
            } catch (Exception ex) {
                log.error("Exception creating system console frame: " + ex);
            }
        }
    }

    //abstract protected void addToActionModel();
    private boolean doDeferredLoad(File file) {
        boolean result;
        if (log.isDebugEnabled()) log.debug("start deferred load from config file " + file.getName());
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration: " + e);
            result = false;
        }
        if (log.isDebugEnabled()) log.debug("end deferred load from config file "+ file.getName() +", OK=" + result);
        return result;
    }

    protected void installShutDownManager() {
        InstanceManager.setShutDownManager(
                new DefaultShutDownManager());
    }

    protected void addDefaultShutDownTasks() {
        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
                register(new AbstractShutDownTask("Writing Blocks") {

            public boolean execute() {
                // Save block values prior to exit, if necessary
                log.debug("Start writing block info");
                try {
                    new BlockValueFile().writeBlockValues();
                } //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }
                catch (java.io.IOException ioe) {
                    log.error("Exception writing blocks: " + ioe);
                }

                // continue shutdown
                return true;
            }
        });
    }

    /**
     * Final actions before releasing control of app to user, invoked explicitly
     * after object has been constructed, e.g. in main().
     */
    protected void start() {
        log.debug("main initialization done");
    }

    /**
     * Set up the configuration file name at startup. <P> The Configuration File
     * name variable holds the name used to load the configuration file during
     * later startup processing. Applications invoke this method to handle the
     * usual startup hierarchy: <UL> <LI>If an absolute filename was provided on
     * the command line, use it <LI>If a filename was provided that's not
     * absolute, consider it to be in the preferences directory <LI>If no
     * filename provided, use a default name (that's application specific) </UL>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em>.
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
        // save the configuration filename if present on the command line

        if (args.length >= 1 && args[0] != null && !args[0].equals("") && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: " + args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: " + arg);
            }
        }
        if (def != null) {
            setJmriSystemProperty("configFilename", def);
            log.debug("Config file set to: " + def);
        }
    }

    // We will use the value stored in the system property 
    static public String getConfigFileName() {
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return System.getProperty("org.jmri.Apps.configFilename");
        }
        return configFilename;
    }

    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps." + key);
            if (current == null) {
                System.setProperty("org.jmri.Apps." + key, value);
            } else if (!current.equals(value)) {
                log.warn("JMRI property " + key + " already set to " + current
                        + ", skipping reset to " + value);
            }
        } catch (Exception e) {
            log.error("Unable to set JMRI property " + key + " to " + value
                    + "due to exception: " + e);
        }
    }

    static protected void initLog4J() {
        if (log4JSetUp) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set-up and usable by the ConsoleAppender
        SystemConsole.create();

        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        // if it can be found:
        // first look in program launch directory
        // second look in JMRI distribution directory
        String logFile = "default.lcf"; // NOI18N
        try {
            if (new File(logFile).canRead()) {
                PropertyConfigurator.configure(logFile);
            } else if (new File(FileUtil.getProgramPath() + logFile).canRead()) {
                PropertyConfigurator.configure(FileUtil.getProgramPath() + logFile);
            } else {
                BasicConfigurator.configure();
                Logger.getRootLogger().setLevel(Level.WARN);
            }
        } catch (java.lang.NoSuchMethodError e) {
            log.error("Exception starting logging: " + e);
        }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", AwtHandler.class.getName()); // NOI18N
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        // first log entry
        //log.info(jmriLog);

        // now indicate logging locations
        @SuppressWarnings("unchecked")
        Enumeration<Logger> e = Logger.getRootLogger().getAllAppenders();

        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (a instanceof RollingFileAppender) {
                log.info("This log is stored in file: " + ((RollingFileAppender) a).getFile());
            } else if (a instanceof FileAppender) {
                log.info("This log is stored in file: " + ((FileAppender) a).getFile());
            }
        }
    }

    /**
     * The application decided to quit, handle that.
     */
    static public Boolean handleQuit() {
        log.debug("Start handleQuit");
        try {
            return InstanceManager.shutDownManagerInstance().shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit", e);
        }
        return false;
    }

    /**
     * The application decided to restart, handle that.
     */
    static public Boolean handleRestart() {
        log.debug("Start handleRestart");
        try {
            return InstanceManager.shutDownManagerInstance().restart();
        } catch (Exception e) {
            log.error("Continuing after error in handleRestart", e);
        }
        return false;
    }
}

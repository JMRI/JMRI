// AppsBase.java
package apps;

import apps.gui3.TabbedPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandleManager;
import jmri.UserPreferencesManager;
import jmri.implementation.AbstractShutDownTask;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultShutDownManager;
import jmri.managers.JmriUserPreferencesManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;
import jmri.util.Log4JUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the core of JMRI applications.
 * <p>
 * This provides a non-GUI base for applications. Below this is the
 * {@link apps.gui3.Apps3} subclass which provides basic Swing GUI support.
 * <p>
 * There are a series of steps in the configuration:
 * <dl>
 * <dt>preInit<dd>Initialize log4j, invoked from the main()
 * <dt>ctor<dd>
 * </dl>
 * <P>
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 * @version $Revision$
 */
public abstract class AppsBase {

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "not a library pattern")
    private final static String configFilename = "/JmriConfig3.xml";
    protected boolean configOK;
    protected boolean configDeferredLoadOK;
    protected boolean preferenceFileExists;
    static boolean preInit = false;
    private final static Logger log = LoggerFactory.getLogger(AppsBase.class.getName());

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
     */
    static public void preInit(String applicationName) {
        Log4JUtil.initLogging();

        try {
            Application.setApplicationName(applicationName);
        } catch (IllegalAccessException ex) {
            log.error("Unable to set application name");
        } catch (IllegalArgumentException ex) {
            log.error("Unable to set application name");
        }

        log.info(Log4JUtil.startupInfo(applicationName));

        preInit = true;
    }

    /**
     * Create and initialize the application object.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SC_START_IN_CTOR",
            justification = "The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public AppsBase(String applicationName, String configFileDef, String[] args) {

        if (!preInit) {
            preInit(applicationName);
            setConfigFilename(configFileDef, args);
        }

        Log4JUtil.initLogging();

        configureProfile();

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
        if (preferenceFileExists && Boolean.getBoolean("java.awt.headless")) {
            r = new Runnable() {

                public void run() {
                    try {
                        InstanceManager.tabbedPreferencesInstance().init();
                    } catch (Exception ex) {
                        log.error(ex.toString(), ex);
                    }
                }
            };
            Thread thr = new Thread(r);
            thr.start();
        }

        if (Boolean.getBoolean("org.jmri.python.preload")) {
            r = new Runnable() {

                public void run() {
                    try {
                        JmriScriptEngineManager.getDefault().initializeAllEngines();
                    } catch (Exception ex) {
                        log.error("Error in trying to initialize python interpreter " + ex.toString());
                    }
                }
            };
            Thread thr2 = new Thread(r, "initialize python interpreter");
            thr2.start();
        }

        // all loaded, initialize objects as necessary
        InstanceManager.logixManagerInstance().activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

    }

    /**
     * Configure the {@link jmri.profile.Profile} to use for this application.
     * <p>
     * Note that GUI-based applications must override this method, since this
     * method does not provide user feedback.
     */
    protected void configureProfile() {
        String profileFilename;
        FileUtil.createDirectory(FileUtil.getPreferencesPath());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        File profileFile;
        profileFilename = getConfigFileName().replaceFirst(".xml", ".properties");
        // decide whether name is absolute or relative
        if (!new File(profileFilename).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            profileFile = new File(FileUtil.getPreferencesPath() + profileFilename);
        } else {
            profileFile = new File(profileFilename);
        }
        ProfileManager.getDefault().setConfigFile(profileFile);
        // See if the profile to use has been specified on the command line as
        // a system property jmri.profile as a profile id.
        if (System.getProperties().containsKey(ProfileManager.SYSTEM_PROPERTY)) {
            ProfileManager.getDefault().setActiveProfile(System.getProperty(ProfileManager.SYSTEM_PROPERTY));
        }
        // @see jmri.profile.ProfileManager#migrateToProfiles JavaDoc for conditions handled here
        if (!ProfileManager.getDefault().getConfigFile().exists()) { // no profile config for this app
            try {
                if (ProfileManager.getDefault().migrateToProfiles(getConfigFileName())) { // migration or first use
                    // GUI should show message here
                    log.info(Bundle.getMessage("ConfigMigratedToProfile"));
                }
            } catch (IOException | IllegalArgumentException ex) {
                // GUI should show message here
                log.error("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
            }
        }
        try {
            // GUI should use ProfileManagerDialog.getStartingProfile here
            if (ProfileManager.getStartingProfile() != null) {
                // Manually setting the configFilename property since calling
                // Apps.setConfigFilename() does not reset the system property
                System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
                log.info("Starting with profile {}", ProfileManager.getDefault().getActiveProfile().getId());
            } else {
                log.error("Specify profile to use as command line argument.");
                log.error("If starting with saved profile configuration, ensure the autoStart property is set to \"true\"");
                log.error("Profiles not configurable. Using fallback per-application configuration.");
            }
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
        }
    }

    protected void installConfigurationManager() {
        ConfigureManager cm = new JmriConfigurationManager();
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        InstanceManager.store(cm, ConfigureManager.class);
        InstanceManager.setDefault(ConfigureManager.class, cm);
        log.debug("config manager installed");
    }

    protected void installManagers() {
        // Install a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", Application.getApplicationName(), null);

        // Install a user preferences manager
        InstanceManager.store(JmriUserPreferencesManager.getDefault(), UserPreferencesManager.class);

        // install the abstract action model that allows items to be added to the, both 
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);

        // install preference manager
        InstanceManager.store(new TabbedPreferences(), TabbedPreferences.class);

        // install the named bean handler
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);

        // Install an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);

        //Install Entry Exit Pairs Manager
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);

    }

    protected void setAndLoadPreferenceFile() {
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        final File file;
        File sharedConfig = null;
        try {
            sharedConfig = FileUtil.getFile(FileUtil.PROFILE + Profile.SHARED_CONFIG);
            if (!sharedConfig.canRead()) {
                sharedConfig = null;
            }
        } catch (FileNotFoundException ex) {
            // ignore - this only means that sharedConfig does not exist.
        }
        if (sharedConfig != null) {
            file = sharedConfig;
        } else if (!new File(getConfigFileName()).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath() + getConfigFileName());
        } else {
            file = new File(getConfigFileName());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            preferenceFileExists = false;
            configOK = false;
            log.info("No pre-existing config file found, searched for '" + file.getPath() + "'");
            return;
        }
        preferenceFileExists = true;
        try {
            configOK = InstanceManager.configureManagerInstance().load(file);
            log.debug("end load config file {}, OK={}", file.getName(), configOK);
        } catch (JmriException e) {
            configOK = false;
        }

        if (sharedConfig != null) {
            // sharedConfigs do not need deferred loads
            configDeferredLoadOK = true;
        } else {
        // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                configDeferredLoadOK = doDeferredLoad(file);
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until deferred load is completed
                    SwingUtilities.invokeAndWait(() -> {
                        configDeferredLoadOK = doDeferredLoad(file);
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    log.error("Exception creating system console frame: " + ex);
                }
            }
        }
        if (sharedConfig == null && configOK == true && configDeferredLoadOK == true) {
            log.info("Migrating preferences to new format...");
            // migrate preferences
            InstanceManager.tabbedPreferencesInstance().init();
            InstanceManager.tabbedPreferencesInstance().saveContents();
            InstanceManager.configureManagerInstance().storePrefs();
            // notify user of change
            log.info("Preferences have been migrated to new format.");
            log.info("New preferences format will be used after JMRI is restarted.");
        }
    }

    //abstract protected void addToActionModel();
    private boolean doDeferredLoad(File file) {
        boolean result;
        if (log.isDebugEnabled()) {
            log.debug("start deferred load from config file " + file.getName());
        }
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration: " + e);
            result = false;
        }
        if (log.isDebugEnabled()) {
            log.debug("end deferred load from config file " + file.getName() + ", OK=" + result);
        }
        return result;
    }

    protected void installShutDownManager() {
        InstanceManager.setShutDownManager(
                new DefaultShutDownManager());

        // configure the shutdown manager as a shutdown hook
        // when it is installed.  This allows a clean shutdown
        // when the shutdown hook is triggered via the POSIX signals
        // HUP (Signal 1), INT (Signal 2), or TERM (Signal 15).  Note 
        // SIGHUP, SIGINT, and SIGTERM cause the program to go through
        // the shutdown actions, but the Java process still remains until
        // it receives a KILL (Signal 9).  A completely orderly shutdown
        // can be forced by the two step process:
        // `kill -s 15 pid`
        // `kill -s 9 pid`
        jmri.util.RuntimeUtil.addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Shutdown hook called");
                }
                handleQuit();
            }
        }));
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
                        } //catch (org.jdom2.JDOMException jde) { log.error("Exception writing blocks: "+jde); }
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
     * Set up the configuration file name at startup.
     * <P>
     * The Configuration File name variable holds the name used to load the
     * configuration file during later startup processing. Applications invoke
     * this method to handle the usual startup hierarchy: <UL> <LI>If an
     * absolute filename was provided on the command line, use it <LI>If a
     * filename was provided that's not absolute, consider it to be in the
     * preferences directory <LI>If no filename provided, use a default name
     * (that's application specific) </UL>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em>.
     *
     * @param def  Default value if no other is provided
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
    // TODO: change to return profile-name/profile.xml
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

    /**
     * The application decided to quit, handle that.
     */
    static public boolean handleQuit() {
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
    static public boolean handleRestart() {
        log.debug("Start handleRestart");
        try {
            return InstanceManager.shutDownManagerInstance().restart();
        } catch (Exception e) {
            log.error("Continuing after error in handleRestart", e);
        }
        return false;
    }
}

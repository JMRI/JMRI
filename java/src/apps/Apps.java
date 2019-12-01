package apps;

import apps.gui3.tabbedpreferences.TabbedPreferences;
import apps.gui3.tabbedpreferences.TabbedPreferencesAction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventObject;
import java.util.Locale;
import javax.help.SwingHelpUtilities;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.JmriPlugin;
import jmri.ShutDownManager;
import jmri.implementation.AbstractShutDownTask;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.DebugMenu;
import jmri.jmrit.ToolsMenu;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.PrintDecoderListAction;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.jython.RunJythonScript;
import jmri.jmrit.operations.OperationsMenu;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.roster.swing.RosterMenu;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.withrottle.WiThrottleCreationAction;
import jmri.jmrix.ActiveSystemsMenu;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.plaf.macosx.Application;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileManagerDialog;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.Log4JUtil;
import jmri.util.SystemType;
import jmri.util.ThreadingUtil;
import jmri.util.WindowMenu;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.swing.FontComboUtil;
import jmri.util.swing.JFrameInterface;
import jmri.util.swing.SliderSnap;
import jmri.util.swing.WindowInterface;
import jmri.util.usb.RailDriverMenuItem;
import jmri.web.server.WebServerAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for JMRI applications.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2010
 * @author Dennis Miller Copyright 2005
 * @author Giorgio Terdina Copyright 2008
 * @author Matthew Harris Copyright (C) 2011
 */
public class Apps extends JPanel implements PropertyChangeListener, WindowListener {

    static String profileFilename;
    private Action prefsAction;  // defer initialization until needed so that Bundle accesses translate

    @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "SC_START_IN_CTOR"},
            justification = "only one application at a time. The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public Apps() {

        super(true);
        long start = System.nanoTime();
        log.trace("starting ctor at {}", start);

        splash(false);
        splash(true, true);
        log.trace("splash screens up, about to setButtonSpace");
        setButtonSpace();
        log.trace("about to setJynstrumentSpace");
        setJynstrumentSpace();

        log.trace("setLogo");
        jmri.Application.setLogo(logo());
        log.trace("setURL");
        jmri.Application.setURL(line2());

        // Enable proper snapping of JSliders
        SliderSnap.init();

        // Prepare font lists
        log.trace("prepareFontLists");
        prepareFontLists();

        // Get configuration profile
        log.trace("start to get configuration profile - locate files");
        // Needs to be done before loading a ConfigManager or UserPreferencesManager
        FileUtil.createDirectory(FileUtil.getPreferencesPath());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File profileFile;
        profileFilename = configFilename.replaceFirst(".xml", ".properties");
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
        // a system property org.jmri.profile as a profile id.
        if (System.getProperties().containsKey(ProfileManager.SYSTEM_PROPERTY)) {
            ProfileManager.getDefault().setActiveProfile(System.getProperty(ProfileManager.SYSTEM_PROPERTY));
        }
        log.trace("check if profile exists");
        // @see jmri.profile.ProfileManager#migrateToProfiles Javadoc for conditions handled here
        if (!profileFile.exists()) { // no profile config for this app
            log.trace("profileFile {} doesn't exist", profileFile);
            try {
                if (ProfileManager.getDefault().migrateToProfiles(configFilename)) { // migration or first use
                    // notify user of change only if migration occurred
                    // TODO: a real migration message
                    JOptionPane.showMessageDialog(sp,
                            Bundle.getMessage("ConfigMigratedToProfile"),
                            jmri.Application.getApplicationName(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(sp,
                        ex.getLocalizedMessage(),
                        jmri.Application.getApplicationName(),
                        JOptionPane.ERROR_MESSAGE);
                log.error(ex.getMessage());
            }
        }
        log.trace("about to try getStartingProfile");
        try {
            ProfileManagerDialog.getStartingProfile(sp);
            // Manually setting the configFilename property since calling
            // Apps.setConfigFilename() does not reset the system property
            configFilename = FileUtil.getProfilePath() + Profile.CONFIG_FILENAME;
            System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
            Profile profile = ProfileManager.getDefault().getActiveProfile();
            if (profile != null) {
                log.info("Starting with profile {}", profile.getId());
            } else {
                log.info("Starting without a profile");
            }
            
            // rapid language set; must follow up later with full setting as part of preferences
            apps.gui.GuiLafPreferencesManager.setLocaleMinimally(profile);
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
        }

        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.getDefault(ShutDownManager.class).
                register(new AbstractShutDownTask("Writing Blocks") {
                    @Override
                    public boolean execute() {
                        // Save block values prior to exit, if necessary
                        log.debug("Start writing block info");
                        try {
                            new BlockValueFile().writeBlockValues();
                        } //catch (org.jdom2.JDOMException jde) { log.error("Exception writing blocks: {}", jde); }
                        catch (IOException ioe) {
                            log.error("Exception writing blocks: {}", ioe.getMessage());
                        }

                        // continue shutdown
                        return true;
                    }
                });

        // Install configuration manager and Swing error handler
        // Constructing the JmriConfigurationManager also loads various configuration services
        ConfigureManager cm = InstanceManager.setDefault(ConfigureManager.class, new JmriConfigurationManager());

        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", nameString, null);

        // Install abstractActionModel
        InstanceManager.store(new apps.CreateButtonModel(), apps.CreateButtonModel.class);

        // find preference file and set location in configuration manager
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File file;
        File singleConfig;
        File sharedConfig = null;
        // decide whether name is absolute or relative
        if (!new File(configFilename).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            singleConfig = new File(FileUtil.getUserFilesPath() + configFilename);
        } else {
            singleConfig = new File(configFilename);
        }
        try {
            // get preferences file
            sharedConfig = FileUtil.getFile(FileUtil.PROFILE + Profile.SHARED_CONFIG);
            if (!sharedConfig.canRead()) {
                sharedConfig = null;
            }
        } catch (FileNotFoundException ex) {
            // ignore - sharedConfig will remain null in this case
        }
        // load config file if it exists
        if (sharedConfig != null) {
            file = sharedConfig;
        } else {
            file = singleConfig;
        }
                
        // ensure the UserPreferencesManager has loaded. Done on GUI
        // thread as it can modify GUI objects
        log.debug("*** About to getDefault(jmri.UserPreferencesManager.class) with file {}", file);
        ThreadingUtil.runOnGUI(() -> {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        });
        log.debug("*** Done");

        // now (attempt to) load the config file
        log.debug("Using config file(s) {}", file.getPath());
        if (file.exists()) {
            log.debug("start load config file {}", file.getPath());
            try {
                configOK = cm.load(file, true);
            } catch (JmriException e) {
                log.error("Unhandled problem loading configuration", e);
                configOK = false;
            }
            log.debug("end load config file, OK={}", configOK);
        } else {
            log.info("No saved preferences, will open preferences window.  Searched for {}", file.getPath());
            configOK = false;
        }

        // populate GUI
        log.debug("Start UI");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // done
        long end = System.nanoTime();

        long elapsedTime = (end - start) / 1000000;
        /*
         This ensures that the message is displayed on the screen for a minimum of 2.5seconds, if the time taken
         to get to this point in the code is longer that 2.5seconds then the wait is not invoked.
         */
        long sleep = 2500 - elapsedTime;
        if (sleep > 0) {
            log.debug("Debug message was displayed for less than 2500ms ({}ms). Sleeping for {}ms to allow user sufficient time to do something.",
                    elapsedTime, sleep);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }

        FileUtil.logFilePaths();

        splash(false);
        splash(true, false);
        Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
        while (debugmsg) {
            /*The user has pressed the interupt key that allows them to disable logixs
             at start up we do not want to process any more information until the user
             has answered the question */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        // Now load deferred config items
        if (file.exists()) {
            if (file.equals(singleConfig)) {
                // To avoid possible locks, deferred load should be
                // performed on the Swing thread
                if (SwingUtilities.isEventDispatchThread()) {
                    configDeferredLoadOK = doDeferredLoad(file);
                } else {
                    try {
                        // Use invokeAndWait method as we don't want to
                        // return until deferred load is completed
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "configDeferredLoadOK write is semi-global")
                            public void run() {
                                configDeferredLoadOK = doDeferredLoad(file);
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        log.error("Exception creating system console frame", ex);
                    }
                }
            } else {
                // deferred loading is not done in the new config
                configDeferredLoadOK = true;
            }
        } else {
            configDeferredLoadOK = false;
        }
        // If preferences need to be migrated, do it now
        if (sharedConfig == null && configOK == true && configDeferredLoadOK == true) {
            log.info("Migrating preferences to new format...");
            // migrate preferences
            InstanceManager.getOptionalDefault(TabbedPreferences.class).ifPresent(tp -> {
                // tp.init();
                tp.saveContents();
                cm.storePrefs();
            });
            // notify user of change
            log.info("Preferences have been migrated to new format.");
            log.info("New preferences format will be used after JMRI is restarted.");
            if (!GraphicsEnvironment.isHeadless()) {
                Profile profile = ProfileManager.getDefault().getActiveProfile();
                JOptionPane.showMessageDialog(sp,
                        Bundle.getMessage("SingleConfigMigratedToSharedConfig", profile),
                        jmri.Application.getApplicationName(),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Before starting to load preferences, make sure some managers are created.
        // This is needed because these aren't particularly well-behaved during
        // creation.
        InstanceManager.getDefault(jmri.LogixManager.class);
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);

        // Initialise the decoderindex file instance within a seperate thread to help improve first use perfomance
        new Thread(() -> {
            try {
                InstanceManager.getDefault(DecoderIndexFile.class);
            } catch (RuntimeException ex) {
                log.error("Error in trying to initialize decoder index file {}", ex.getMessage());
            }
        }, "initialize decoder index").start();

        if (Boolean.getBoolean("org.jmri.python.preload")) {
            new Thread(() -> {
                try {
                    JmriScriptEngineManager.getDefault().initializeAllEngines();
                } catch (RuntimeException ex) {
                    log.error("Error in trying to initialize python interpreter {}", ex.getMessage());
                }
            }, "initialize python interpreter").start();
        }

        // if the configuration didn't complete OK, pop the prefs frame and help
        log.debug("Config OK? {}, deferred config OK? {}", configOK, configDeferredLoadOK);
        if (!configOK || !configDeferredLoadOK) {
            HelpUtil.displayHelpRef("package.apps.AppConfigPanelErrorPage");
            doPreferences();
        }
        log.debug("Done with doPreferences, start statusPanel");

        add(statusPanel());
        log.debug("Done with statusPanel, start buttonSpace");
        add(buttonSpace());
        add(_jynstrumentSpace);
        long eventMask = AWTEvent.MOUSE_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEvent e) -> {
            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                if (me.isPopupTrigger() && me.getComponent() instanceof JTextComponent) {
                    final JTextComponent component1 = (JTextComponent) me.getComponent();
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item;
                    item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText("Copy");
                    item.setEnabled(component1.getSelectionStart() != component1.getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.CutAction());
                    item.setText("Cut");
                    item.setEnabled(component1.isEditable() && component1.getSelectionStart() != component1.getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.PasteAction());
                    item.setText("Paste");
                    item.setEnabled(component1.isEditable());
                    menu.add(item);
                    menu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        }, eventMask);

        // do final activation
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        log.debug("End constructor");
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                result = cmOD.loadDeferred(file);
            } else {
                log.error("Failed to get default configure manager");
                result = false;
            }
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration", e);
            result = false;
        }
        log.debug("end deferred load from config file, OK={}", result);
        return result;
    }

    /**
     * Prepare the JPanel to contain buttons in the startup GUI. Since it's
     * possible to add buttons via the preferences, this space may have
     * additional buttons appended to it later. The default implementation here
     * just creates an empty space for these to be added to.
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout());
    }
    static JComponent _jynstrumentSpace = null;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setJynstrumentSpace() {
        _jynstrumentSpace = new JPanel();
        _jynstrumentSpace.setLayout(new FlowLayout());
        new FileDrop(_jynstrumentSpace, (File[] files) -> {
            for (File file : files) {
                ynstrument(file.getPath());
            }
        });
    }

    public static void ynstrument(String path) {
        Jynstrument it = JynstrumentFactory.createInstrument(path, _jynstrumentSpace);
        if (it == null) {
            log.error("Error while creating Jynstrument {}", path);
            return;
        }
        ThrottleFrame.setTransparent(it);
        it.setVisible(true);
        _jynstrumentSpace.setVisible(true);
        _jynstrumentSpace.add(it);
    }

    /**
     * Create default menubar.
     * <p>
     * This does not include the development menu.
     *
     * @param menuBar Menu bar to be populated
     * @param wi      WindowInterface where this menu bar will appear
     */
    protected void createMenus(JMenuBar menuBar, WindowInterface wi) {
        // the debugging statements in the following are
        // for testing startup time
        log.debug("start building menus");

        if (SystemType.isMacOSX()) {
            Application.getApplication().setQuitHandler((EventObject eo) -> handleQuit());
        }

        fileMenu(menuBar, wi);
        editMenu(menuBar, wi);
        toolsMenu(menuBar, wi);
        rosterMenu(menuBar, wi);
        panelMenu(menuBar, wi);
        // check to see if operations in main menu
        if (jmri.jmrit.operations.setup.Setup.isMainMenuEnabled()) {
            operationsMenu(menuBar, wi);
        }
        systemsMenu(menuBar, wi);
        debugMenu(menuBar, wi);
        menuBar.add(new WindowMenu(wi));
        helpMenu(menuBar, wi);
        log.debug("end building menus");
    }

    /**
     * Create default File menu
     *
     * @param menuBar Menu bar to be populated
     * @param wi      WindowInterface where this menu will appear as part of the
     *                menu bar
     */
    protected void fileMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);

        fileMenu.add(new PrintDecoderListAction(Bundle.getMessage("MenuPrintDecoderDefinitions"), wi.getFrame(), false));
        fileMenu.add(new PrintDecoderListAction(Bundle.getMessage("MenuPrintPreviewDecoderDefinitions"), wi.getFrame(), true));

        // Use Mac OS X native Quit if using Aqua look and feel
        if (!(SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel())) {
            fileMenu.add(new JSeparator());
            fileMenu.add(new AbstractAction(Bundle.getMessage("MenuItemQuit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleQuit();
                }
            });
        }
    }

    /**
     * Open Preferences action.
     * Often done due to error
     */
    public void doPreferences() {
        if (prefsAction == null) prefsAction = new TabbedPreferencesAction();
        prefsAction.actionPerformed(null);
    }

    /**
     * Set the location of the window-specific help for the preferences pane.
     * Made a separate method so if can be overridden for application specific
     * preferences help
     *
     * @param frame    The frame being described in the help system
     * @param location The location within the JavaHelp system
     */
    protected void setPrefsFrameHelp(JmriJFrame frame, String location) {
        frame.addHelpMenu(location, true);
    }

    protected void editMenu(JMenuBar menuBar, WindowInterface wi) {

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        menuBar.add(editMenu);

        // cut, copy, paste
        AbstractAction a;
        a = new DefaultEditorKit.CutAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemCut"));
        editMenu.add(a);
        a = new DefaultEditorKit.CopyAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemCopy"));
        editMenu.add(a);
        a = new DefaultEditorKit.PasteAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemPaste"));
        editMenu.add(a);

        // Put prefs in Apple's prefered area on Mac OS X
        if (SystemType.isMacOSX()) {
            Application.getApplication().setPreferencesHandler((EventObject eo) -> {
                doPreferences();
            });
        }
        // Include prefs in Edit menu if not on Mac OS X or not using Aqua Look and Feel
        if (!SystemType.isMacOSX() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            editMenu.addSeparator();
            if (prefsAction == null) prefsAction = new TabbedPreferencesAction();
            editMenu.add(prefsAction);
        }

    }

    protected void toolsMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new ToolsMenu(Bundle.getMessage("MenuTools")));
    }

    protected void operationsMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new OperationsMenu());
    }

    protected void rosterMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new RosterMenu(Bundle.getMessage("MenuRoster"), RosterMenu.MAINMENU, this));
    }

    protected void panelMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(InstanceManager.getDefault(PanelMenu.class));
    }

    /**
     * Show only active systems in the menu bar.
     *
     * @param menuBar the menu bar to add systems to
     * @param wi      the containing WindowInterface
     */
    protected void systemsMenu(JMenuBar menuBar, WindowInterface wi) {
        ActiveSystemsMenu.addItems(menuBar);
    }

    protected void debugMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu d = new DebugMenu(this);

        // also add some tentative items from jmrix
        d.add(new JSeparator());
        d.add(new jmri.jmrix.pricom.PricomMenu());
        d.add(new JSeparator());

        d.add(new jmri.jmrix.jinput.treecontrol.TreeAction());
        d.add(new jmri.jmrix.libusb.UsbViewAction());

        d.add(new JSeparator());

        d.add(new RailDriverMenuItem());

        try {
            d.add(new RunJythonScript(Bundle.getMessage("MenuRailDriverThrottle"), new File(FileUtil.findURL("jython/RailDriver.py").toURI())));
        } catch (URISyntaxException | NullPointerException ex) {
            log.error("Unable to load RailDriver Throttle", ex);
            JMenuItem i = new JMenuItem(Bundle.getMessage("MenuRailDriverThrottle"));
            i.setEnabled(false);
            d.add(i);
        }

        // also add some tentative items from webserver
        d.add(new JSeparator());
        d.add(new WebServerAction());

        d.add(new JSeparator());
        d.add(new WiThrottleCreationAction());

        d.add(new JSeparator());
        d.add(new apps.TrainCrew.InstallFromURL());
        
        // add final to menu bar
        menuBar.add(d);

    }

    /**
     * Add a script menu to a window menu bar.
     * 
     * @param menuBar the menu bar to add the script menu to
     * @param wi the window interface containing menuBar
     * @deprecated since 4.17.5 without direct replacement; appears
     * to have been empty method since 1.2.3
     */
    @Deprecated
    protected void scriptMenu(JMenuBar menuBar, WindowInterface wi) {
        // temporarily remove Scripts menu; note that "Run Script"
        // has been added to the Panels menu
        // JMenu menu = new JMenu("Scripts");
        // menuBar.add(menu);
    }

    protected void developmentMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction("Sample automaton 1"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
        //devMenu.add(new JSeparator());
        //devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));
    }

    protected void helpMenu(JMenuBar menuBar, WindowInterface wi) {
        // create menu and standard items
        JMenu helpMenu = HelpUtil.makeHelpMenu(mainWindowHelpID(), true);

        // tell help to use default browser for external types
        SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");

        // use as main help menu
        menuBar.add(helpMenu);
    }

    /**
     * Returns the ID for the main window's help, which is application specific
     *
     * @return help identifier for main window
     */
    protected String mainWindowHelpID() {
        return "package.apps.Apps";
    }

    protected String line1() {
        return Bundle.getMessage("DefaultVersionCredit", jmri.Version.name());
    }

    protected String line2() {
        return "http://jmri.org/";
    }

    protected String line3() {
        return " ";
    }
    // line 4
    JLabel cs4 = new JLabel();

    protected void buildLine4(JPanel pane) {
        if (connection[0] != null) {
            buildLine(connection[0], cs4, pane);
        }
    }
    // line 5 optional
    JLabel cs5 = new JLabel();

    protected void buildLine5(JPanel pane) {
        if (connection[1] != null) {
            buildLine(connection[1], cs5, pane);
        }
    }
    // line 6 optional
    JLabel cs6 = new JLabel();

    protected void buildLine6(JPanel pane) {
        if (connection[2] != null) {
            buildLine(connection[2], cs6, pane);
        }
    }
    // line 7 optional
    JLabel cs7 = new JLabel();

    protected void buildLine7(JPanel pane) {
        if (connection[3] != null) {
            buildLine(connection[3], cs7, pane);
        }
    }

    protected void buildLine(ConnectionConfig conn, JLabel cs, JPanel pane) {
        if (conn.name().equals(JmrixConfigPane.NONE)) {
            cs.setText(" ");
            return;
        }
        
        log.debug("conn.name() is {} ",conn.name()); // eg CAN via MERG Network Interface
        log.debug("conn.getConnectionName() is {} ",conn.getConnectionName()); // eg MERG2
        log.debug("conn.getManufacturer() is {} ",conn.getManufacturer()); // eg MERG
        
        ConnectionStatus.instance().addConnection(conn.getConnectionName(), conn.getInfo());
        cs.setFont(pane.getFont());
        updateLine(conn, cs);
        pane.add(cs);
    }

    protected void updateLine(ConnectionConfig conn, JLabel cs) {
        if (conn.getDisabled()) {
            return;
        }
        String name = conn.getConnectionName();
        if (name == null) {
            name = conn.getManufacturer();
        }
        if (ConnectionStatus.instance().isConnectionOk(name, conn.getInfo())) {
            cs.setForeground(Color.black);
            String cf = Bundle.getMessage("ConnectionSucceeded", name, conn.name(), conn.getInfo());
            cs.setText(cf);
        } else {
            cs.setForeground(Color.red);
            String cf = Bundle.getMessage("ConnectionFailed", name, conn.name(), conn.getInfo());
            cs.setText(cf);
        }

        this.revalidate();
    }

    protected String line8() {
        return " ";
    }

    protected String line9() {
        return Bundle.getMessage("JavaVersionCredit",
                System.getProperty("java.version", "<unknown>"),
                Locale.getDefault());
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    /**
     * Fill in the logo and status panel
     *
     * @return Properly-filled out JPanel
     */
    protected JPanel statusPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        log.debug("Fetch main logo: {}", logo());
        pane1.add(new JLabel(new ImageIcon(getToolkit().getImage(FileUtil.findURL(logo(), FileUtil.Location.INSTALLED)), "JMRI logo"), JLabel.LEFT));
        pane1.add(Box.createRigidArea(new Dimension(15, 0))); // Some spacing between logo and status panel

        log.debug("start labels");
        JPanel pane2 = new JPanel();

        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(line1()));
        pane2.add(new JLabel(line2()));
        pane2.add(new JLabel(line3()));

        String name = ProfileManager.getDefault().getActiveProfileName();
        pane2.add(new JLabel(Bundle.getMessage("ActiveProfile", name)));

        // add listener for Com port updates
        ConnectionStatus.instance().addPropertyChangeListener(this);
        int i = 0;
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                connection[i] = conn;
                i++;
            }
            if (i > 3) {
                break;
            }
        }
        buildLine4(pane2);
        buildLine5(pane2);
        buildLine6(pane2);
        buildLine7(pane2);

        pane2.add(new JLabel(line8()));
        pane2.add(new JLabel(line9()));
        pane1.add(pane2);
        return pane1;
    }
    //int[] connection = {-1,-1,-1,-1};
    ConnectionConfig[] connection = {null, null, null, null};

    /**
     * Closing the main window is a shutdown request.
     *
     * @param e the event triggering the close
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (!InstanceManager.getDefault(ShutDownManager.class).isShuttingDown()
                && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                        null,
                        Bundle.getMessage("MessageLongCloseWarning"),
                        Bundle.getMessage("MessageShortCloseWarning"),
                        JOptionPane.YES_NO_OPTION)) {
            handleQuit();
        }
        // if get here, didn't quit, so don't close window
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps." + key);
            if (current == null) {
                System.setProperty("org.jmri.Apps." + key, value);
            } else if (!current.equals(value)) {
                log.warn("JMRI property {} already set to {}, skipping reset to {}", key, current, value);
            }
        } catch (RuntimeException e) {
            log.error("Unable to set JMRI property {} to {} due to execption {}", key, value, e);
        }
    }

    /**
     * Provide access to a place where applications can expect the configuration
     * code to build run-time buttons.
     *
     * @see apps.startup.CreateButtonModelFactory
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;
    static SplashWindow sp = null;
    static AWTEventListener debugListener = null;

    // TODO: Remove the "static" nature of much of the initialization someday.
    //       It exits to allow splash() to be called first-thing in main(), see
    //       apps.DecoderPro.DecoderPro.main(...)
    //       Or maybe, just not worry about this here, in the older base class,
    //       and address it in the newer apps.gui3.Apps3 as that's the base class of the future.
    static boolean debugFired = false;  // true if we've seen F8 during startup
    static boolean debugmsg = false;    // true while we're handling the "No Logix?" prompt window on startup

    static protected void splash(boolean show) {
        splash(show, false);
    }

    static protected void splash(boolean show, boolean debug) {
        Log4JUtil.initLogging();
        if (debugListener == null && debug) {
            // set a global listener for debug options
            debugFired = false;
            Toolkit.getDefaultToolkit().addAWTEventListener(
                    debugListener = (AWTEvent e) -> {
                        if (!debugFired) {
                            /*We set the debugmsg flag on the first instance of the user pressing any button
                            and the if the debugFired hasn't been set, this allows us to ensure that we don't
                            miss the user pressing F8, while we are checking*/
                            debugmsg = true;
                            if (e.getID() == KeyEvent.KEY_PRESSED && e instanceof KeyEvent && ((KeyEvent) e).getKeyCode() == 119) {
                                startupDebug();
                            } else {
                                debugmsg = false;
                            }
                        }
                    },
                    AWTEvent.KEY_EVENT_MASK);
        }

        // bring up splash window for startup
        if (sp == null) {
            if (debug) {
                sp = new SplashWindow(splashDebugMsg());
            } else {
                sp = new SplashWindow();
            }
        }
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
            Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
            debugListener = null;
            sp = null;
        }
    }

    static protected JPanel splashDebugMsg() {
        JLabel panelLabel = new JLabel(Bundle.getMessage("PressF8ToDebug"));
        panelLabel.setFont(panelLabel.getFont().deriveFont(9f));
        JPanel panel = new JPanel();
        panel.add(panelLabel);
        return panel;
    }

    static protected void startupDebug() {
        debugFired = true;
        debugmsg = true;

        Object[] options = {"Disable", "Enable"};

        int retval = JOptionPane.showOptionDialog(null, "Start JMRI with Logix enabled or disabled?", "Start Up",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (retval != 0) {
            debugmsg = false;
            return;
        }
        InstanceManager.getDefault(jmri.LogixManager.class).setLoadDisabled(true);
        log.info("Requested loading with Logixs disabled.");
        debugmsg = false;
    }

    /**
     * The application decided to quit, handle that.
     *
     * @return true if successfully ran all shutdown tasks and can quit; false
     *         otherwise
     */
    static public boolean handleQuit() {
        return AppsBase.handleQuit();
    }

    /**
     * The application decided to restart, handle that.
     *
     * @return true if successfully ran all shutdown tasks and can quit; false
     *         otherwise
     */
    static public boolean handleRestart() {
        return AppsBase.handleRestart();
    }

    /**
     * Set up the configuration file name at startup.
     * <p>
     * The Configuration File name variable holds the name used to load the
     * configuration file during later startup processing. Applications invoke
     * this method to handle the usual startup hierarchy:
     * <ul>
     * <li>If an absolute filename was provided on the command line, use it
     * <li>If a filename was provided that's not absolute, consider it to be in
     * the preferences directory
     * <li>If no filename provided, use a default name (that's application
     * specific)
     * </ul>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em> and may not contain the equals sign (=).
     *
     * @param def  Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
        // skip if org.jmri.Apps.configFilename is set
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return;
        }
        // save the configuration filename if present on the command line
        if (args.length >= 1 && args[0] != null && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: {}", args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: {}", arg);
            }
        }
        Apps.configFilename = def;
        setJmriSystemProperty("configFilename", def);
    }

    static public String getConfigFileName() {
        return configFilename;
    }

    static protected void createFrame(Apps containedPane, JmriJFrame frame) {
        // create the main frame and menus
        // Create a WindowInterface object based on the passed-in Frame
        JFrameInterface wi = new JFrameInterface(frame);
        // Create a menu bar
        containedPane.menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        containedPane.createMenus(containedPane.menuBar, wi);
        // connect Help target now that globalHelpBroker has been instantiated
        containedPane.attachHelp();

        // invoke plugin, if any
        JmriPlugin.start(frame, containedPane.menuBar);

        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);

        // handle window close
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(containedPane);

        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();

        // first set a default position and size
        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
        
        // then attempt set from stored preference
        frame.setFrameLocation();
        
        // and finally show
        frame.setVisible(true);
    }

    static protected void loadFile(String name) {
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            URL pFile = cmOD.find(name);
            if (pFile != null) {
                try {
                    cmOD.load(pFile);
                } catch (JmriException e) {
                    log.error("Unhandled problem in loadFile", e);
                }
            } else {
                log.warn("Could not find {} config file", name);
            }
        } else {
            log.error("Failed to get default configure manager");
        }
    }

    static String configFilename = System.getProperty("org.jmri.Apps.configFilename", "jmriconfig2.xml");  // usually overridden, this is default
    // The following MUST be protected for 3rd party applications
    // (such as CATS) which are derived from this class.
    @SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configOK;
    @SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configDeferredLoadOK;
    // GUI members
    private JMenuBar menuBar;

    static String nameString = "JMRI program";

    protected static void setApplication(String name) {
        try {
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            log.warn("Unable to set application name", ex);
        }
    }

    /**
     * Set and log some startup information. This is intended to be the central
     * connection point for common startup and logging.
     *
     * @param name Program/application name as known by the user
     */
    protected static void setStartupInfo(String name) {
        // Set the application name
        try {
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            log.warn("Unable to set application name", ex);
        }

        // Log the startup information
        log.info(Log4JUtil.startupInfo(name));
    }

    private void prepareFontLists() {
        // Prepare font lists
        Thread fontThread = new Thread(() -> {
            log.debug("Prepare font lists...");
            FontComboUtil.prepareFontLists();
            log.debug("...Font lists built");
        }, "PrepareFontListsThread");
        
        fontThread.setDaemon(true);
        fontThread.setPriority(Thread.MIN_PRIORITY);
        fontThread.start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        log.debug("property change: comm port status update");
        if (connection[0] != null) {
            updateLine(connection[0], cs4);
        }

        if (connection[1] != null) {
            updateLine(connection[1], cs5);
        }

        if (connection[2] != null) {
            updateLine(connection[2], cs6);
        }

        if (connection[3] != null) {
            updateLine(connection[3], cs7);
        }

    }

    /**
     * Attach Help target to Help button on Main Screen.
     */
    protected void attachHelp() {
    }

    private final static Logger log = LoggerFactory.getLogger(Apps.class);

}

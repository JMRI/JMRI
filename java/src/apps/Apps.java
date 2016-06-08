package apps;

import apps.gui3.TabbedPreferences;
import apps.startup.StartupActionModelUtil;
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
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.help.SwingHelpUtilities;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.JmriPlugin;
import jmri.NamedBeanHandleManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
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
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.withrottle.WiThrottleCreationAction;
import jmri.jmrix.ActiveSystemsMenu;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultShutDownManager;
import jmri.managers.JmriUserPreferencesManager;
import jmri.plaf.macosx.Application;
import jmri.plaf.macosx.PreferencesHandler;
import jmri.plaf.macosx.QuitHandler;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileManagerDialog;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.Log4JUtil;
import jmri.util.SystemType;
import jmri.util.WindowMenu;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.iharder.dnd.FileDrop.Listener;
import jmri.util.swing.FontComboUtil;
import jmri.util.swing.JFrameInterface;
import jmri.util.swing.SliderSnap;
import jmri.util.swing.WindowInterface;
import jmri.web.server.WebServerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Jmri applications.
 * <P>
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2010
 * @author Dennis Miller Copyright 2005
 * @author Giorgio Terdina Copyright 2008
 * @author Matthew Harris Copyright (C) 2011
 */
public class Apps extends JPanel implements PropertyChangeListener, WindowListener {

    static String profileFilename;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "SC_START_IN_CTOR"},
            justification = "only one application at a time. The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public Apps(JFrame frame) {

        super(true);
        long start = System.nanoTime();

        splash(false);
        splash(true, true);
        setButtonSpace();
        setJynstrumentSpace();

        jmri.Application.setLogo(logo());
        jmri.Application.setURL(line2());

        // Enable proper snapping of JSliders
        SliderSnap.init();

        // Prepare font lists
        prepareFontLists();

        // install shutdown manager
        InstanceManager.setShutDownManager(new DefaultShutDownManager());

        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
                register(new AbstractShutDownTask("Writing Blocks") {
                    @Override
                    public boolean execute() {
                        // Save block values prior to exit, if necessary
                        log.debug("Start writing block info");
                        try {
                            new BlockValueFile().writeBlockValues();
                        } //catch (org.jdom2.JDOMException jde) { log.error("Exception writing blocks: {}", jde); }
                        catch (IOException ioe) {
                            log.error("Exception writing blocks: {}", ioe);
                        }

                        // continue shutdown
                        return true;
                    }
                });

        // Get configuration profile
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
        // a system property jmri.profile as a profile id.
        if (System.getProperties().containsKey(ProfileManager.SYSTEM_PROPERTY)) {
            ProfileManager.getDefault().setActiveProfile(System.getProperty(ProfileManager.SYSTEM_PROPERTY));
        }
        // @see jmri.profile.ProfileManager#migrateToProfiles JavaDoc for conditions handled here
        if (!ProfileManager.getDefault().getConfigFile().exists()) { // no profile config for this app
            try {
                if (ProfileManager.getDefault().migrateToProfiles(configFilename)) { // migration or first use
                    // notify user of change only if migration occured
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
        try {
            ProfileManagerDialog.getStartingProfile(sp);
            // Manually setting the configFilename property since calling
            // Apps.setConfigFilename() does not reset the system property
            configFilename = FileUtil.getProfilePath() + Profile.CONFIG_FILENAME;
            System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
            log.info("Starting with profile {}", ProfileManager.getDefault().getActiveProfile().getId());
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
        }

        // Install configuration manager and Swing error handler
        ConfigureManager cm = new JmriConfigurationManager();
        InstanceManager.store(cm, ConfigureManager.class);
        InstanceManager.setDefault(ConfigureManager.class, cm);

        // Install a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", nameString, null);

        // Install a user preferences manager
        InstanceManager.store(JmriUserPreferencesManager.getDefault(), UserPreferencesManager.class);
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        // Install an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);

        // install preference manager
        InstanceManager.store(new TabbedPreferences(), TabbedPreferences.class);

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
        log.debug("Using config file(s) {}", file.getPath());
        if (file.exists()) {
            log.debug("start load config file {}", file.getPath());
            try {
                configOK = InstanceManager.configureManagerInstance().load(file, true);
            } catch (JmriException e) {
                log.error("Unhandled problem loading configuration", e);
                configOK = false;
            }
            log.debug("end load config file, OK={}", configOK);
        } else {
            log.info("No saved preferences, will open preferences window.  Searched for {}", file.getPath());
            configOK = false;
        }

        //Install Entry Exit Pairs Manager
        //   Done after load config file so that connection-system-specific Managers are defined and usable
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);

        // Add actions to abstractActionModel
        // Done here as initial non-GUI initialisation is completed
        // and UI L&F has been set
        addToActionModel();

        // populate GUI
        log.debug("Start UI");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a WindowInterface object based on the passed-in Frame
        JFrameInterface wi = new JFrameInterface(frame);
        // Create a menu bar
        menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        createMenus(menuBar, wi);

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
                            @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "configDeferredLoadOK write is semi-global")
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
            InstanceManager.tabbedPreferencesInstance().init();
            InstanceManager.tabbedPreferencesInstance().saveContents();
            InstanceManager.configureManagerInstance().storePrefs();
            // notify user of change
            log.info("Preferences have been migrated to new format.");
            log.info("New preferences format will be used after JMRI is restarted.");
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(sp,
                        Bundle.getMessage("SingleConfigMigratedToSharedConfig", ProfileManager.getDefault().getActiveProfile().getName()),
                        jmri.Application.getApplicationName(),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        /*Once all the preferences have been loaded we can initial the preferences
         doing it in a thread at this stage means we can let it work in the background*/
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    InstanceManager.tabbedPreferencesInstance().init();
                } catch (Exception ex) {
                    log.error("Error trying to setup preferences {}", ex.getLocalizedMessage(), ex);
                }
            }
        };
        Thread thr = new Thread(r, "init prefs");
        thr.start();
        //Initialise the decoderindex file instance within a seperate thread to help improve first use perfomance
        r = new Runnable() {
            @Override
            public void run() {
                try {
                    DecoderIndexFile.instance();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize decoder index file {}", ex.toString());
                }
            }
        };
        Thread thr2 = new Thread(r, "initialize decoder index");
        thr2.start();

        if (Boolean.getBoolean("org.jmri.python.preload")) {
            r = new Runnable() {
                public void run() {
                    try {
                        JmriScriptEngineManager.getDefault().initializeAllEngines();
                    } catch (Exception ex) {
                        log.error("Error in trying to initialize python interpreter {}", ex.toString());
                    }
                }
            };
            Thread thr3 = new Thread(r, "initialize python interpreter");
            thr3.start();
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

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent e) {
                if (e instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) e;
                    if (me.isPopupTrigger() && me.getComponent() instanceof JTextComponent) {
                        final JTextComponent component = (JTextComponent) me.getComponent();
                        final JPopupMenu menu = new JPopupMenu();
                        JMenuItem item;
                        item = new JMenuItem(new DefaultEditorKit.CopyAction());
                        item.setText("Copy");
                        item.setEnabled(component.getSelectionStart() != component.getSelectionEnd());
                        menu.add(item);
                        item = new JMenuItem(new DefaultEditorKit.CutAction());
                        item.setText("Cut");
                        item.setEnabled(component.isEditable() && component.getSelectionStart() != component.getSelectionEnd());
                        menu.add(item);
                        item = new JMenuItem(new DefaultEditorKit.PasteAction());
                        item.setText("Paste");
                        item.setEnabled(component.isEditable());
                        menu.add(item);
                        menu.show(me.getComponent(), me.getX(), me.getY());
                    }
                }
            }
        }, eventMask);

        // do final activation
        InstanceManager.logixManagerInstance().activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        // Loads too late - now started from ItemPalette
//        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();

        log.debug("End constructor");
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration", e);
            result = false;
        }
        log.debug("end deferred load from config file, OK={}", result);
        return result;
    }

    protected final void addToActionModel() {
        StartupActionModelUtil util = InstanceManager.getDefault(StartupActionModelUtil.class);
        ResourceBundle actionList = ResourceBundle.getBundle("apps.ActionListBundle");
        Enumeration<String> e = actionList.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                util.addAction(key, actionList.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        }
    }

    /**
     * Prepare the JPanel to contain buttons in the startup GUI. Since it's
     * possible to add buttons via the preferences, this space may have
     * additional buttons appended to it later. The default implementation here
     * just creates an empty space for these to be added to.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout());
    }
    static JComponent _jynstrumentSpace = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setJynstrumentSpace() {
        _jynstrumentSpace = new JPanel();
        _jynstrumentSpace.setLayout(new FlowLayout());
        new FileDrop(_jynstrumentSpace, new Listener() {
            @Override
            public void filesDropped(File[] files) {
                for (int i = 0; i < files.length; i++) {
                    ynstrument(files[i].getPath());
                }
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
     * <P>
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
            Application.getApplication().setQuitHandler(new QuitHandler() {
                @Override
                public boolean handleQuitRequest(EventObject eo) {
                    return handleQuit();
                }
            });
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
        scriptMenu(menuBar, wi);
        debugMenu(menuBar, wi);
        menuBar.add(new WindowMenu(wi)); // * GT 28-AUG-2008 Added window menu
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
    Action prefsAction;

    public void doPreferences() {
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

        // prefs
        prefsAction = new apps.gui3.TabbedPreferencesAction(Bundle.getMessage("MenuItemPreferences"));
        // Put prefs in Apple's prefered area on Mac OS X
        if (SystemType.isMacOSX()) {
            Application.getApplication().setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(EventObject eo) {
                    doPreferences();
                }
            });
        }
        // Include prefs in Edit menu if not on Mac OS X or not using Aqua Look and Feel
        if (!SystemType.isMacOSX() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            editMenu.addSeparator();
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
        menuBar.add(PanelMenu.instance());
    }

    /**
     * Show only active systems in the menu bar.
     * <P>
     * Alternately, you might want to do
     * <PRE>
     *    menuBar.add(new jmri.jmrix.SystemsMenu());
     * </PRE>
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
        menuBar.add(d);

    }

    protected void scriptMenu(JMenuBar menuBar, WindowInterface wi) {
        // temporarily remove Scripts menu; note that "Run Script"
        // has been added to the Panels menu
        // JMenu menu = new JMenu("Scripts");
        // menuBar.add(menu);
        // menu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython script", this));
        // menu.add(new jmri.jmrit.automat.JythonSigletAction("Jython siglet", this));
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
        try {

            // create menu and standard items
            JMenu helpMenu = HelpUtil.makeHelpMenu(mainWindowHelpID(), true);

            // tell help to use default browser for external types
            SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");

            // use as main help menu 
            menuBar.add(helpMenu);

        } catch (Throwable e3) {
            log.error("Unexpected error creating help.", e3);
        }

    }

    /**
     * Returns the ID for the main window's help, which is application specific
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
        ConnectionStatus.instance().addConnection(conn.name(), conn.getInfo());
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
        if (ConnectionStatus.instance().isConnectionOk(conn.getInfo())) {
            cs.setForeground(Color.black);
            String cf = Bundle.getMessage("ConnectionSucceeded", name, conn.name(), conn.getInfo());
            cs.setText(cf);
        } else {
            cs.setForeground(Color.red);
            String cf = Bundle.getMessage("ConnectionFailed", name, conn.name(), conn.getInfo());
            cf = cf.toUpperCase();
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
                Locale.getDefault().toString());
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
        
        if (ProfileManager.getDefault()!=null && ProfileManager.getDefault().getActiveProfile() != null) {
            pane2.add(new JLabel(Bundle.getMessage("ActiveProfile", ProfileManager.getDefault().getActiveProfile().getName())));
        } else {
            pane2.add(new JLabel(Bundle.getMessage("FailedProfile")));            
        }
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
     * Closing the main window is a shutdown request
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
        } catch (Exception e) {
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
    //       It exits to allow splash() to be called first-thing in main(), see e.g.
    //       apps.DecoderPro.DecoderPro.main(...) 
    //       Or maybe, just not worry about this here, in the older base class,
    //       and address it in the newer apps.gui3.Apps3 as that's the base class of the future.
    static boolean debugFired = false;  // true if we've seen F8 during startup
    static boolean debugmsg = false;    // true while we're handling the "No Logix?" prompt window on startup

    static protected void splash(boolean show) {
        splash(show, false);
    }

    /**
     * Invoke the standard Log4J logging initialization.
     * <p>
     * No longer used here. ({@link #splash} calls the initialization directly.
     * Left as a deprecated method because other code, e.g. CATS is still using
     * in in JMRI 3.7 and perhaps 3.8
     *
     * @deprecated Since 3.7.2, use @{link jmri.util.Log4JUtil#initLogging} directly.
     */
    @Deprecated
    static protected void initLog4J() {
        jmri.util.Log4JUtil.initLogging();
    }

    static protected void splash(boolean show, boolean debug) {
        Log4JUtil.initLogging();
        if (debugListener == null && debug) {
            // set a global listener for debug options
            debugFired = false;
            Toolkit.getDefaultToolkit().addAWTEventListener(
                    debugListener = new AWTEventListener() {
                @Override
                @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "debugmsg write is semi-global")
                public void eventDispatched(AWTEvent e) {
                    if (!debugFired) {
                        /*We set the debugmsg flag on the first instance of the user pressing any button
                                 and the if the debugFired hasn't been set, this allows us to ensure that we don't
                                 miss the user pressing F8, while we are checking*/
                        debugmsg = true;
                        if (e.getID() == KeyEvent.KEY_PRESSED && e instanceof KeyEvent && ((KeyEvent)e).getKeyCode() == 119) {
                            startupDebug();
                        } else {
                            debugmsg = false;
                        }
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

        Object[] options = {"Disable",
            "Enable"};

        int retval = JOptionPane.showOptionDialog(null, "Start JMRI with Logix enabled or disabled?", "Start Up",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (retval != 0) {
            debugmsg = false;
            return;
        }
        InstanceManager.logixManagerInstance().setLoadDisabled(true);
        log.info("Requested loading with Logixs disabled.");
        debugmsg = false;
    }

    /**
     * The application decided to quit, handle that.
     */
    static public boolean handleQuit() {
        return AppsBase.handleQuit();
    }

    /**
     * The application decided to restart, handle that.
     */
    static public boolean handleRestart() {
        return AppsBase.handleRestart();
    }

    /**
     * Set up the configuration file name at startup.
     * <P>
     * The Configuration File name variable holds the name used to load the
     * configuration file during later startup processing. Applications invoke
     * this method to handle the usual startup hierarchy:
     * <UL>
     * <LI>If an absolute filename was provided on the command line, use it
     * <LI>If a filename was provided that's not absolute, consider it to be in
     * the preferences directory
     * <LI>If no filename provided, use a default name (that's application
     * specific)
     * </UL>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em> and may not contain the equals sign (=).
     *
     * @param def  Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
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

    static protected void createFrame(Apps containedPane, JFrame frame) {
        // create the main frame and menus

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
        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
        frame.setVisible(true);
    }

    static protected void loadFile(String name) {
        URL pFile = InstanceManager.configureManagerInstance().find(name);
        if (pFile != null) {
            try {
                InstanceManager.configureManagerInstance().load(pFile);
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile", e);
            }
        } else {
            log.warn("Could not find {} config file", name);
        }

    }
    static String configFilename = "jmriconfig2.xml";  // usually overridden, this is default
    // The following MUST be protected for 3rd party applications 
    // (such as CATS) which are derived from this class.
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configOK;
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configDeferredLoadOK;
    // GUI members
    private JMenuBar menuBar;

    static String nameString = "JMRI program";

    protected static void setApplication(String name) {
        try {
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name", ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name", ex);
        }
    }

    /**
     * Set, log and return some startup information.
     * <p>
     * This method needs to be refactored, but it's in use (2/2014) by CATS so
     * can't easily be changed right away.
     *
     * @deprecated Since 3.7.1, use {@link #setStartupInfo(java.lang.String) }
     * plus {@link Log4JUtil#startupInfo(java.lang.String) }
     */
    @Deprecated
    protected static String startupInfo(String name) {
        setStartupInfo(name);
        return Log4JUtil.startupInfo(name);
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
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name", ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name", ex);
        }

        // Log the startup information
        log.info(Log4JUtil.startupInfo(name));
    }

    private void prepareFontLists() {
        // Prepare font lists
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Prepare font lists...");
                FontComboUtil.prepareFontLists();
                log.debug("...Font lists built");
            }
        }).start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        if (log.isDebugEnabled()) {
            log.debug("property change: comm port status update");
        }
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

    private final static Logger log = LoggerFactory.getLogger(Apps.class);
}

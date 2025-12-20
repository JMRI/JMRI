package jmri.implementation;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;

import jmri.util.prefs.JmriPreferencesActionFactory;

import jmri.Application;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.jmrit.XmlFile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.SystemType;
import jmri.util.com.sun.TransferActionListener;
import jmri.util.prefs.HasConnectionButUnableToConnectException;
import jmri.util.prefs.InitializationException;
import jmri.util.swing.JmriJOptionPane;

/**
 *
 * @author Randall Wood
 */
public class JmriConfigurationManager implements ConfigureManager {

    private final ConfigXmlManager legacy = new ConfigXmlManager();
    private final HashMap<PreferencesManager, InitializationException> initializationExceptions = new HashMap<>();
    /*
     * This list is in order of initialization and is used to display errors in
     * the order they appear.
     */
    private final List<PreferencesManager> initialized = new ArrayList<>();
    /*
     * This set is used to prevent a stack overflow by preventing
     * initializeProvider from recursively being called with the same provider.
     */
    private final Set<PreferencesManager> initializing = new HashSet<>();

    public JmriConfigurationManager() {
        ServiceLoader<PreferencesManager> sl = ServiceLoader.load(PreferencesManager.class);
        for (PreferencesManager pp : sl) {
            InstanceManager.store(pp, PreferencesManager.class);

            for (Class<?> provided : pp.getProvides()) {
                InstanceManager.storeUnchecked(pp, provided);
            }

        }
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            this.legacy.setPrefsLocation(new File(profile.getPath(), Profile.CONFIG_FILENAME));
        }
        if (!GraphicsEnvironment.isHeadless()) {
            ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
        }
    }

    @Override
    public void registerPref(Object o) {
        if ((o instanceof PreferencesManager)) {
            InstanceManager.store((PreferencesManager) o, PreferencesManager.class);
        }
        this.legacy.registerPref(o);
    }

    @Override
    public void removePrefItems() {
        this.legacy.removePrefItems();
    }

    @Override
    public void registerConfig(Object o) {
        this.legacy.registerConfig(o);
    }

    @Override
    public void registerConfig(Object o, int x) {
        this.legacy.registerConfig(o, x);
    }

    @Override
    public void registerTool(Object o) {
        this.legacy.registerTool(o);
    }

    @Override
    public void registerUser(Object o) {
        this.legacy.registerUser(o);
    }

    @Override
    public void registerUserPrefs(Object o) {
        this.legacy.registerUserPrefs(o);
    }

    @Override
    public void deregister(Object o) {
        this.legacy.deregister(o);
    }

    @Override
    public Object findInstance(Class<?> c, int index) {
        return this.legacy.findInstance(c, index);
    }

    @Override
    public List<Object> getInstanceList(Class<?> c) {
        return this.legacy.getInstanceList(c);
    }

    /**
     * Save preferences. Preferences are saved using either the
     * {@link jmri.util.prefs.JmriConfigurationProvider} or
     * {@link jmri.util.prefs.JmriPreferencesProvider} as appropriate to the
     * register preferences handler.
     */
    @Override
    public void storePrefs() {
        log.debug("Saving preferences...");
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        InstanceManager.getList(PreferencesManager.class).stream().forEach((o) -> {
            log.debug("Saving preferences for {}", o.getClass().getName());
            o.savePreferences(profile);
        });
    }

    /**
     * Save preferences. This method calls {@link #storePrefs() }.
     *
     * @param file Ignored.
     */
    @Override
    public void storePrefs(File file) {
        this.storePrefs();
    }

    @Override
    public void storeUserPrefs(File file) {
        this.legacy.storeUserPrefs(file);
    }

    @Override
    public boolean storeConfig(File file) {
        return this.legacy.storeConfig(file);
    }

    @Override
    public boolean storeUser(File file) {
        return this.legacy.storeUser(file);
    }

    @Override
    public boolean load(File file) throws JmriException {
        return this.load(file, false);
    }

    @Override
    public boolean load(URL url) throws JmriException {
        return this.load(url, false);
    }

    @Override
    public boolean load(File file, boolean registerDeferred) throws JmriException {
        return this.load(FileUtil.fileToURL(file), registerDeferred);
    }

    @Override
    public boolean load(URL url, boolean registerDeferred) throws JmriException {
        log.debug("loading {} ...", url);
        try {
            if (url == null
                    || (new File(url.toURI())).getName().equals(Profile.CONFIG_FILENAME)
                    || (new File(url.toURI())).getName().equals(Profile.CONFIG)) {
                Profile profile = ProfileManager.getDefault().getActiveProfile();
                List<PreferencesManager> providers = new ArrayList<>(InstanceManager.getList(PreferencesManager.class));
                providers.stream()
                        // sorting is a best-effort attempt to ensure that the
                        // more providers a provider relies on the later it will
                        // be initialized; this should tend to cause providers
                        // that list explicit requirements get run before providers
                        // attempting to force themselves to run last by requiring
                        // all providers
                        .sorted(Comparator.comparingInt(p -> p.getRequires().size()))
                        .forEachOrdered(provider -> initializeProvider(provider, profile));
                if (!this.initializationExceptions.isEmpty()) {
                    handleInitializationExceptions(profile);
                }
                if (url != null && (new File(url.toURI())).getName().equals(Profile.CONFIG_FILENAME)) {
                    log.debug("Loading legacy configuration...");
                    return this.legacy.load(url, registerDeferred);
                }
                return this.initializationExceptions.isEmpty();
            }
        } catch (URISyntaxException ex) {
            log.error("Unable to get File for {}", url);
            throw new JmriException(ex.getMessage(), ex);
        }
        // make this url the default "Store Panels..." file
        try {
            JFileChooser ufc = jmri.configurexml.StoreXmlUserAction.getUserFileChooser();
            ufc.setSelectedFile(new File(FileUtil.urlToURI(url)));
        } catch (Exception e) {
            // A user was seeing an IndexOutOfBoundsException in the setSelectedFile above
            // when loading a file at startup.  
            // We don't know why, but see https://stackoverflow.com/questions/37322892/jfilechooser-java-lang-indexoutofboundsexception-invalid-index
            // and https://web.archive.org/web/20170924021323/http://bugs.java.com/view_bug.do?bug_id=6684952
            // This lets operation proceed past that exception.
            log.error("Exception caught while setting default load file in file chooser: {}", e.toString());
        }

        return this.legacy.load(url, registerDeferred);
        // return true; // always return true once legacy support is dropped
    }

    private void handleInitializationExceptions(Profile profile) {
        if (!GraphicsEnvironment.isHeadless()) {

            AtomicBoolean isUnableToConnect = new AtomicBoolean(false);

            List<String> errors = new ArrayList<>();
            this.initialized.forEach((provider) -> {
                List<Exception> exceptions = provider.getInitializationExceptions(profile);
                if (!exceptions.isEmpty()) {
                    exceptions.forEach((exception) -> {
                        if (exception instanceof HasConnectionButUnableToConnectException) {
                            isUnableToConnect.set(true);
                        }
                        errors.add(exception.getLocalizedMessage());
                    });
                } else if (this.initializationExceptions.get(provider) != null) {
                    errors.add(this.initializationExceptions.get(provider).getLocalizedMessage());
                }
            });
            Object list = getErrorListObject(errors);

            if (isUnableToConnect.get()) {
                handleConnectionError(errors, list);
            } else {
                displayErrorListDialog(list);
            }
        }
    }

    private Object getErrorListObject(List<String> errors) {
        Object list;
        if (errors.size() == 1) {
            list = errors.get(0);
        } else {
            list = new JList<>(errors.toArray(new String[0]));
        }
        return list;
    }

    protected void displayErrorListDialog(Object list) {
        JmriJOptionPane.showMessageDialog(null,
                new Object[]{
                    (list instanceof JList) ? Bundle.getMessage("InitExMessageListHeader") : null,
                    list,
                    "<html><br></html>", // Add a visual break between list of errors and notes // NOI18N
                    Bundle.getMessage("InitExMessageLogs"), // NOI18N
                    Bundle.getMessage("InitExMessagePrefs"), // NOI18N
                },
                Bundle.getMessage("InitExMessageTitle", Application.getApplicationName()), // NOI18N
                JmriJOptionPane.ERROR_MESSAGE);
            InstanceManager.getDefault(JmriPreferencesActionFactory.class)
                    .getDefaultAction().actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
    }

    /**
     * Show a dialog with options Quit, Restart, Change profile, Edit connections
     * @param errors the list of error messages
     * @param list A JList or a String with error message(s)
     */
    private void handleConnectionError(List<String> errors, Object list) {
        List<String> errorList = errors;

        errorList.add(" "); // blank line below errors
        errorList.add(Bundle.getMessage("InitExMessageLogs"));

        Object[] options = generateErrorDialogButtonOptions();

        if (list instanceof JList) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem copyMenuItem = buildCopyMenuItem((JList<?>) list);
            popupMenu.add(copyMenuItem);

            JMenuItem copyAllMenuItem = buildCopyAllMenuItem((JList<?>) list);
            popupMenu.add(copyAllMenuItem);

            ((JList<?>) list).setComponentPopupMenu(popupMenu);

            ((JList<?>) list).addListSelectionListener((ListSelectionEvent e) -> copyMenuItem.setEnabled(((JList<?>)e.getSource()).getSelectedIndex() != -1));
        }

        handleRestartSelection(getjOptionPane(list, options));

    }

    // see order of generateErrorDialogButtonOptions()
    // -1 - dialog closed, 0 - quit, 1 - continue, 2 - editconns
    private void handleRestartSelection(int selectedValue) {
        if (selectedValue == 0) {
            // Exit program
            handleQuit();

        } else if (selectedValue == 1 || selectedValue == -1 ) {
            // Do nothing. Let the program continue

        } else if (selectedValue == 2) {
           if (isEditDialogRestart()) {
               handleRestart();
           } else {
                // Quit program
                handleQuit();
            }

        } else {
            // Exit program
            handleQuit();
        }
    }

    protected boolean isEditDialogRestart() {
        return false;
    }

    protected void handleRestart() {
        // Restart program
        try {
            InstanceManager.getDefault(jmri.ShutDownManager.class).restart();
        } catch (Exception er) {
            log.error("Continuing after error in handleRestart", er);
        }
    }


    private int getjOptionPane(Object list, Object[] options) {
        return JmriJOptionPane.showOptionDialog(
            null, 
            new Object[] {
                (list instanceof JList) ? Bundle.getMessage("InitExMessageListHeader") : null,
                list,
                "<html><br></html>", // Add a visual break between list of errors and notes
                Bundle.getMessage("InitExMessageLogs"),
                Bundle.getMessage("ErrorDialogConnectLayout")}, 
            Bundle.getMessage("InitExMessageTitle", Application.getApplicationName()), 
            JmriJOptionPane.DEFAULT_OPTION, 
            JmriJOptionPane.ERROR_MESSAGE, 
            null, 
            options, 
            null);
    }

    private JMenuItem buildCopyAllMenuItem(JList<?> list) {
        JMenuItem copyAllMenuItem = new JMenuItem(Bundle.getMessage("MenuItemCopyAll"));
        ActionListener copyAllActionListener = (ActionEvent e) -> {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                text.append(list.getModel().getElementAt(i).toString());
                text.append(System.getProperty("line.separator")); // NOI18N
            }
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(new StringSelection(text.toString()), null);
        };
        copyAllMenuItem.setActionCommand("copyAll"); // NOI18N
        copyAllMenuItem.addActionListener(copyAllActionListener);
        return copyAllMenuItem;
    }

    private JMenuItem buildCopyMenuItem(JList<?> list) {
        JMenuItem copyMenuItem = new JMenuItem(Bundle.getMessage("MenuItemCopy"));
        TransferActionListener copyActionListener = new TransferActionListener();
        copyMenuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        copyMenuItem.addActionListener(copyActionListener);
        if (SystemType.isMacOSX()) {
            copyMenuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.META_MASK));
        } else {
            copyMenuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        }
        copyMenuItem.setMnemonic(KeyEvent.VK_C);
        copyMenuItem.setEnabled(list.getSelectedIndex() != -1);
        return copyMenuItem;
    }

    private Object[] generateErrorDialogButtonOptions() {
        return new Object[] {
                Bundle.getMessage("ErrorDialogButtonQuitProgram", Application.getApplicationName()),
                Bundle.getMessage("ErrorDialogButtonContinue"),
                Bundle.getMessage("ErrorDialogButtonEditConnections")
            };
    }

    protected void handleQuit(){
        try {
            InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit", e);
        }
    }

    @Override
    public boolean loadDeferred(File file) {
        return this.legacy.loadDeferred(file);
    }

    @Override
    public boolean loadDeferred(URL file) {
        return this.legacy.loadDeferred(file);
    }

    @Override
    public URL find(String filename) {
        return this.legacy.find(filename);
    }

    @Override
    public boolean makeBackup(File file) {
        return this.legacy.makeBackup(file);
    }

    private void initializeProvider(PreferencesManager provider, Profile profile) {
        if (!initializing.contains(provider) && !provider.isInitialized(profile) && !provider.isInitializedWithExceptions(profile)) {
            initializing.add(provider);
            log.debug("Initializing provider {}", provider.getClass());
            provider.getRequires()
                    .forEach(c -> InstanceManager.getList(c)
                            .forEach(p -> initializeProvider(p, profile)));
            try {
                provider.initialize(profile);
            } catch (InitializationException ex) {
                // log all initialization exceptions, but only retain for GUI display the
                // first initialization exception for a provider
                if (this.initializationExceptions.putIfAbsent(provider, ex) == null) {
                    log.error("Exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                } else {
                    log.error("Additional exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                }
            }
            this.initialized.add(provider);
            log.debug("Initialized provider {}", provider.getClass());
            initializing.remove(provider);
        }
    }

    public HashMap<PreferencesManager, InitializationException> getInitializationExceptions() {
        return new HashMap<>(initializationExceptions);
    }

    @Override
    public void setValidate(XmlFile.Validate v) {
        legacy.setValidate(v);
    }

    @Override
    public XmlFile.Validate getValidate() {
        return legacy.getValidate();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriConfigurationManager.class);

}

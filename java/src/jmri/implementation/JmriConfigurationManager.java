package jmri.implementation;

import apps.gui3.TabbedPreferencesAction;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import javax.swing.JList;
import javax.swing.JOptionPane;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.spi.PreferencesProvider;
import jmri.util.FileUtil;
import jmri.util.prefs.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JmriConfigurationManager implements ConfigureManager {

    private final static Logger log = LoggerFactory.getLogger(JmriConfigurationManager.class);
    private final ConfigXmlManager legacy = new ConfigXmlManager();
    private final HashMap<PreferencesProvider, InitializationException> initializationExceptions = new HashMap<>();

    @SuppressWarnings("unchecked") // For types in InstanceManager.store()
    public JmriConfigurationManager() {
        ServiceLoader<PreferencesProvider> sl = ServiceLoader.load(PreferencesProvider.class);
        for (PreferencesProvider pp : sl) {
            InstanceManager.store(pp, PreferencesProvider.class);
            for (@SuppressWarnings("rawtypes") Class provided : pp.getProvides()) { // use raw class so next line can compile
                InstanceManager.store(provided.cast(pp), provided);
            }
        }
        if (ProfileManager.getDefault().getActiveProfile() != null) {
            this.legacy.setPrefsLocation(new File(ProfileManager.getDefault().getActiveProfile().getPath(), Profile.CONFIG_FILENAME));
        }
        if (!GraphicsEnvironment.isHeadless()) {
            ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
        }
    }

    @Override
    public void registerPref(Object o) {
        if ((o instanceof PreferencesProvider)) {
            InstanceManager.store((PreferencesProvider) o, PreferencesProvider.class);
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
    public ArrayList<Object> getInstanceList(Class<?> c) {
        return this.legacy.getInstanceList(c);
    }

    @Override
    public boolean storeAll(File file) {
        return this.legacy.storeAll(file);
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
        InstanceManager.getList(PreferencesProvider.class).stream().forEach((o) -> {
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
    public boolean load(URL file) throws JmriException {
        return this.load(file, false);
    }

    @Override
    public boolean load(File file, boolean registerDeferred) throws JmriException {
        return this.load(FileUtil.fileToURL(file), registerDeferred);
    }

    @Override
    public boolean load(URL file, boolean registerDeferred) throws JmriException {
        log.debug("loading {} ...", file);
        try {
            if (file == null
                    || (new File(file.toURI())).getName().equals("ProfileConfig.xml") //NOI18N
                    || (new File(file.toURI())).getName().equals(Profile.CONFIG)) {
                List<PreferencesProvider> providers = new ArrayList<>(InstanceManager.getList(PreferencesProvider.class));
                providers.stream().forEach((provider) -> {
                    this.initializeProvider(provider, ProfileManager.getDefault().getActiveProfile());
                });
                if (!this.initializationExceptions.isEmpty()) {
                    if (!GraphicsEnvironment.isHeadless()) {
                        String[] errors = new String[this.initializationExceptions.size()];
                        int i = 0;
                        for (InitializationException e : this.initializationExceptions.values()) {
                            errors[i] = e.getLocalizedMessage();
                            i++;
                        }
                        Object list;
                        if (this.initializationExceptions.size() == 1) {
                            list = errors[0];
                        } else {
                            list = new JList<>(errors);
                        }
                        JOptionPane.showMessageDialog(null,
                                new Object[]{
                                    list,
                                    "<html><br></html>", // Add a visual break between list of errors and notes // NOI18N
                                    Bundle.getMessage("InitExMessageLogs"), // NOI18N
                                    Bundle.getMessage("InitExMessagePrefs"), // NOI18N
                                },
                                Bundle.getMessage("InitExMessageTitle", Application.getApplicationName()), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        (new TabbedPreferencesAction()).actionPerformed();
                    }
                }
                if (file != null && (new File(file.toURI())).getName().equals("ProfileConfig.xml")) { // NOI18N
                    log.debug("Loading legacy configuration...");
                    return this.legacy.load(file, registerDeferred);
                }
                return this.initializationExceptions.isEmpty();
            }
        } catch (URISyntaxException ex) {
            log.error("Unable to get File for {}", file);
            throw new JmriException(ex.getMessage(), ex);
        }
        return this.legacy.load(file, registerDeferred);
        // return true; // always return true once legacy support is dropped
    }

    @Override
    public boolean loadDeferred(File file) throws JmriException {
        return this.legacy.loadDeferred(file);
    }

    @Override
    public boolean loadDeferred(URL file) throws JmriException {
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

    private boolean initializeProvider(PreferencesProvider provider, Profile profile) {
        if (!provider.isInitialized(profile)) {
            log.debug("Initializing provider {}", provider.getClass());
            for (Class<? extends PreferencesProvider> c : provider.getRequires()) {
                InstanceManager.getList(c).stream().forEach((p) -> {
                    this.initializeProvider(p, profile);
                });
            }
            try {
                provider.initialize(profile);
            } catch (InitializationException ex) {
                // log all initialization exceptions, but only retain for GUI display the
                // first initialization exception for a provider
                InitializationException put = this.initializationExceptions.putIfAbsent(provider, ex);
                log.error("Exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                if (put != null) {
                    log.error("Additional exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                }
            }
            log.debug("Initialized provider {}", provider.getClass());
        }
        return provider.isInitialized(profile);
    }

    public HashMap<PreferencesProvider, InitializationException> getInitializationExceptions() {
        return new HashMap<>(initializationExceptions);
    }
}

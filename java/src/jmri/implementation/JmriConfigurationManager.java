package jmri.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.spi.InitializationException;
import jmri.spi.PreferencesProvider;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JmriConfigurationManager implements ConfigureManager {

    private final static Logger log = LoggerFactory.getLogger(JmriConfigurationManager.class);
    private ConfigXmlManager legacy = new ConfigXmlManager();
    private final HashMap<PreferencesProvider, InitializationException> initializationExceptions = new HashMap<>();

    public JmriConfigurationManager() {
        ServiceLoader<PreferencesProvider> sl = ServiceLoader.load(PreferencesProvider.class);
        for (PreferencesProvider pp : sl) {
            InstanceManager.store(pp, PreferencesProvider.class);
            for (Class provided : pp.getProvides()) { // use raw class so next line can compile
                InstanceManager.store(provided.cast(pp), provided);
            }
        }
        try {
            List<String> classNames = (new ObjectMapper()).readValue(
                    ResourceBundle.getBundle("apps.PreferencesProviders").getString("PreferencesProviders"),
                    new TypeReference<List<String>>() {
                    });
            classNames.stream().forEach((String className) -> {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (PreferencesProvider.class.isAssignableFrom(clazz)) {
                        PreferencesProvider pp = (PreferencesProvider) clazz.newInstance();
                        InstanceManager.store(pp, PreferencesProvider.class);
                        for (Class provided : pp.getProvides()) { // use raw class so next line can compile
                            InstanceManager.store(provided.cast(pp), provided);
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("Unable to add preferences class (" + className + ")", e);
                }
            });
        } catch (IOException e) {
            log.error("Unable to parse PreferenceProviders property", e);
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
        } else {
            this.legacy.registerPref(o);
        }
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
        log.debug("Saving backwards compatible preferences...");
        this.legacy.storePrefs();
    }

    /**
     * Save preferences. This method calls {@link #storePrefs() }.
     *
     * @param file Ignored.
     */
    @Override
    public void storePrefs(File file) {
        // only call storePrefs() once legacy is removed
        log.debug("Saving preferences...");
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        InstanceManager.getList(PreferencesProvider.class).stream().forEach((o) -> {
            log.debug("Saving preferences for {}", o.getClass().getName());
            o.savePreferences(profile);
        });
        log.debug("Saving backwards compatible preferences...");
        this.legacy.storePrefs(file);
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
        try {
            if (file == null || (new File(file.toURI())).getName().equals("ProfileConfig.xml")) {
                List<PreferencesProvider> providers = new ArrayList<>(InstanceManager.getList(PreferencesProvider.class));
                providers.stream().forEach((provider) -> {
                    this.initializeProvider(provider, ProfileManager.getDefault().getActiveProfile());
                });
                if (!this.initializationExceptions.isEmpty()) {
                    // TODO: Display list of errors
                    // TODO: Open TabbedPreferences
                }
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
                // only log an initializations exception for a provider the first times
                if (!this.initializationExceptions.containsKey(provider)) {
                    log.error("Exception initializing {}: {}", provider.getClass().getName(), this.getInitializationExceptions().put(provider, ex).getMessage());
                }
            }
        }
        return provider.isInitialized(profile);
    }

    public HashMap<PreferencesProvider, InitializationException> getInitializationExceptions() {
        return initializationExceptions;
    }
}

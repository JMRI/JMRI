package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammerManager;
import jmri.CommandStation;
import jmri.ConfigureManager;
import jmri.ConsistManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.ThrottleManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Records and executes a desired set of defaults for the JMRI InstanceManager
 * and ProxyManagers.
 * <p>
 * Provided that a connection provides a default, this verifies, unless the
 * per-profile property {@code jmri-managers.allInternalDefaults} is
 * {@code true}, that a non-Internal connection (other than type None in the
 * preferences window) is the default for at least one type of manager.
 * <p>
 * allInternalDefaults is preserved as a preference when set here, but
 * {@link #setAllInternalDefaultsValid} is not (originally) invoked from the
 * GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2015, 2017
 * @since 2.9.4
 * @see jmri.jmrix.SystemConnectionMemo#provides(java.lang.Class)
 */
@ServiceProvider(service = PreferencesManager.class)
public class ManagerDefaultSelector extends AbstractPreferencesManager {

    public final HashMap<Class<?>, String> defaults = new HashMap<>();
    private PropertyChangeListener memoListener;
    private boolean allInternalDefaultsValid = false;
    public final static String ALL_INTERNAL_DEFAULTS = "allInternalDefaults";

    public ManagerDefaultSelector() {
        memoListener = (PropertyChangeEvent e) -> {
            log.trace("memoListener fired via {}", e);
            switch (e.getPropertyName()) {
                case SystemConnectionMemo.USER_NAME:
                    String oldName = (String) e.getOldValue();
                    String newName = (String) e.getNewValue();
                    log.debug("ConnectionNameChanged from \"{}\" to \"{}\"", oldName, newName);
                    // Takes a copy of the keys to avoid ConcurrentModificationException.
                    new HashSet<>(defaults.keySet()).forEach((c) -> {
                        String connectionName = this.defaults.get(c);
                        if (connectionName.equals(oldName)) {
                            ManagerDefaultSelector.this.defaults.put(c, newName);
                        }
                    });
                    this.firePropertyChange("Updated", null, null);
                    break;
                case SystemConnectionMemo.DISABLED:
                    Boolean newState = (Boolean) e.getNewValue();
                    if (newState) {
                        String disabledName = ((SystemConnectionMemo) e.getSource()).getUserName();
                        log.debug("ConnectionDisabled true: \"{}\"", disabledName);
                        removeConnectionAsDefault(disabledName);
                    }
                    break;
                default:
                    log.debug("ignoring notification of \"{}\"", e.getPropertyName());
                    break;
            }
        };
        SystemConnectionMemoManager.getDefault().addPropertyChangeListener((PropertyChangeEvent e) -> {
            //
            // Note that when JMRI is starting, this listener does
            // trigger as connections are added, but that after the
            // configured connections are set, the defaults are reset
            // when configure(Profile) is called. We do, however,
            // want these to be set immediately when a new profile
            // is launched for the first time, so these listeners
            // need to be in place as early as possible.
            //
            log.trace("addPropertyChangeListener fired via {}", e);
            switch (e.getPropertyName()) {
                case SystemConnectionMemoManager.CONNECTION_REMOVED:
                    if (e.getOldValue() instanceof SystemConnectionMemo) {
                        SystemConnectionMemo memo = (SystemConnectionMemo) e.getOldValue();
                        String removedName = ((SystemConnectionMemo) e.getOldValue()).getUserName();
                        log.debug("ConnectionRemoved for \"{}\"", removedName);
                        removeConnectionAsDefault(removedName);
                        memo.removePropertyChangeListener(this.memoListener);
                    }
                    break;
                case SystemConnectionMemoManager.CONNECTION_ADDED:
                    if (e.getNewValue() instanceof SystemConnectionMemo) {
                        SystemConnectionMemo memo = (SystemConnectionMemo) e.getNewValue();
                        memo.addPropertyChangeListener(this.memoListener);
                        // check for special case of anything else then Internal
                        // and set first system to be default for all provided defaults
                        List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);

                        if (log.isDebugEnabled()) {
                            log.debug("Start CONNECTION_ADDED processing with {} existing", list.size());
                            for (int i = 0; i < list.size(); i++) {
                                log.debug("    System {}: {}", i, list.get(i));
                            }
                        }

                        if ((list.size() == 1 && !(list.get(0) instanceof InternalSystemConnectionMemo)) ||
                                (list.size() == 2 && !(list.get(0) instanceof InternalSystemConnectionMemo) && list.get(1) instanceof InternalSystemConnectionMemo)) {
                            // first system added is hardware, gets defaults for everything it supports
                            log.debug("First real system added, reset defaults");
                            for (Item item : knownManagers) {
                                if (memo.provides(item.managerClass)) {
                                    this.setDefault(item.managerClass, memo.getUserName());
                                }
                            }
                        }
                        // any new connection that provides a missing default
                        // gets set as the default for that missing default
                        // use new HashSet over this.defaults.keySet to avoid
                        // ConcurrentModificationException on this.defaults
                        new HashSet<>(defaults.keySet()).forEach((cls) -> {
                            String userName = defaults.get(cls);
                            if (userName == null && memo.provides(cls)) {
                                this.setDefault(cls, memo.getUserName());
                            }
                        });
                    }
                    break;
                default:
                    log.debug("ignoring notification of \"{}\"", e.getPropertyName());
                    break;
            }
        });
        InstanceManager.getList(SystemConnectionMemo.class).forEach((memo) -> {
            memo.addPropertyChangeListener(this.memoListener);
        });
    }

    // remove connection's record
    void removeConnectionAsDefault(String removedName) {
        ArrayList<Class<?>> tmpArray = new ArrayList<>();
        defaults.keySet().stream().forEach((c) -> {
            String connectionName = ManagerDefaultSelector.this.defaults.get(c);
            if (connectionName.equals(removedName)) {
                log.debug("Connection {} has been removed as the default for {}", removedName, c);
                tmpArray.add(c);
            }
        });
        tmpArray.stream().forEach((tmpArray1) -> {
            ManagerDefaultSelector.this.defaults.remove(tmpArray1);
        });
        this.firePropertyChange("Updated", null, null);
    }

    /**
     * Return the userName of the system that provides the default instance for
     * a specific class.
     *
     * @param managerClass the specific type, for example, TurnoutManager, for
     *                     which a default system is desired
     * @return userName of the system, or null if none set
     */
    public String getDefault(Class<?> managerClass) {
        return defaults.get(managerClass);
    }

    /**
     * Record the userName of the system that provides the default instance for
     * a specific class.
     * <p>
     * To ensure compatibility of different preference versions, only classes
     * that are current registered are preserved. This way, reading in an old
     * file will just have irrelevant items ignored.
     *
     * @param managerClass the specific type, for example, TurnoutManager, for
     *                     which a default system is desired
     * @param userName     of the system, or null if none set
     */
    public void setDefault(Class<?> managerClass, String userName) {
        for (Item item : knownManagers) {
            if (item.managerClass.equals(managerClass)) {
                log.debug("   setting default for \"{}\" to \"{}\" by request", managerClass, userName);
                defaults.put(managerClass, userName);
                return;
            }
        }
        log.warn("Ignoring preference for class {} with name {}", managerClass, userName);
    }

    /**
     * Load into InstanceManager
     *
     * @param profile the profile to configure against
     * @return an exception that can be passed to the user or null if no errors
     *         occur
     */
    @CheckForNull
    public InitializationException configure(Profile profile) {
        InitializationException error = null;
        List<SystemConnectionMemo> connList = InstanceManager.getList(SystemConnectionMemo.class);
        log.debug("configure defaults into InstanceManager from {} memos, {} defaults", connList.size(), defaults.keySet().size());
        // Takes a copy to avoid ConcurrentModificationException.
        Set<Class<?>> keys = new HashSet<>(defaults.keySet());
        for (Class<?> c : keys) {
            // 'c' is the class to load
            String connectionName = defaults.get(c);
            // have to find object of that type from proper connection
            boolean found = false;
            for (SystemConnectionMemo memo : connList) {
                String testName = memo.getUserName();
                if (testName.equals(connectionName)) {
                    found = true;
                    // match, store
                    try {
                        if (memo.provides(c)) {
                            log.debug("   setting default for \"{}\" to \"{}\" in configure", c, memo.get(c));
                            InstanceManager.setDefault(c, memo.get(c));
                        }
                    } catch (NullPointerException ex) {
                        String englishMsg = Bundle.getMessage(Locale.ENGLISH, "ErrorNullDefault", memo.getUserName(), c); // NOI18N
                        String localizedMsg = Bundle.getMessage("ErrorNullDefault", memo.getUserName(), c); // NOI18N
                        error = new InitializationException(englishMsg, localizedMsg);
                        log.warn("SystemConnectionMemo for {} ({}) provides a null {} instance", memo.getUserName(), memo.getClass(), c);
                    }
                    break;
                } else {
                    log.debug("   memo name didn't match: {} vs {}", testName, connectionName);
                }
            }
            /*
             * If the set connection can not be found then we shall set the manager default to use what
             * has currently been set.
             */
            if (!found) {
                log.debug("!found, so resetting");
                String currentName = null;
                if (c == ThrottleManager.class && InstanceManager.getOptionalDefault(ThrottleManager.class).isPresent()) {
                    currentName = InstanceManager.throttleManagerInstance().getUserName();
                } else if (c == PowerManager.class && InstanceManager.getOptionalDefault(PowerManager.class).isPresent()) {
                    currentName = InstanceManager.getDefault(PowerManager.class).getUserName();
                }
                if (currentName != null) {
                    log.warn("The configured {} for {} can not be found so will use the default {}", connectionName, c, currentName);
                    this.defaults.put(c, currentName);
                }
            }
        }
        if (!isPreferencesValid(profile, connList)) {
            error = new InitializationException(Bundle.getMessage(Locale.ENGLISH, "ManagerDefaultSelector.AllInternal"), Bundle.getMessage("ManagerDefaultSelector.AllInternal"));
        }
        return error;
    }

    // Define set of items that we remember defaults for, manually maintained because
    // there are lots of JMRI-internal types of no interest to the user and/or not system-specific.
    // This grows if you add something to the SystemConnectionMemo system
    final public Item[] knownManagers = new Item[]{
        new Item("<html>Throttles</html>", ThrottleManager.class),
        new Item("<html>Power<br>Control</html>", PowerManager.class),
        new Item("<html>Command<br>Station</html>", CommandStation.class),
        new Item("<html>Service<br>Programmer</html>", GlobalProgrammerManager.class),
        new Item("<html>Ops Mode<br>Programmer</html>", AddressedProgrammerManager.class),
        new Item("<html>Consists</html>", ConsistManager.class)
    };

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true); // NOI18N
            Preferences defaultsPreferences = preferences.node("defaults");
            try {
                for (String name : defaultsPreferences.keys()) {
                    String connection = defaultsPreferences.get(name, null);
                    Class<?> cls = this.classForName(name);
                    log.debug("Loading default {} for {}", connection, name);
                    if (cls != null) {
                        this.defaults.put(cls, connection);
                        log.debug("Loaded default {} for {}", connection, cls);
                    }
                }
                this.allInternalDefaultsValid = preferences.getBoolean(ALL_INTERNAL_DEFAULTS, this.allInternalDefaultsValid);
            } catch (BackingStoreException ex) {
                log.info("Unable to read preferences for Default Selector.");
            }
            InitializationException ex = this.configure(profile);
            InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent((manager) -> {
                manager.registerPref(this); // allow profile configuration to be written correctly
            });
            this.setInitialized(profile, true);
            if (ex != null) {
                this.addInitializationException(profile, ex);
                throw ex;
            }
        }
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true); // NOI18N
        Preferences defaultsPreferences = preferences.node("defaults");
        try {
            this.defaults.keySet().stream().forEach((cls) -> {
                defaultsPreferences.put(this.nameForClass(cls), this.defaults.get(cls));
            });
            preferences.putBoolean(ALL_INTERNAL_DEFAULTS, this.allInternalDefaultsValid);
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences for Default Selector.", ex);
        }
    }

    private boolean isPreferencesValid(Profile profile, List<SystemConnectionMemo> connections) {
        log.trace("isPreferencesValid start");
        if (allInternalDefaultsValid) {
            log.trace("allInternalDefaultsValid returns true");
            return true;
        }
        boolean usesExternalConnections = false;

        // classes of managers being provided, and set of which SystemConnectionMemos can provide each
        Map<Class<?>, Set<SystemConnectionMemo>> providing = new HashMap<>();

        // list of all external providers (i.e. SystemConnectionMemos) that provide at least one known manager type
        Set<SystemConnectionMemo> providers = new HashSet<>();

        if (connections.size() > 1) {
            connections.stream().filter((memo) -> (!(memo instanceof InternalSystemConnectionMemo))).forEachOrdered((memo) -> {
                // populate providers by adding all external (non-internal) connections that provide at least one default
                for (Item item : knownManagers) {
                    if (memo.provides(item.managerClass)) {
                        providers.add(memo);
                        break;
                    }
                }
            });
            // if there are no external providers of managers, no further checks are needed
            if (providers.size() >= 1) {
                // build a list of defaults provided by external connections
                providers.stream().forEach((memo) -> {
                    for (Item item : knownManagers) {
                        if (memo.provides(item.managerClass)) {
                            Set<SystemConnectionMemo> provides = providing.getOrDefault(item.managerClass, new HashSet<>());
                            provides.add(memo);
                            providing.put(item.managerClass, provides);
                        }
                    }
                });

                if (log.isDebugEnabled()) {
                    // avoid unneeded overhead of looping through providers
                    providing.forEach((cls, clsProviders) -> {
                        log.debug("{} default provider is {}, is provided by:", cls.getName(), defaults.get(cls));
                        clsProviders.forEach((provider) -> {
                            log.debug("    {}", provider.getUserName());
                        });
                    });
                }

                for (SystemConnectionMemo memo : providers) {
                    if (providing.keySet().stream().filter((cls) -> {
                        Set<SystemConnectionMemo> provides = providing.get(cls);
                        log.debug("{} is provided by {} out of {} connections", cls.getName(), provides.size(), providers.size());
                        log.trace("memo stream returns {} due to producers.size() {}", (provides.size() > 0), provides.size());
                        return (provides.size() > 0);
                    }).anyMatch((cls) -> {
                        log.debug("{} has an external default", cls);
                        if (defaults.get(cls) == null) {
                            log.trace("memo stream returns true because there's no default defined and an external provider exists");
                            return true;
                        }
                        log.trace("memo stream returns {} due to memo.getUserName() {} and {}", (memo.getUserName().equals(defaults.get(cls))), memo.getUserName(), defaults.get(cls));
                        return memo.getUserName().equals(defaults.get(cls));
                    })) {
                        log.trace("setting usesExternalConnections true");
                        usesExternalConnections = true;
                        // no need to check further
                        break;
                    }
                }
            }
        }
        log.trace("method end returns {} due to providers.size() {} and usesExternalConnections {}", (providers.size() >= 1 ? usesExternalConnections : true), providers.size(), usesExternalConnections);
        return providers.size() >= 1 ? usesExternalConnections : true;
    }

    public boolean isPreferencesValid(Profile profile) {
        return isPreferencesValid(profile, InstanceManager.getList(SystemConnectionMemo.class));
    }

    public static class Item {

        public String typeName;
        public Class<?> managerClass;

        Item(String typeName, Class<?> managerClass) {
            this.typeName = typeName;
            this.managerClass = managerClass;
        }
    }

    private String nameForClass(@Nonnull Class<?> cls) {
        return cls.getCanonicalName().replace('.', '-');
    }

    private Class<?> classForName(@Nonnull String name) {
        try {
            return Class.forName(name.replace('-', '.'));
        } catch (ClassNotFoundException ex) {
            log.error("Could not find class for {}", name);
            return null;
        }
    }

    /**
     * Check if having all defaults assigned to internal connections should be
     * considered is valid in the presence of an external System Connection.
     *
     * @return true if having all internal defaults should be valid; false
     *         otherwise
     */
    public boolean isAllInternalDefaultsValid() {
        return allInternalDefaultsValid;
    }

    /**
     * Set if having all defaults assigned to internal connections should be
     * considered is valid in the presence of an external System Connection.
     *
     * @param isAllInternalDefaultsValid true if having all internal defaults
     *                                   should be valid; false otherwise
     */
    public void setAllInternalDefaultsValid(boolean isAllInternalDefaultsValid) {
        this.allInternalDefaultsValid = isAllInternalDefaultsValid;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagerDefaultSelector.class);
}

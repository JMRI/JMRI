package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
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
import jmri.ProgrammerManager;
import jmri.ThrottleManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records and executes a desired set of defaults for the JMRI InstanceManager
 * and ProxyManagers
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2015
 * @since 2.9.4
 */
public class ManagerDefaultSelector extends AbstractPreferencesManager {

    public final Hashtable<Class<?>, String> defaults = new Hashtable<>();

    public ManagerDefaultSelector() {
        SystemConnectionMemo.addPropertyChangeListener((PropertyChangeEvent e) -> {
            switch (e.getPropertyName()) {
                case "ConnectionNameChanged":
                    String oldName = (String) e.getOldValue();
                    String newName = (String) e.getNewValue();
                    log.debug("ConnectionNameChanged from \"{}\" to \"{}\"", oldName, newName);
                    defaults.keySet().stream().forEach((c) -> {
                        String connectionName = this.defaults.get(c);
                        if (connectionName.equals(oldName)) {
                            ManagerDefaultSelector.this.defaults.put(c, newName);
                        }
                    });
                    break;
                case "ConnectionDisabled":
                    Boolean newState = (Boolean) e.getNewValue();
                    if (newState) {
                        SystemConnectionMemo memo = (SystemConnectionMemo) e.getSource();
                        String disabledName = memo.getUserName();
                        log.debug("ConnectionDisabled true: \"{}\"", disabledName);
                        removeConnectionAsDefault(disabledName);
                    }
                    break;
                case "ConnectionRemoved":
                    String removedName = (String) e.getOldValue();
                    log.debug("ConnectionRemoved for \"{}\"", removedName);
                    removeConnectionAsDefault(removedName);
                    break;
                case "ConnectionAdded":
                    // check for special case of anything else then Internal
                    List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);
                    if (list.size() == 2 && list.get(1) instanceof InternalSystemConnectionMemo) {
                        log.debug("First real system added, reset defaults");
                        String name = list.get(1).getUserName();
                        removeConnectionAsDefault(name);
                    }
                    break;
                default:
                    log.debug("ignoring notification of \"{}\"", e.getPropertyName());
                    break;
            }
            this.firePropertyChange("Updated", null, null);
        });
    }

    // remove connection's record
    void removeConnectionAsDefault(String removedName) {
        ArrayList<Class<?>> tmpArray = new ArrayList<>();
        defaults.keySet().stream().forEach((c) -> {
            String connectionName = ManagerDefaultSelector.this.defaults.get(c);
            if (connectionName.equals(removedName)) {
                log.debug("Connection " + removedName + " has been removed as the default for " + c);
                tmpArray.add(c);
            }
        });
        tmpArray.stream().forEach((tmpArray1) -> {
            ManagerDefaultSelector.this.defaults.remove(tmpArray1);
        });
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
     *
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
     * @return an exception that can be passed to the user or null if no errors
     *         occur
     */
    @CheckForNull
    public InitializationException configure() {
        InitializationException error = null;
        List<SystemConnectionMemo> connList = InstanceManager.getList(SystemConnectionMemo.class);
        log.debug("configure defaults into InstanceManager from {} memos, {} defaults", connList.size(), defaults.keySet().size());
        for (Class<?> c : defaults.keySet()) {
            // 'c' is the class to load
            String connectionName = this.defaults.get(c);
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
                if (c == ThrottleManager.class && InstanceManager.getNullableDefault(ThrottleManager.class) != null) {
                    currentName = InstanceManager.throttleManagerInstance().getUserName();
                } else if (c == PowerManager.class && InstanceManager.getNullableDefault(PowerManager.class) != null) {
                    currentName = InstanceManager.getDefault(PowerManager.class).getUserName();
                } else if (c == ProgrammerManager.class && InstanceManager.getNullableDefault(ProgrammerManager.class) != null) {
                    currentName = InstanceManager.getDefault(ProgrammerManager.class).getUserName();
                }
                if (currentName != null) {
                    log.warn("The configured " + connectionName + " for " + c + " can not be found so will use the default " + currentName);
                    this.defaults.put(c, currentName);
                }
            }
        }
        return error;
    }

    // Define set of items that we remember defaults for, manually maintained because
    // there are lots of JMRI-internal types of no interest to the user and/or not system-specific.
    // This grows if you add something to the SystemConnectionMemo system
    final public Item[] knownManagers = new Item[]{
        new Item("Throttles", ThrottleManager.class),
        new Item("<html>Power<br>Control</html>", PowerManager.class),
        new Item("<html>Command<br>Station</html>", CommandStation.class),
        new Item("<html>Service<br>Programmer</html>", GlobalProgrammerManager.class),
        new Item("<html>Ops Mode<br>Programmer</html>", AddressedProgrammerManager.class),
        new Item("Consists ", ConsistManager.class)
    };

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences settings = ProfileUtils.getPreferences(profile, this.getClass(), true).node("defaults"); // NOI18N
            try {
                for (String name : settings.keys()) {
                    String connection = settings.get(name, null);
                    Class<?> cls = this.classForName(name);
                    log.debug("Loading default {} for {}", connection, name);
                    if (cls != null) {
                        this.defaults.put(cls, connection);
                        log.debug("Loaded default {} for {}", connection, cls);
                    }
                }
            } catch (BackingStoreException ex) {
                log.info("Unable to read preferences for Default Selector.");
            }
            InitializationException ex = this.configure();
            ConfigureManager manager = InstanceManager.getNullableDefault(ConfigureManager.class);
            if (manager != null) {
                manager.registerPref(this); // allow ProfileConfig.xml to be written correctly
            }
            this.setInitialized(profile, true);
            if (ex != null) {
                throw ex;
            }
        }
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences settings = ProfileUtils.getPreferences(profile, this.getClass(), true).node("defaults"); // NOI18N
        try {
            this.defaults.keySet().stream().forEach((cls) -> {
                settings.put(this.nameForClass(cls), this.defaults.get(cls));
            });
            settings.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences for Default Selector.", ex);
        }
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

    private final static Logger log = LoggerFactory.getLogger(ManagerDefaultSelector.class.getName());
}

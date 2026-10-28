package jmri.util.startup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.Disposable;
import jmri.InstanceManager;
import jmri.beans.Bean;
import jmri.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain a list of actions that can be used by
 * {@link jmri.util.startup.AbstractActionModel} and its descendants. This list is
 * populated by {@link StartupActionFactory} instances
 * registered with a {@link java.util.ServiceLoader}.
 *
 * @author Randall Wood Copyright 2016, 2020
 */
public class StartupActionModelUtil extends Bean implements Disposable {

    private HashMap<Class<?>, ActionAttributes> actions = null;
    private HashMap<String, Class<?>> overrides = null;
    private HashSet<Class<?>> serviceClasses = null; // offered by ServiceLoader factories, never removed
    private ArrayList<String> actionNames = null; // built on demand, invalidated in changes to actions
    private final PropertyChangeListener memosListener = this::memoChanged;
    private final PropertyChangeListener actionFactoryListener = this::actionFactoryChanged;
    private static final Logger log = LoggerFactory.getLogger(StartupActionModelUtil.class);

    /**
     * Get the default StartupActionModelUtil instance, creating it if
     * necessary.
     *
     * @return the default instance
     */
    @Nonnull
    public static StartupActionModelUtil getDefault() {
        return InstanceManager.getOptionalDefault(StartupActionModelUtil.class).orElseGet(() -> {
            return InstanceManager.setDefault(StartupActionModelUtil.class, new StartupActionModelUtil());
        });
    }

    public StartupActionModelUtil() {
        InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(SystemConnectionMemo.class), memosListener);
    }

    @CheckForNull
    public String getActionName(@Nonnull Class<?> clazz) {
        this.prepareActionsHashMap();
        ActionAttributes attrs = this.actions.get(clazz);
        return attrs != null ? attrs.name : null;
    }

    @CheckForNull
    public String getActionName(@Nonnull String className) {
        if (!className.isEmpty()) {
            try {
                return this.getActionName(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", className);
            }
        }
        return null;
    }

    public boolean isSystemConnectionAction(@Nonnull Class<?> clazz) {
        this.prepareActionsHashMap();
        if (this.actions.containsKey(clazz)) {
            return this.actions.get(clazz).isSystemConnectionAction;
        }
        return false;
    }

    public boolean isSystemConnectionAction(@Nonnull String className) {
        if (!className.isEmpty()) {
            try {
                return this.isSystemConnectionAction(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", className);
            }
        }
        return false;
    }

    @CheckForNull
    public String getClassName(@CheckForNull String name) {
        if (name != null && !name.isEmpty()) {
            this.prepareActionsHashMap();
            for (Entry<Class<?>, ActionAttributes> entry : this.actions.entrySet()) {
                if (entry.getValue().name.equals(name)) {
                    return entry.getKey().getName();
                }
            }
        }
        return null;
    }

    @CheckForNull
    public String[] getNames() {
        this.prepareActionsHashMap();
        if (this.actionNames == null) {
            this.actionNames = new ArrayList<>();
            this.actions.values().stream().forEach((attrs) -> {
                this.actionNames.add(attrs.name);
            });
            this.actionNames.sort(null);
        }
        return this.actionNames.toArray(new String[this.actionNames.size()]);
    }

    @Nonnull
    public Class<?>[] getClasses() {
        this.prepareActionsHashMap();
        return actions.keySet().toArray(new Class<?>[actions.size()]);
    }

    private void prepareActionsHashMap() {
        if (this.actions == null) {
            this.actions = new HashMap<>();
            this.overrides = new HashMap<>();

            ServiceLoader<StartupActionFactory> jusLoader = ServiceLoader.load(StartupActionFactory.class);
            this.serviceClasses = new HashSet<>();
            jusLoader.forEach(factory -> {
                addActions(factory);
                serviceClasses.addAll(Arrays.asList(factory.getActionClasses()));
            });
            jusLoader.reload(); // allow factories to be garbage collected

            InstanceManager.getList(SystemConnectionMemo.class).forEach(memo -> addActions(memo.getActionFactory()));
            InstanceManager.getList(SystemConnectionMemo.class).forEach(memo -> listenTo(memo));

            firePropertyChange("length", 0, actions.size());
        }
    }

    // add the listener once only, even if both prepareActionsHashMap() and
    // memoChanged() see the same new memo
    private void listenTo(SystemConnectionMemo memo) {
        memo.removePropertyChangeListener("actionFactory", actionFactoryListener);
        memo.addPropertyChangeListener("actionFactory", actionFactoryListener);
    }

    private void addActions(StartupActionFactory factory) {
        Arrays.stream(factory.getActionClasses()).forEach(clazz -> {
            actions.put(clazz, new ActionAttributes(factory.getTitle(clazz), clazz));
            Arrays.stream(factory.getOverriddenClasses(clazz))
                    .forEach(overridden -> overrides.put(overridden, clazz));
        });
    }

    /**
     * Remove the actions of a factory that is going away.
     * <p>
     * Several connections of the same type (for example two CBUS connections)
     * offer the same action classes. A class is only removed if no remaining
     * connection, and no general factory, still offers it. Otherwise removing
     * one connection would make the startup actions of the others unknown.
     *
     * @param factory the factory being removed
     * @param leaving the connection the factory belongs to, which is not
     *                counted as still offering the actions; may be null
     */
    private void removeActions(StartupActionFactory factory, @CheckForNull SystemConnectionMemo leaving) {
        Arrays.stream(factory.getActionClasses())
                .filter(clazz -> !isOfferedElsewhere(clazz, leaving))
                .forEach(actions::remove);
    }

    private boolean isOfferedElsewhere(Class<?> clazz, @CheckForNull SystemConnectionMemo leaving) {
        if (serviceClasses != null && serviceClasses.contains(clazz)) {
            return true;
        }
        for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo != leaving && Arrays.asList(memo.getActionFactory().getActionClasses()).contains(clazz)) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    public String getOverride(@CheckForNull String name) {
        this.prepareActionsHashMap();
        if (name != null && this.overrides.containsKey(name)) {
            return this.overrides.get(name).getName();
        }
        return null;
    }

    @Override
    public void dispose() {
        InstanceManager.removePropertyChangeListener(InstanceManager.getListPropertyName(SystemConnectionMemo.class), memosListener);
    }

    private void memoChanged(PropertyChangeEvent evt) {
        prepareActionsHashMap();
        actionNames = null;
        int size = actions.size();
        Object src = evt.getNewValue();
        if (src instanceof SystemConnectionMemo) {
            SystemConnectionMemo memo = (SystemConnectionMemo) src;
            addActions(memo.getActionFactory());
            listenTo(memo);
        } else {
            src = evt.getOldValue();
            if (src instanceof SystemConnectionMemo) {
                SystemConnectionMemo memo = (SystemConnectionMemo) src;
                removeActions(memo.getActionFactory(), memo);
                memo.removePropertyChangeListener("actionFactory", actionFactoryListener);
            }
        }
        firePropertyChange("length", size, actions.size());
    }

    private void actionFactoryChanged(PropertyChangeEvent evt) {
        prepareActionsHashMap();
        actionNames = null;
        int size = actions.size();
        Object value = evt.getOldValue();
        if (value instanceof StartupActionFactory) {
            Object source = evt.getSource();
            removeActions((StartupActionFactory) value,
                    source instanceof SystemConnectionMemo ? (SystemConnectionMemo) source : null);
        }
        value = evt.getNewValue();
        if (value instanceof StartupActionFactory) {
            addActions((StartupActionFactory) value);
        }
        firePropertyChange("length", size, actions.size());
    }

    private static class ActionAttributes {

        final String name;
        final boolean isSystemConnectionAction;

        ActionAttributes(String name, Class<?> clazz) {
            this.name = name;
            this.isSystemConnectionAction = SystemConnectionAction.class.isAssignableFrom(clazz);
        }
    }
}

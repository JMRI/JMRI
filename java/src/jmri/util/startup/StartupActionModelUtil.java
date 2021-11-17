package jmri.util.startup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private ArrayList<String> actionNames = null; // built on demand, invalidated in changes to actions
    private final PropertyChangeListener memosListener = this::memoChanged;
    private final PropertyChangeListener actionFactoryListener = this::actionFactoryChanged;
    private final static Logger log = LoggerFactory.getLogger(StartupActionModelUtil.class);

    /**
     * Get the default StartupActionModelUtil instance, creating it if
     * necessary.
     *
     * @return the default instance
     */
    @Nonnull
    static public StartupActionModelUtil getDefault() {
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

    /**
     * Add an action from the list of actions.
     *
     * @param strClass the action class
     * @param name the localized action name
     * @throws ClassNotFoundException if the action class cannot be found
     * @deprecated since 4.19.7 without direct replacement
     */
    @Deprecated
    public void addAction(@Nonnull String strClass, @Nonnull String name) throws ClassNotFoundException {
        this.prepareActionsHashMap();
        this.actionNames = null;
        Class<?> clazz;
        try {
            clazz = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", strClass);
            throw ex;
        }
        ActionAttributes attrs = new ActionAttributes(name, clazz);
        actions.put(clazz, attrs);
        this.firePropertyChange("length", null, null);
    }

    /**
     * Remove an action from the list of actions.
     *
     * @param strClass the action class
     * @throws ClassNotFoundException if the action class cannot be found
     * @deprecated since 4.19.7 without direct replacement
     */
    @Deprecated
    public void removeAction(@Nonnull String strClass) throws ClassNotFoundException {
        this.prepareActionsHashMap();
        this.actionNames = null;
        Class<?> clazz;
        try {
            clazz = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", strClass);
            throw ex;
        }
        actions.remove(clazz);
        this.firePropertyChange("length", null, null);
    }

    @SuppressWarnings("deprecation") // StartupActionFactory in apps.startup has been deprecated
    private void prepareActionsHashMap() {
        if (this.actions == null) {
            this.actions = new HashMap<>();
            this.overrides = new HashMap<>();

            ServiceLoader<StartupActionFactory> jusLoader = ServiceLoader.load(StartupActionFactory.class);
            jusLoader.forEach(factory -> addActions(factory));
            jusLoader.reload(); // allow factories to be garbage collected

            InstanceManager.getList(SystemConnectionMemo.class).forEach(memo -> addActions(memo.getActionFactory()));
            InstanceManager.getList(SystemConnectionMemo.class).forEach(memo -> memo.addPropertyChangeListener("actionFactory", actionFactoryListener));

            firePropertyChange("length", 0, actions.size());
        }
    }

    private void addActions(StartupActionFactory factory) {
        Arrays.stream(factory.getActionClasses()).forEach(clazz -> {
            actions.put(clazz, new ActionAttributes(factory.getTitle(clazz), clazz));
            Arrays.stream(factory.getOverriddenClasses(clazz))
                    .forEach(overridden -> overrides.put(overridden, clazz));
        });
    }

    private void removeActions(StartupActionFactory factory) {
        Arrays.stream(factory.getActionClasses()).forEach(actions::remove);
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
            memo.addPropertyChangeListener("actionFactory", actionFactoryListener);
        } else {
            src = evt.getOldValue();
            if (src instanceof SystemConnectionMemo) {
                SystemConnectionMemo memo = (SystemConnectionMemo) src;
                removeActions(memo.getActionFactory());
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
            removeActions((StartupActionFactory) value);
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

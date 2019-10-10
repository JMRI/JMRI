package apps.startup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.beans.Bean;
import jmri.jmrix.swing.SystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain a list of actions that can be used by
 * {@link apps.startup.AbstractActionModel} and its descendants. This list is
 * populated by {@link apps.startup.StartupActionFactory} instances registered
 * with a {@link java.util.ServiceLoader}.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupActionModelUtil extends Bean {

    private HashMap<Class<?>, ActionAttributes> actions = null;
    private HashMap<String, Class<?>> overrides = null;
    private ArrayList<String> actionNames = null; // built on demand, invalidated in changes to actions
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

    private void prepareActionsHashMap() {
        if (this.actions == null) {
            this.actions = new HashMap<>();
            this.overrides = new HashMap<>();
            ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
            rb.keySet().stream().filter((key) -> (!key.isEmpty())).forEach((key) -> {
                try {
                    Class<?> clazz = Class.forName(key);
                    ActionAttributes attrs = new ActionAttributes(rb.getString(key), clazz);
                    this.actions.put(clazz, attrs);
                } catch (ClassNotFoundException ex) {
                    log.error("Did not find class \"{}\"", key);
                }
            });
            ServiceLoader<StartupActionFactory> loader = ServiceLoader.load(StartupActionFactory.class);
            loader.forEach(factory -> {
                for (Class<?> clazz : factory.getActionClasses()) {
                    ActionAttributes attrs = new ActionAttributes(factory.getTitle(clazz), clazz);
                    this.actions.put(clazz, attrs);
                    for (String overridden : factory.getOverriddenClasses(clazz)) {
                        this.overrides.put(overridden, clazz);
                    }
                }
            });
            loader.reload(); // allow factories to be garbage collected
        }
    }

    @CheckForNull
    public String getOverride(@CheckForNull String name) {
        this.prepareActionsHashMap();
        if (name != null && this.overrides.containsKey(name)) {
            return this.overrides.get(name).getName();
        }
        return null;
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

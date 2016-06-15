package apps.startup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.beans.Bean;
import jmri.jmrix.swing.SystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain a list of actions that can be used by
 * {@link apps.startup.AbstractActionModel} and it's descendants.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupActionModelUtil extends Bean {

    private HashMap<Class<?>, ActionAttributes> actions = null;
    private ArrayList<String> actionNames = null; // built on demand, invalidated in changes to actions
    private final static Logger log = LoggerFactory.getLogger(StartupActionModelUtil.class);

    @CheckForNull
    public String getActionName(@Nonnull Class<?> clazz) {
        this.prepareActionsHashMap();
        return this.actions.get(clazz).name;
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
    public String getClassName(@Nonnull String name) {
        this.prepareActionsHashMap();
        if (!name.isEmpty()) {
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
        ActionAttributes attrs = new ActionAttributes(strClass, SystemConnectionAction.class.isAssignableFrom(clazz));
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
            ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
            rb.keySet().stream().filter((key) -> (!key.isEmpty())).forEach((key) -> {
                try {
                    Class<?> clazz = Class.forName(key);
                    ActionAttributes attrs = new ActionAttributes(rb.getString(key), SystemConnectionAction.class.isAssignableFrom(clazz));
                    this.actions.put(clazz, attrs);
                } catch (ClassNotFoundException ex) {
                    log.error("Did not find class \"{}\"", key);
                }
            });
        }
    }

    private static class ActionAttributes {

        final String name;
        final boolean isSystemConnectionAction;

        ActionAttributes(String name, boolean isSystemConnectionAction) {
            this.name = name;
            this.isSystemConnectionAction = isSystemConnectionAction;
        }
    }
}

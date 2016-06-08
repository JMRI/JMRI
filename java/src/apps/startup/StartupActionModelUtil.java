package apps.startup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain a list of actions that can be used by
 * {@link apps.AbstractActionModel} and it's descendants.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupActionModelUtil extends Bean {

    private HashMap<Class<?>, String> actions = null;
    private final static Logger log = LoggerFactory.getLogger(StartupActionModelUtil.class);

    @Nullable
    public String getActionName(@Nonnull Class<?> clazz) {
        this.prepareActionsHashMap();
        return this.actions.get(clazz);
    }

    @Nullable
    public String getActionName(@Nonnull String className) {
        this.prepareActionsHashMap();
        if (!className.isEmpty()) {
            try {
                return this.getActionName(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", className);
            }
        }
        return null;
    }

    @Nullable
    public String getClassName(@Nonnull String name) {
        this.prepareActionsHashMap();
        if (!name.isEmpty()) {
            for (Entry<Class<?>, String> entry : this.actions.entrySet()) {
                if (entry.getValue().equals(name)) {
                    return entry.getKey().getName();
                }
            }
        }
        return null;
    }

    @Nonnull
    public String[] getNames() {
        this.prepareActionsHashMap();
        String[] names = actions.values().toArray(new String[actions.size()]);
        Arrays.sort(names);
        return names;
    }

    @Nonnull
    public Class<?>[] getClasses() {
        this.prepareActionsHashMap();
        return actions.keySet().toArray(new Class<?>[actions.size()]);
    }

    public void addAction(@Nonnull String strClass, @Nonnull String name) throws ClassNotFoundException {
        this.prepareActionsHashMap();
        Class<?> clazz;
        try {
            clazz = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", strClass);
            throw ex;
        }
        actions.put(clazz, name);
        this.firePropertyChange("length", null, null);
    }

    public void removeAction(@Nonnull String strClass) throws ClassNotFoundException {
        this.prepareActionsHashMap();
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
                    this.actions.put(clazz, rb.getString(key));
                } catch (ClassNotFoundException ex) {
                    log.error("Did not find class \"{}\"", key);
                }
            });
        }
    }
}

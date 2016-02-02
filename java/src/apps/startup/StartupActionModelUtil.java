package apps.startup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
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

    private final HashMap<Class<?>, String> actions = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(StartupActionModelUtil.class);

    public StartupActionModelUtil() {
        ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
        rb.keySet().stream().filter((key) -> (!key.isEmpty())).forEach((key) -> {
            try {
                Class<?> clazz = Class.forName(key);
                actions.put(clazz, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        });
    }

    public String getActionName(Class<?> clazz) {
        return this.actions.get(clazz);
    }

    public String getActionName(String className) {
        try {
            return this.getActionName(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", className);
        }
        return null;
    }

    public String getClassName(String name) {
        for (Entry<Class<?>, String> entry : this.actions.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey().getName();
            }
        }
        return null;
    }

    public String[] getNames() {
        String[] names = actions.values().toArray(new String[actions.size()]);
        Arrays.sort(names);
        return names;
    }

    public Class<?>[] getClasses() {
        return actions.keySet().toArray(new Class<?>[actions.size()]);
    }

    public void addAction(String strClass, String name) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", strClass);
            throw ex;
        }
        actions.put(clazz, name);
        this.propertyChangeSupport.firePropertyChange("length", null, null);
    }

    public void removeAction(String strClass) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class \"{}\"", strClass);
            throw ex;
        }
        actions.remove(clazz);
        this.propertyChangeSupport.firePropertyChange("length", null, null);
    }

}

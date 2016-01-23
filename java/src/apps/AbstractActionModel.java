// AbstractActionModel.java
package apps;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide services for invoking actions during configuration and startup.
 * <P>
 * The action classes and corresponding human-readable names are kept in the
 * apps.ActionListBundle properties file (which can be translated). They are
 * displayed in lexical order by human-readable name.
 * <P>
 * @author	Bob Jacobsen Copyright 2003, 2007, 2014
 * @version $Revision$
 * @see apps.startup.AbstractActionModelFactory
 */
public abstract class AbstractActionModel implements StartupModel {

    public AbstractActionModel() {
        className = "";
    }
    //TODO At some point this class might need to consider which system connection memo is being against certain system specific items
    String className;

    public String getClassName() {
        return className;
    }

    public String getName() {
        Iterator<Class<?>> iterator = classList.keySet().iterator();
        while (iterator.hasNext()) {
            Class<?> key = iterator.next();
            if (key.getName().equals(className)) {
                return classList.get(key);
            }
        }
        return null;
    }

    public void setName(String n) {
        Iterator<Class<?>> iterator = classList.keySet().iterator();
        while (iterator.hasNext()) {
            Class<?> key = iterator.next();
            if (classList.get(key).equals(n)) {
                className = key.getName();
                return;
            }
        }
    }

    public void setClassName(String n) {
        className = n;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public String[] nameList() {
        if (classList == null) {
            loadArrays();
        }
        String[] names = classList.values().toArray(new String[classList.size()]);
        jmri.util.StringUtil.sort(names);
        return names;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public Class<?>[] classList() {
        if (classList == null) {
            loadArrays();
        }
        return classList.keySet().toArray(new Class<?>[classList.size()]);
    }

    static private void loadArrays() {
        ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
        // count entries (not entirely efficiently!)
        classList = new HashMap<Class<?>, String>();
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            Class<?> classes;
            String key = e.nextElement();
            if (!key.equals("")) { // ignoring empty lines in file
                try {
                    classes = Class.forName(key);
                    classList.put(classes, rb.getString(key));
                } catch (ClassNotFoundException ex) {
                    log.error("Did not find class [" + key + "]");
                }
            }
        }
    }

    public void addAction(String strClass, String name) throws ClassNotFoundException {
        if (classList == null) {
            loadArrays();
        }
        Class<?> classes;
        try {
            classes = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class " + strClass);
            throw ex;
        }
        classList.put(classes, name);
        firePropertyChange("length", null, null);
    }

    public void removeAction(String strClass) throws ClassNotFoundException {
        if (classList == null) {
            return;
        }
        Class<?> classes;
        try {
            classes = Class.forName(strClass);
        } catch (ClassNotFoundException ex) {
            log.error("Did not find class " + strClass);
            throw ex;
        }
        classList.remove(classes);
        firePropertyChange("length", null, null);
    }

    static private HashMap<Class<?>, String> classList = null;

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(AbstractActionModel.class.getName());

}

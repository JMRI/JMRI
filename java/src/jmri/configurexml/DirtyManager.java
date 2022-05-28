package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.configurexml.swing.DirtyManagerDialog;
import jmri.jmrit.display.*;
import jmri.jmrit.simpleclock.SimpleTimebase;

/**
 * The Dirty Manager is notified whenever potential PanelPro data content is added, deleted or modified.
 * During shutdown, a dialog will be displayed if any of the potential data is 'dirty'.
 * The user will have an opportunity to store the data before shutdown proceeds.
 *
 * The basic process is to listen for propertyChange events.  When a relevant event occurs, the global
 * dirty flag is set.  When the store process has completed, the dirty flag is cleared.  In addition
 * to existing property changes, the SetConfigDirty event can be used for changes that don't have
 * an existing event.
 *
 * During PanelPro data file loading, the propertyChange events are ignored.
 *
 * The propertyChange events can be triggered by managers, individual beans, panels and other special
 * objects such as SSL (blockboss).
 *
 * For classes that don't support propertyChangeEvents, a direct call can used:
 * jmri.InstanceManager.getDefault(jmri.configurexml.DirtyManager.class).setDirty(true).
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class DirtyManager {

    boolean _dirty = false;
    boolean _loading = true;

    final private PropertyChangeListener _mgrListen = new ManagerListener();
    final private PropertyChangeListener _beanListen = new BeanListener();

    public DirtyManager() {
    }

    /**
     * When a manager notifies ConfigXmlManager that it is responsible for creating a portion of
     * the PanelPro data file, the manager is forwarded here so that its changes can be captured.
     * @param o The manager object.
     */
    protected void addManager(Object o) {
        log.info("++ 1 dm add manager :: {}", o.getClass().getName());
        var cname = o.getClass().getName();
        if (cname.contains("DefaultConditionalManager")) return;  // Special handling required.
        if (o instanceof jmri.managers.AbstractManager) {
            Manager<?> m = (Manager<?>) o;
            m.addPropertyChangeListener(_mgrListen);
            loadBeans(o);
        } else if (o instanceof SimpleTimebase) {       // SIMPLECLOCK
            var sb = (SimpleTimebase) o;
            sb.removePropertyChangeListener(_beanListen);
            sb.addPropertyChangeListener(_beanListen);
        } else if (o instanceof Editor) {
            var em = InstanceManager.getDefault(EditorManager.class);
            em.removePropertyChangeListener("SetConfigDirty", _beanListen);
            em.addPropertyChangeListener("SetConfigDirty", _beanListen);
        } else {
            log.info("!! Unable to identify class:  {}", o);
        }
    }

    /**
     * Attach a listener to all of the children of a manager to listen for bean changes.
     * @param o The bean manager object.
     */
    private void loadBeans(Object o) {
        Manager<?> m = (Manager<?>) o;
        m.getNamedBeanSet().forEach((bean) -> {
            bean.removePropertyChangeListener(_beanListen);
            bean.addPropertyChangeListener(_beanListen);
        });
    }

    /**
     * Set the dirty flag true or false.
     * True occurs when changes are reported by listener events and other special cases.
     * After a Store, the flag is set to false.
     * @param dirty Either true or false.
     */
    public void setDirty(boolean dirty) {
        log.info("## dirty flag = {}, loading = {}", dirty, _loading);
        if (!_loading) {
            _dirty = dirty;
        }
    }

    /**
     * This is used by storeIfNeeded which is invoked by the shutdown manager.  If true, provide an
     * option to do the Store process before the shutdown occurs.
     * @return the dirty state.
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * This is used to ignore the changes that naturally occur data loading.
     * Set by the PanelPro data load processes to before loading and false when done.
     * @param loading Either true or false.
     */
    public void setLoading(boolean loading) {
        log.info("@@ loading flag = {}", loading);
        _loading = loading;
    }

    /**
     * This in invoked by the various dirty processes to see if a load is in process.
     * @return the loading state.
     */
    public boolean isLoading() {
        return _loading;
    }

    /**
     * If the dirty state is true, provide the user with the opporutnity to do a Store before
     * the shutdown completes.  This is invoked by the shutdown manager.
     */
    public void storeIfNeeded() {
        if (isDirty() && !GraphicsEnvironment.isHeadless()) {
            DirtyManagerDialog.showDialog();
        }
    }

    /**
     * Listen for changes from the managers.  The primary event is the <b>length</b> which indicates
     * a bean has been added to deleted.  When this occurs, the bean listeners are updated.  The
     * other events are default values that some managers have, such as sensor debounce values.
     */
    class ManagerListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            log.info("== mgr evt {} :: {}", evt.getPropertyName(), evt.getSource());
            if (evt.getPropertyName().equals("length")) {
                log.info("== 0 mgr evt {}", evt.getSource());
                setDirty(true);
                loadBeans(evt.getSource());
                return;
            }
            setDirty(true);
        }
    }

    /**
     * Listen for changes for individual beans.
     */
    class BeanListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
//             log.info("-- bean evt prop = {} :: src = {} :: evt = {}", evt.getPropertyName(), evt.getSource().getClass().getName(), evt);
//             log.info("    evt = {}", evt.toString());
            log.info("    evt = {} :: {}", evt.getPropertyName(), evt.getSource());
            if (evt.getPropertyName().equals("KnownState")) return;
            if (evt.getPropertyName().equals("value")) return;
            if (evt.getPropertyName().equals("time")) return;
            if (evt.getPropertyName().equals("minutes")) return;
            setDirty(true);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirtyManager.class);

}

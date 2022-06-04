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
    boolean _enabled = false;

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
//         log.debug("++ 1 dm add manager :: {}", o.getClass().getName());
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
            em.removePropertyChangeListener(_beanListen);
            em.addPropertyChangeListener(_beanListen);
        } else {
            log.debug("!! Unable to identify class:  {}", o);
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
    public void setDirty(boolean dirty, PropertyChangeEvent e) {
        if (_enabled) {
            log.debug("## dirty = {} :: {}", dirty, getEventString(e));
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
     * Set by the PanelPro data load processes to before loading and true when done.
     * @param enabled Either true or false.
     */
    public void setEnabled(boolean enabled) {
        log.debug("@@ enabled flag = {}", enabled);
        _enabled = enabled;
    }

    /**
     * This in invoked by the various dirty processes to see if a load is in process.
     * @return the loading state.
     */
    public boolean isEnabled() {
        return _enabled;
    }

    private String getEventString(PropertyChangeEvent event) {
        var property = event.getPropertyName();
        var vOld = event.getOldValue();
        var vNew = event.getNewValue();
        var obj = event.getSource();
        var format = "%s~%s~%s~%s";
        if (vOld instanceof Integer) format = "%s~%d~%d~%s";
        return String.format(format, property, vOld, vNew, obj);
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
            if (evt.getPropertyName().equals("length")) {
//                 log.debug("== 0 mgr evt {}", evt.getSource());
                if (isDefaultAudio(evt)) return;
                setDirty(true, evt);
                loadBeans(evt.getSource());
                return;
            }
            if (isDefaultAudio(evt)) return;
//             log.debug("== mgr evt {} :: {}", evt.getPropertyName(), evt.getSource());
            setDirty(true, evt);
        }
    }

    /**
     * Listen for changes for individual beans.
     */
    class BeanListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
//             log.debug("-- bean evt prop = {} :: src = {} :: evt = {}", evt.getPropertyName(), evt.getSource().getClass().getName(), evt);
//             log.debug("    evt = {}", evt.toString());
            if (evt.getPropertyName().equals("CommandedState")) return;
            if (evt.getPropertyName().equals("KnownState")) return;
            if (evt.getPropertyName().equals("value")) return;
            if (evt.getPropertyName().equals("time")) return;
            if (evt.getPropertyName().equals("minutes")) return;
            if (evt.getPropertyName().equals("Comment") && evt.getOldValue() == null && ((String) evt.getNewValue()).isEmpty()) return;
//             log.debug("    evt = {} :: {}", evt.getPropertyName(), evt.getSource());
            setDirty(true, evt);
        }
    }

    private boolean isDefaultAudio(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "length":
                if (e.getSource() instanceof jmri.AudioManager) {
                    if ((Integer) e.getNewValue() == 1) return true;
                }
                break;

            case "beans":
                if (e.getSource() instanceof jmri.AudioManager) {
                    var audio = (jmri.Audio) e.getNewValue();
                    if (audio.getSystemName().equals("IAL$")) return true;
                }
                break;
        }
        return false;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirtyManager.class);

}

package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.Namespace;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.HashMap;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.beans.Beans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A namespace for virtual names.
 */
public class VirtualNamespace implements Namespace, PropertyChangeListener, VetoableChangeListener {

    Map<String, String> namespaceMap = new HashMap<>();
    
    @Override
    public <M extends Manager> void registerName(Namespace namespace, Class<M> type, String name) throws UnsupportedOperationException {
        namespaceMap.put(name, null);
//        jmri.NamedBeanHandle a;
//        jmri.NamedBeanHandleManager b;
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Manager, N extends NamedBean> N get(Namespace namespace, Class<M> type, Class<N> clazz, String name) {
        return (N) InstanceManager.getDefault(type).getNamedBean(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Manager, N extends NamedBean> N provide(Namespace namespace, Class<M> type, Class<N> clazz, String name) {
        
        if (type.isInstance(ProvidingManager.class)) {
            return ((ProvidingManager<N>)InstanceManager.getDefault(type))
                    .provide(name);
        } else {
            return null;
        }
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //  public void firePropertyChange(String propertyName,
    //             Object oldValue,
    //      Object newValue)
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected final HashMap<PropertyChangeListener, String> register = new HashMap<>();
    protected final HashMap<PropertyChangeListener, String> listenerRefs = new HashMap<>();

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(propertyName, l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
        if (listener != null && !Beans.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
        if (listener != null && !Beans.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /**
     * The PropertyChangeListener interface in this class is intended to keep
     * track of user name changes to individual NamedBeans. It is not completely
     * implemented yet. In particular, listeners are not added to newly
     * registered objects.
     *
     * @param e the event
     */
    @Override
    @SuppressWarnings("unchecked") // The cast of getSource() to E can't be checked due to type erasure, but we catch errors
    @OverridingMethodsMustInvokeSuper
    public void propertyChange(PropertyChangeEvent e) {
        throw new UnsupportedOperationException("Not inplemented yet.");
/*
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();  // previous user name
            String now = (String) e.getNewValue();  // current user name
            try { // really should always succeed
                E t = (E) e.getSource();
                if (old != null) {
                    _tuser.remove(old); // remove old name for this bean
                }
                if (now != null) {
                    // was there previously a bean with the new name?
                    if (_tuser.get(now) != null && _tuser.get(now) != t) {
                        // If so, clear. Note that this is not a "move" operation
                        _tuser.get(now).setUserName(null);
                    }

                    _tuser.put(now, t); // put new name for this bean
                }
            } catch (ClassCastException ex) {
                log.error("Received event of wrong type {}", e.getSource().getClass().getName(), ex);
            }

            // called DisplayListName, as DisplayName might get used at some point by a NamedBean
            firePropertyChange("DisplayListName", old, now); //IN18N
        }
*/
    }

    VetoableChangeSupport vcs = new VetoableChangeSupport(this);

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vcs.getVetoableChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return vcs.getVetoableChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vcs.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * Method to inform all registered listeners of a vetoable change. If the
     * propertyName is "CanDelete" ALL listeners with an interest in the bean
     * will throw an exception, which is recorded returned back to the invoking
     * method, so that it can be presented back to the user. However if a
     * listener decides that the bean can not be deleted then it should throw an
     * exception with a property name of "DoNotDelete", this is thrown back up
     * to the user and the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will enquire with all listerners if the item can
     *            be deleted. "DoDelete" tells the listerner to delete the item.
     * @param old The old value of the property.
     * @param n   The new value of the property.
     * @throws PropertyVetoException - if the recipients wishes the delete to be
     *                               aborted.
     */
    @OverridingMethodsMustInvokeSuper
    protected void fireVetoableChange(String p, Object old, Object n) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, n);
        if (p.equals("CanDelete")) { //IN18N
            StringBuilder message = new StringBuilder();
            for (VetoableChangeListener vc : vcs.getVetoableChangeListeners()) {
                try {
                    vc.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        log.info(e.getMessage());
                        throw e;
                    }
                    message.append(e.getMessage());
                    message.append("<hr>"); //IN18N
                }
            }
            throw new PropertyVetoException(message.toString(), evt);
        } else {
            try {
                vcs.fireVetoableChange(evt);
            } catch (PropertyVetoException e) {
                log.error("Change vetoed.", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

        throw new UnsupportedOperationException("Not inplemented yet.");
/*        
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            message.append(jmri.managers.Bundle.getMessage("VetoFoundIn", getBeanTypeHandled()))
                    .append("<ul>");
            boolean found = false;
            for (NamedBean nb : _beans) {
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        throw e;
                    }
                    found = true;
                    message.append("<li>")
                            .append(e.getMessage())
                            .append("</li>");
                }
            }
            message.append("</ul>")
                    .append(jmri.managers.Bundle.getMessage("VetoWillBeRemovedFrom", getBeanTypeHandled()));
            if (found) {
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else {
            for (NamedBean nb : _beans) {
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    throw e;
                }
            }
        }
*/
    }


    private final static Logger log = LoggerFactory.getLogger(VirtualNamespace.class);

}

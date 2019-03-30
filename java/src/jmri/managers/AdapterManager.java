package jmri.managers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.List;
import java.util.SortedSet;
import jmri.Manager;
import jmri.NamedBean;

/**
 * An adaptor for managers.
 * 
 * @author Daniel Bergqvist 2019
 */
public class AdapterManager<A extends NamedBean, B extends A, C extends Manager<B>> implements Manager<A> {

    private final C _manager;
    
    public AdapterManager(C manager) {
        this._manager = manager;
    }
    
    @Override
    public String getSystemPrefix() {
        return _manager.getSystemPrefix();
    }

    @Override
    public char typeLetter() {
        return _manager.typeLetter();
    }

    @Override
    public String makeSystemName(String s) {
        return _manager.makeSystemName(s);
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return _manager.validSystemNameFormat(systemName);
    }

    @Override
    public void dispose() {
        _manager.dispose();
    }

    @Override
    public int getObjectCount() {
        return _manager.getObjectCount();
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public String[] getSystemNameArray() {
        return _manager.getSystemNameArray();
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<String> getSystemNameList() {
        return _manager.getSystemNameList();
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<A> getNamedBeanList() {
        throw new RuntimeException("error");
//        return _manager.getNamedBeanList();
    }

    @Override
    public SortedSet<A> getNamedBeanSet() {
        throw new RuntimeException("error");
//        return _manager.getNamedBeanSet();
    }

    @Override
    public A getBeanBySystemName(String systemName) {
        return _manager.getBeanBySystemName(systemName);
    }

    @Override
    public A getBeanByUserName(String userName) {
        return _manager.getBeanByUserName(userName);
    }

    @Override
    public A getNamedBean(String name) {
        return _manager.getNamedBean(name);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        _manager.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        _manager.removePropertyChangeListener(l);
    }

    @Override
    public void addVetoableChangeListener(VetoableChangeListener l) {
        _manager.addVetoableChangeListener(l);
    }

    @Override
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        _manager.removeVetoableChangeListener(l);
    }

    @Override
    public void deleteBean(A n, String property) throws PropertyVetoException {
        throw new UnsupportedOperationException("operation not supported");
    }

    @Override
    public void register(A n) {
        throw new UnsupportedOperationException("operation not supported");
    }

    @Override
    public void deregister(A n) {
        throw new UnsupportedOperationException("operation not supported");
    }

    @Override
    public int getXMLOrder() {
        return _manager.getXMLOrder();
    }

    @Override
    public String getBeanTypeHandled() {
        return _manager.getBeanTypeHandled();
    }

    @Override
    public String normalizeSystemName(String inputName) throws NamedBean.BadSystemNameException {
        return _manager.normalizeSystemName(inputName);
    }

    @Override
    public void addDataListener(ManagerDataListener<A> e) {
        _manager.addDataListener(new ManagerDataListenerAdapter<>(e));
    }

    @Override
    public void removeDataListener(ManagerDataListener<A> e) {
        _manager.removeDataListener(new ManagerDataListenerAdapter<>(e));
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _manager.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _manager.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _manager.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _manager.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        _manager.addVetoableChangeListener(propertyName, listener);
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return _manager.getVetoableChangeListeners();
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return _manager.getVetoableChangeListeners(propertyName);
    }

    @Override
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        _manager.removeVetoableChangeListener(propertyName, listener);
    }
    
    
    private static class ManagerDataListenerAdapter<A extends NamedBean, B extends A> implements ManagerDataListener<B> {

        private final ManagerDataListener<A> _listener;

        private ManagerDataListenerAdapter(ManagerDataListener<A> listener) {
            this._listener = listener;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void contentsChanged(ManagerDataEvent<B> e) {
            _listener.contentsChanged((ManagerDataEvent<A>)e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void intervalAdded(ManagerDataEvent<B> e) {
            _listener.intervalAdded((ManagerDataEvent<A>)e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void intervalRemoved(ManagerDataEvent<B> e) {
            _listener.intervalRemoved((ManagerDataEvent<A>)e);
        }
        
    }
    
}

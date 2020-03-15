package jmri.jmrit.operations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.annotation.Nonnull;

import jmri.beans.PropertyChangeProvider;

/**
 * An operations object that has an identity and provides support for listening to property changes.Ï
 * 
 * @author Randall Wood Copyright 2020
 */
public abstract class Identified implements PropertyChangeProvider {

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public abstract String getId();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}

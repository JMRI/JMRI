package jmri.jmrit.audio;

import java.beans.*;
import java.util.SortedSet;

import jmri.*;

/**
 * The default AudioSourceManager.
 * @author Daniel Bergqvist (C) 2023
 */
public class DefaultAudioSourceManager implements AudioSourceManager {

    private final AudioManager manager =
            InstanceManager.getDefault(AudioManager.class);


    /** {@inheritDoc} */
    @Override
    public Audio provideAudio(String name) throws AudioException {
        return manager.provideAudio(name);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getAudio(String name) {
        return manager.getAudio(name);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getBySystemName(String systemName) {
        return manager.getBySystemName(systemName);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getByUserName(String userName) {
        return manager.getByUserName(userName);
    }

    /** {@inheritDoc} */
    @Override
    public Audio newAudio(String systemName, String userName) throws AudioException {
        return manager.newAudio(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    public AudioFactory getActiveAudioFactory() {
        return manager.getActiveAudioFactory();
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<Audio> getNamedBeanSet(char subType) {
        return manager.getNamedBeanSet(subType);
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        manager.init();
    }

    /** {@inheritDoc} */
    @Override
    public void cleanup() {
        manager.cleanup();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInitialised() {
        return manager.isInitialised();
    }

    /** {@inheritDoc} */
    @Override
    public SystemConnectionMemo getMemo() {
        return manager.getMemo();
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemPrefix() {
        return manager.getSystemPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return manager.typeLetter();
    }

    /** {@inheritDoc} */
    @Override
    public Class<Audio> getNamedBeanClass() {
        return manager.getNamedBeanClass();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        manager.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public int getObjectCount() {
        return manager.getNamedBeanSet(Audio.SOURCE).size();
//        return manager.getObjectCount();
    }

    /**
     * {@inheritDoc}
     * <P>
     * Note that this method only returns audio sources.
     */
    @Override
    public SortedSet<Audio> getNamedBeanSet() {
        return manager.getNamedBeanSet(Audio.SOURCE);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getNamedBean(String name) {
        return manager.getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteBean(Audio n, String property) throws PropertyVetoException {
        manager.deleteBean(n, property);
    }

    /** {@inheritDoc} */
    @Override
    public void register(Audio n) {
        manager.register(n);
    }

    /** {@inheritDoc} */
    @Override
    public void deregister(Audio n) {
        manager.deregister(n);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return manager.getXMLOrder();
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return manager.getBeanTypeHandled(plural);
    }

    /** {@inheritDoc} */
    @Override
    public void addDataListener(ManagerDataListener<Audio> e) {
        manager.addDataListener(e);
    }

    /** {@inheritDoc} */
    @Override
    public void removeDataListener(ManagerDataListener<Audio> e) {
        manager.removeDataListener(e);
    }

    /** {@inheritDoc} */
    @Override
    public void setPropertyChangesSilenced(String propertyName, boolean silenced) {
        manager.setPropertyChangesSilenced(propertyName, silenced);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        manager.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        manager.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return manager.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return manager.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        manager.removePropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        manager.removePropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        manager.addVetoableChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        manager.addVetoableChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return manager.getVetoableChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return manager.getVetoableChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        manager.removeVetoableChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        manager.removeVetoableChangeListener(propertyName, listener);
    }

}

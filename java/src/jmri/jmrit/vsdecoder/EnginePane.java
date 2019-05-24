package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/**
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <p>
 *
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018
 */
public class EnginePane extends JPanel {
    // Superclass for Diesel, Steam, Electric panes.
    // Doesn't really do anything.

    String name;

    EngineSoundEvent engine;

    public EnginePane(String n, EngineSoundEvent e) {
        super();
        name = n;
        engine = e;
        listenerList = new javax.swing.event.EventListenerList();
    }

    public EnginePane(String n) {
        this(n, null);
    }

    public EnginePane() {
        this(null, null);
    }

    public void init() {
    }

    public void initContext(Object context) {
        initComponents();
    }

    public void initComponents() {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String n) {
        name = n;
    }

    public EngineSoundEvent getEngine() {
        return engine;
    }

    public void setEngine(EngineSoundEvent e) {
        engine = e;
    }

    public void setThrottle(int t) {
    }

    public void setSpeed(float s) {
    }

    public void startButtonClick() {
    }

    public void setButtonDelay(long t) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChangeEvent(PropertyChangeEvent evt) {
        //Object[] listeners = listenerList.getListenerList();

        for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
            l.propertyChange(evt);
        }
    }

}

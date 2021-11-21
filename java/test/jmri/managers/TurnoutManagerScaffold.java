package jmri.managers;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.time.LocalDateTime;
import java.util.Comparator;

import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import javax.annotation.*;

/**
 * Dummy implementation of TurnoutManager for testing purposes.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class TurnoutManagerScaffold implements TurnoutManager {

    @Override
    public Turnout provideTurnout(@Nonnull String name) {
        return null;
    }

    @Override
    public Turnout getTurnout(@Nonnull String name) {
        return null;
    }

    @Override
    public Turnout getBySystemName(@Nonnull String systemName) {
        return null;
    }

    @Override
    public Turnout getByUserName(@Nonnull String userName) {
        return null;
    }

    @Override
    public Turnout newTurnout(@Nonnull String systemName, String userName) {
        return null;
    }

    @Override
    public int getObjectCount() { return -1;}

    @Override
    @Nonnull
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public java.util.List<String> getSystemNameList() {
        return null;
    }

    @Override
    @CheckForNull
    public java.util.SortedSet<Turnout> getNamedBeanSet() {
        return null;
    }

    @Override
    @CheckForNull
    public String getClosedText() {
        return null;
    }

    @Override
    @CheckForNull
    public String getThrownText() {
        return null;
    }

    @Override
    @CheckForNull
    public String[] getValidOperationTypes() {
        return null;
    }

    @Override
    public int askNumControlBits(@Nonnull String systemName) {
        return -1;
    }

    @Override
    public int askControlType(@Nonnull String systemName) {
        return -1;
    }

    @Override
    @Nonnull
    public String getSystemPrefix() {
        return " ";
    }

    @Override
    public char typeLetter() {
        return ' ';
    }

    @Override
    @Nonnull
    public Class<Turnout> getNamedBeanClass() {
        return Turnout.class;
    }

    @Override
    @Nonnull
    public String makeSystemName(@Nonnull String s) {
        return null;
    }

    @Override
    public void setPropertyChangesSilenced(@Nonnull String propertyName, boolean muted) {
        // do nothing
    }

    @Override
    public void dispose() {
    }

    @Override
    public void deleteBean(@Nonnull Turnout bean, @Nonnull String s) {
    }

    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    }

    @Override
    @Nonnull
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return new PropertyChangeListener[0];
    }

    @Override
    @Nonnull
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return new PropertyChangeListener[0];
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    }

    @Override
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }

    @Override
    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }

    @Override
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    }

    @Override
    @Nonnull
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return new VetoableChangeListener[0];
    }

    @Override
    @Nonnull
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return new VetoableChangeListener[0];
    }

    @Override
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    }

    @Override
    public void register(@Nonnull Turnout n) {
    }

    @Override
    public void deregister(@Nonnull Turnout n) {
    }

    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return NameValidity.VALID;
    }

    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {
        return curAddress;
    }

    @Override
    public boolean isControlTypeSupported(@Nonnull String systemName) {
        return false;
    }

    @Override
    public boolean isNumControlBitsSupported(@Nonnull String systemName) {
        return false;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    @Override
    public void setDefaultClosedSpeed(@Nonnull String speed) {
    }

    @Override
    public void setDefaultThrownSpeed(@Nonnull String speed) {
    }

    @Override
    public String getDefaultThrownSpeed() {
        return null;
    }

    @Override
    public String getDefaultClosedSpeed() {
        return null;
    }

    @Override
    public int getXMLOrder() {
        return -1;
    }

    @Override
    public Turnout getNamedBean(@Nonnull String name) {
        return null;
    }

    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        return " ";
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return plural ? "Turnouts" : "Turnout";
    }

    @Override
    public String getEntryToolTip() { return "No Help"; }

    /** {@inheritDoc} */
    @Override
    public void addDataListener(ManagerDataListener<Turnout> e) {}

    /** {@inheritDoc} */
    @Override
    public void removeDataListener(ManagerDataListener<Turnout> e) {}

    @Override
    @Nonnull
    public SystemConnectionMemo getMemo() {
        return new InternalSystemConnectionMemo("J", "Juliet");
    }

    @Override
    public int getOutputInterval() {
        return 0;
    }

    @Override
    public void setOutputInterval(int newInterval) {}

    @Override
    @Nonnull
    public LocalDateTime outputIntervalEnds() { return LocalDateTime.now(); }

}

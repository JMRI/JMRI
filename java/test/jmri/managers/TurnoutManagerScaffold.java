package jmri.managers;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;

/**
 * Dummy implementation of TurnoutManager for testing purposes.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
  */
public class TurnoutManagerScaffold implements TurnoutManager {

    @Override
    public Turnout provideTurnout(String name) {
        return null;
    }

    @Override
    public Turnout getTurnout(String name) {
        return null;
    }

    @Override
    public Turnout getBySystemName(String systemName) {
        return null;
    }

    @Override
    public Turnout getByUserName(String userName) {
        return null;
    }

    @Override
    public Turnout newTurnout(String systemName, String userName) {
        return null;
    }

    @Override
    public java.util.List<String> getSystemNameList() {
        return null;
    }

    @Override
    public java.util.List<Turnout> getNamedBeanList() {
        return null;
    }

    @Override
    public String[] getSystemNameArray() {
        return null;
    }

    @Override
    public String getClosedText() {
        return null;
    }

    @Override
    public String getThrownText() {
        return null;
    }

    @Override
    public String[] getValidOperationTypes() {
        return null;
    }

    @Override
    public int askNumControlBits(String systemName) {
        return -1;
    }

    @Override
    public int askControlType(String systemName) {
        return -1;
    }

    @Override
    public String getSystemPrefix() {
        return " ";
    }

    @Override
    public char typeLetter() {
        return ' ';
    }

    @Override
    public String makeSystemName(String s) {
        return null;
    }

    @Override
    public String normalizeSystemName(String inputName) throws NamedBean.BadSystemNameException {
        return inputName;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void deleteBean(Turnout bean, String s) {
    }

    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    @Override
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }

    @Override
    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }

    @Override
    public void register(Turnout n) {
    }

    @Override
    public void deregister(Turnout n) {
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        return curAddress;
    }

    @Override
    public boolean isControlTypeSupported(String systemName) {
        return false;
    }

    @Override
    public boolean isNumControlBitsSupported(String systemName) {
        return false;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public void setDefaultClosedSpeed(String speed) {
    }

    @Override
    public void setDefaultThrownSpeed(String speed) {
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
    public Turnout getBeanBySystemName(String systemName) {
        return null;
    }

    @Override
    public Turnout getBeanByUserName(String userName) {
        return null;
    }

    @Override
    public Turnout getNamedBean(String name) {
        return null;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        return " ";
    }

    @Override
    public String getBeanTypeHandled() {
        return "Turnout";
    }

    @Override
    public String getEntryToolTip() { return "No Help"; }

}

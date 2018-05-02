package jmri;

import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.managers.AbstractManager;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Implementation of a Transit Manager
 * <P>
 * This doesn't need an interface, since Transits are globaly implemented,
 * instead of being system-specific.
 * <P>
 * Note that Transit system names must begin with IZ, and be followed by a
 * string, usually, but not always, a number. All alphabetic characters in a
 * Transit system name must be upper case. This is enforced when a Transit is
 * created.
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Dave Duchamp Copyright (C) 2008, 2011
 */
public class TransitManager extends AbstractManager<Transit> implements PropertyChangeListener, InstanceManagerAutoDefault {

    public TransitManager() {
        super();
        InstanceManager.getDefault(jmri.SectionManager.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.TRANSITS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'Z';
    }

    /**
     * Create a new Transit if the Transit does not exist.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Transit or null if a Transit with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Transit
     */
    public Transit createNewTransit(String systemName, String userName) {
        // check system name
        if ((systemName == null) || (systemName.length() < 1)) {
            // no valid system name entered, return without creating
            return null;
        }
        String sysName = systemName;
        if ((sysName.length() < 2) || (!sysName.substring(0, 2).equals("IZ"))) {
            sysName = "IZ" + sysName;
        }
        // Check that Transit does not already exist
        Transit z;
        if (userName != null && !userName.equals("")) {
            z = getByUserName(userName);
            if (z != null) {
                return null;
            }
        }
        String sName = sysName.toUpperCase().trim();
        z = getBySystemName(sysName);
        if (z == null) {
            z = getBySystemName(sName);
        }
        if (z != null) {
            return null;
        }
        // Transit does not exist, create a new Transit
        z = new Transit(sName, userName);
        // save in the maps
        register(z);
        return z;
    }

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     * <p>
     * Note: Since system names should be kept short for use in Dispatcher,
     * automatically generated system names are in the form {@code IZnn}, where
     * {@code nn} is the first available number.
     *
     * @param userName the desired user name
     * @return a new Transit or null if userName is already associated with
     *         another Transit
     */
    public Transit createNewTransit(String userName) {
        boolean found = false;
        String testName = "";
        Transit z;
        while (!found) {
            int nextAutoTransitRef = lastAutoTransitRef + 1;
            testName = "IZ" + nextAutoTransitRef;
            z = getBySystemName(testName);
            if (z == null) {
                found = true;
            }
            lastAutoTransitRef = nextAutoTransitRef;
        }
        return createNewTransit(testName, userName);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoTransitRef = 0;

    /**
     * Get an existing Transit. First looks up assuming that name is a User
     * Name. If this fails looks up assuming that name is a System Name. If both
     * fail, returns null.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public Transit getTransit(String name) {
        Transit z = getByUserName(name);
        if (z != null) {
            return z;
        }
        return getBySystemName(name);
    }

    public Transit getBySystemName(String name) {
        String key = name.toUpperCase();
        return  _tsys.get(key);
    }

    public Transit getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
    }

    /**
     * Remove an existing Transit.
     *
     * @param z the transit to remove
     */
    public void deleteTransit(Transit z) {
        // delete the Transit
        deregister(z);
        z.dispose();
    }

    /**
     * Get a list of Transits which use a specified Section.
     *
     * @param s the section to check Transits against
     * @return a list, possibly empty, of Transits using s
     */
    public ArrayList<Transit> getListUsingSection(Section s) {
        ArrayList<Transit> list = new ArrayList<>();
        List<String> tList = getSystemNameList();
        for (int i = 0; i < tList.size(); i++) {
            String tName = tList.get(i);
            if ((tName != null) && (tName.length() > 0)) {
                Transit tTransit = getTransit(tName);
                if (tTransit != null) {
                    if (tTransit.containsSection(s)) {
                        // this Transit uses the specified Section
                        list.add(tTransit);
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Transit> getListUsingBlock(Block b) {
        ArrayList<Transit> list = new ArrayList<>();
        List<String> tList = getSystemNameList();
        for (int i = 0; i < tList.size(); i++) {
            String tName = tList.get(i);
            if ((tName != null) && (tName.length() > 0)) {
                Transit tTransit = getTransit(tName);
                if (tTransit != null) {
                    if (tTransit.containsBlock(b)) {
                        // this Transit uses the specified Section
                        list.add(tTransit);
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Transit> getListEntryBlock(Block b) {
        ArrayList<Transit> list = new ArrayList<>();
        List<String> tList = getSystemNameList();
        for (int i = 0; i < tList.size(); i++) {
            String tName = tList.get(i);
            if ((tName != null) && (tName.length() > 0)) {
                Transit tTransit = getTransit(tName);
                if (tTransit != null) {
                    ArrayList<Block> entryBlock = tTransit.getEntryBlocksList();
                    if (entryBlock.contains(b)) {
                        // this Transit uses the specified Section
                        list.add(tTransit);
                    }
                }
            }
        }
        return list;
    }

    /**
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    static public TransitManager instance() {
        return InstanceManager.getDefault(TransitManager.class);
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameTransit");
    }
}

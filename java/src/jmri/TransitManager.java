package jmri;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.managers.AbstractManager;

/**
 * Implementation of a Transit Manager
 * <p>
 * This doesn't need an interface, since Transits are globaly implemented,
 * instead of being system-specific.
 * <p>
 * Note that Transit system names must begin with system prefix and type character,
 * usually IZ, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Transit is created.
 * <br>
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
 * @author Dave Duchamp Copyright (C) 2008, 2011
 */
public class TransitManager extends AbstractManager<Transit> implements InstanceManagerAutoDefault {

    public TransitManager() {
        super();
        addVetoListener();
    }
    
    final void addVetoListener(){
        InstanceManager.getDefault(SectionManager.class).addVetoableChangeListener(this);
    } 

    @Override
    public int getXMLOrder() {
        return Manager.TRANSITS;
    }

    @Override
    public char typeLetter() {
        return 'Z';
    }

    /**
     * Create a new Transit if the Transit does not exist.
     * This is NOT a provide method.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Transit
     * @throws NamedBean.BadNameException if a Transit with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Transit.
     */
    @Nonnull
    public Transit createNewTransit(@CheckForNull String systemName, String userName) throws NamedBean.BadNameException {
        // check system name
        if ((systemName == null) || (systemName.isEmpty())) {
            throw new NamedBean.BadSystemNameException("Transit System Name cannot be empty or null.", // NOI18N
                Bundle.getMessage("InvalidBeanSystemNameEmpty",getBeanTypeHandled(false)));
        }
        String sysName = systemName;
        if (!sysName.startsWith(getSystemNamePrefix())) {
            sysName = makeSystemName(sysName);
        }
        // Check that Transit does not already exist
        Transit z;
        if (userName != null && !userName.isEmpty()) {
            z = getByUserName(userName);
            if (z != null) {
                throw new NamedBean.BadUserNameException("Transit UserName \""+userName+"\" Already Exists.", // NOI18N
                Bundle.getMessage("InvalidUserNameAlreadyExists",getBeanTypeHandled(false),sysName));
            }
        }
        z = getBySystemName(sysName);
        if (z != null) {
            throw new NamedBean.DuplicateSystemNameException("Transit SytemName \""+sysName+"\" Already Exists.", // NOI18N
                Bundle.getMessage("InvalidSytemNameAlreadyExists",getBeanTypeHandled(false),sysName));
        }
        // Transit does not exist, create a new Transit
        z = new Transit(sysName, userName);
        // save in the maps
        register(z);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

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
     * @return a new Transit
     * @throws NamedBean.BadNameException if userName is already associated with
     *         another Transit
     */
    @Nonnull
    public Transit createNewTransit(String userName) throws NamedBean.BadNameException {
        return createNewTransit(getAutoSystemName(), userName);
    }

    /**
     * Get an existing Transit.
     * First looks up assuming that name is a User
     * Name. If this fails looks up assuming that name is a System Name.
     * If both fail, returns null.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    public Transit getTransit(String name) {
        Transit z = getByUserName(name);
        return (z != null ? z : getBySystemName(name));
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
     * @return a list, possibly empty, of Transits using section s.
     */
    @Nonnull
    public ArrayList<Transit> getListUsingSection(Section s) {
        ArrayList<Transit> list = new ArrayList<>();
        for (Transit tTransit : getNamedBeanSet()) {
            if (tTransit.containsSection(s)) {
                // this Transit uses the specified Section
                list.add(tTransit);
            }
        }
        return list;
    }

    @Nonnull
    public ArrayList<Transit> getListUsingBlock(Block b) {
        ArrayList<Transit> list = new ArrayList<>();
        for (Transit tTransit : getNamedBeanSet()) {
            if (tTransit.containsBlock(b)) {
                // this Transit uses the specified Section
                list.add(tTransit);
            }
        }
        return list;
    }

    @Nonnull
    public ArrayList<Transit> getListEntryBlock(Block b) {
        ArrayList<Transit> list = new ArrayList<>();
        for (Transit tTransit : getNamedBeanSet()) {
            ArrayList<Block> entryBlock = tTransit.getEntryBlocksList();
            if (entryBlock.contains(b)) {
                // this Transit uses the specified Section
                list.add(tTransit);
            }
        }
        return list;
    }
    
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameTransits" : "BeanNameTransit");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Transit> getNamedBeanClass() {
        return Transit.class;
    }
    
    @Override
    public void dispose() {
        InstanceManager.getDefault(SectionManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransitManager.class);

}

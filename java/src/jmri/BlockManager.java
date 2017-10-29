package jmri;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterEntry;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a BlockManager.
 * <P>
 * Note that this does not enforce any particular system naming convention.
 * <P>
 * Note this is a concrete class, unlike the interface/implementation pairs of
 * most Managers, because there are currently only one implementation for
 * Blocks.
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
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class BlockManager extends AbstractManager<Block> implements PropertyChangeListener, VetoableChangeListener, InstanceManagerAutoDefault {

    public BlockManager() {
        super();
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
    }

    @Override
    @CheckReturnValue
    public int getXMLOrder() {
        return Manager.BLOCKS;
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    @CheckReturnValue
    public char typeLetter() {
        return 'B';
    }

    private boolean saveBlockPath = true;

    @CheckReturnValue
    public boolean isSavedPathInfo() {
        return saveBlockPath;
    }

    public void setSavedPathInfo(boolean save) {
        saveBlockPath = save;
    }

    /**
     * Method to create a new Block only if it does not exist
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return null if a Block with the same systemName or userName already
     *         exists, or if there is trouble creating a new Block
     */
    @CheckForNull
    public Block createNewBlock(@Nonnull String systemName, @CheckForNull String userName) {
        // Check that Block does not already exist
        Block r;
        if (userName != null && !userName.equals("")) {
            r = getByUserName(userName);
            if (r != null) {
                return null;
            }
        }
        r = getBySystemName(systemName);
        if (r != null) {
            return null;
        }
        // Block does not exist, create a new Block
        String sName = systemName.toUpperCase();
        r = new Block(sName, userName);
        // save in the maps
        register(r);
        /*The following keeps trace of the last created auto system name.
         currently we do not reuse numbers, although there is nothing to stop the
         user from manually recreating them*/
        if (systemName.startsWith("IB:AUTO:")) {
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                if (autoNumber > lastAutoBlockRef) {
                    lastAutoBlockRef = autoNumber;
                }
            } catch (NumberFormatException e) {
                log.warn("Auto generated SystemName {} is not in the correct format", systemName);
            }
        }
        try {
            r.setBlockSpeed("Global"); // NOI18N
        } catch (JmriException ex) {
            log.error(ex.toString());
        }
        return r;
    }

    /**
     * Method to create a new Block using an automatically incrementing system
     * name.
     *
     * @param userName the user name for the new block
     * @return null if a Block with the same systemName or userName already
     *         exists, or if there is trouble creating a new Block.
     */
    @CheckForNull
    public Block createNewBlock(@Nonnull String userName) {
        int nextAutoBlockRef = lastAutoBlockRef + 1;
        StringBuilder b = new StringBuilder("IB:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoBlockRef);
        b.append(nextNumber);
        return createNewBlock(b.toString(), userName);
    }

    /**
     * If the Block exists, return it, otherwise create a new one and return it.
     * If the argument starts with the system prefix and type letter, usually
     * "IB", then the argument is considered a system name, otherwise it's
     * considered a user name and a system name is automatically created.
     *
     * @param name the system name or the user name for the block
     * @return a new or existing Block
     * @throws IllegalArgumentException if cannot create block; never returns
     *                                  null
     */
    @Nonnull
    public Block provideBlock(@Nonnull String name) {
        Block b = getBlock(name);
        if (b != null) {
            return b;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            b = createNewBlock(name, null);
        } else {
            b = createNewBlock(makeSystemName(name), null);
        }
        if (b == null) {
            throw new IllegalArgumentException("Could not create block \"" + name + "\"");
        }
        return b;
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoBlockRef = 0;

    /**
     * Method to get an existing Block. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     *
     * @param name the name of an existing block
     * @return a Block or null if none found
     */
    @CheckReturnValue
    @CheckForNull
    public Block getBlock(@Nonnull String name) {
        Block r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    @CheckReturnValue
    @CheckForNull
    public Block getBySystemName(@Nonnull String name) {
        String key = name.toUpperCase();
        return _tsys.get(key);
    }

    @CheckReturnValue
    @CheckForNull
    public Block getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    @CheckReturnValue
    @CheckForNull
    public Block getByDisplayName(@Nonnull String key) {
        // First try to find it in the user list.
        // If that fails, look it up in the system list
        Block retv = this.getByUserName(key);
        if (retv == null) {
            retv = this.getBySystemName(key);
        }
        // If it's not in the system list, go ahead and return null
        return (retv);
    }

    /**
     * @return the default BlockManager instance
     * @deprecated since 4.9.1; use
     * {@link InstanceManager#getDefault(Class)} instead
     */
    @Deprecated
    @CheckForNull
    static public BlockManager instance() {
        return InstanceManager.getDefault(BlockManager.class);
    }

    String defaultSpeed = "Normal";

    /**
     * @param speed the speed
     * @throws IllegalArgumentException if provided speed is invalid
     */
    public void setDefaultSpeed(@Nonnull String speed) {
        if (defaultSpeed.equals(speed)) {
            return;
        }

        try {
            Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            try {
                InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Value of requested default block speed \"" + speed + "\" is not valid");
            }
        }
        String oldSpeed = defaultSpeed;
        defaultSpeed = speed;
        firePropertyChange("DefaultBlockSpeedChange", oldSpeed, speed);
    }

    @CheckReturnValue
    @Nonnull
    public String getDefaultSpeed() {
        return defaultSpeed;
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameBlock");
    }

    /**
     * Returns a list of blocks which the supplied roster entry appears to be
     * occupying. A block is assumed to contain this roster entry if its value
     * is the RosterEntry itself, or a string with the entry's id or dcc
     * address.
     *
     * @param re the roster entry
     * @return list of block system names
     */
    @CheckReturnValue
    @Nonnull
    public List<Block> getBlocksOccupiedByRosterEntry(@Nonnull RosterEntry re) {
        List<Block> blockList = new ArrayList<>();

        getSystemNameList().stream().forEach((sysName) -> {
            Block b = getBySystemName(sysName);
            Object o = b.getValue();
            if (o != null) {
                if (o instanceof RosterEntry && o == re) {
                    blockList.add(b);
                } else if (o.toString().equals(re.getId()) || o.toString().equals(re.getDccAddress())) {
                    blockList.add(b);
                }
            }
        });

        return blockList;
    }

    private final static Logger log = LoggerFactory.getLogger(BlockManager.class);
}

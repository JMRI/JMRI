package jmri;

import java.beans.PropertyChangeEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;

/**
 * Basic implementation of a BlockManager.
 * <p>
 * Note that this does not enforce any particular system naming convention.
 * <p>
 * Note this is a concrete class, unlike the interface/implementation pairs of
 * most Managers, because there are currently only one implementation for
 * Blocks.
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
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class BlockManager extends AbstractManager<Block> implements ProvidingManager<Block>, InstanceManagerAutoDefault {

    private final String powerManagerChangeName;

    public BlockManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
        InstanceManager.getList(PowerManager.class).forEach(pm -> pm.addPropertyChangeListener(this));
        powerManagerChangeName = InstanceManager.getListPropertyName(PowerManager.class);
        InstanceManager.addPropertyChangeListener(this);
    }

    @Override
    @CheckReturnValue
    public int getXMLOrder() {
        return Manager.BLOCKS;
    }

    @Override
    @CheckReturnValue
    public char typeLetter() {
        return 'B';
    }

    @Override
    public Class<Block> getNamedBeanClass() {
        return Block.class;
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
     * Create a new Block, only if it does not exist.
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
        r = new Block(systemName, userName);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        // save in the maps
        register(r);
        try {
            r.setBlockSpeed("Global"); // NOI18N
        } catch (JmriException ex) {
            log.error("{}", ex.getMessage());
        }
        return r;
    }

    /**
     * Create a new Block using an automatically incrementing system
     * name.
     *
     * @param userName the user name for the new block
     * @return null if a Block with the same systemName or userName already
     *         exists, or if there is trouble creating a new Block.
     */
    @CheckForNull
    public Block createNewBlock(@Nonnull String userName) {
        return createNewBlock(getAutoSystemName(), userName);
    }

    /**
     * If the Block exists, return it, otherwise create a new one and return it.
     * If the argument starts with the system prefix and type letter, usually
     * "IB", then the argument is considered a system name, otherwise it's
     * considered a user name and a system name is automatically created.
     *
     * @param name the system name or the user name for the block
     * @return a new or existing Block
     * @throws IllegalArgumentException if cannot create block or no name supplied; never returns null
     */
    @Nonnull
    public Block provideBlock(@Nonnull String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Could not create block, no name supplied");
        }
        Block b = getBlock(name);
        if (b != null) {
            return b;
        }
        if (name.startsWith(getSystemNamePrefix())) {
            b = createNewBlock(name, null);
        } else {
            b = createNewBlock(name);
        }
        if (b == null) {
            throw new IllegalArgumentException("Could not create block \"" + name + "\"");
        }
        return b;
    }

    /**
     * Get an existing Block. First looks up assuming that name is a
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
    public Block getBySystemName(@Nonnull String key) {
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

    private String defaultSpeed = "Normal";

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
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Value of requested default block speed \"" + speed + "\" is not valid", ex);
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
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameBlocks" : "BeanNameBlock");
    }

    /**
     * Get a list of blocks which the supplied roster entry appears to be
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
        getNamedBeanSet().stream().forEach(b -> {
            if (b != null) {
                Object obj = b.getValue();
                if ((obj instanceof RosterEntry && obj == re) ||
                        obj.toString().equals(re.getId()) ||
                        obj.toString().equals(re.getDccAddress())) {
                    blockList.add(b);
                }
            }
        });
        return blockList;
    }


    private Instant lastTimeLayoutPowerOn; // the most recent time any power manager had a power ON event

    /**
     * Listen for changes to the power state from any power managers
     * in use in order to track how long it's been since power was applied
     * to the layout. This information is used in {@link Block#goingActive()}
     * when deciding whether to restore a block's last value.
     *
     * Also listen for additions/removals or PowerManagers
     *
     * @param e the change event
     */

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (jmri.PowerManager.POWER.equals(e.getPropertyName())) {
            try {
                PowerManager pm = (PowerManager) e.getSource();
                if (pm.getPower() == jmri.PowerManager.ON) {
                    lastTimeLayoutPowerOn = Instant.now();
                }
            } catch (JmriException | NoSuchMethodError xe) {
                // do nothing
            }
        }
        if (powerManagerChangeName.equals(e.getPropertyName())) {
            if (e.getNewValue() == null) {
                // powermanager has been removed
                PowerManager pm = (PowerManager) e.getOldValue();
                pm.removePropertyChangeListener(this);
            } else {
                // a powermanager has been added
                PowerManager pm = (PowerManager) e.getNewValue();
                pm.addPropertyChangeListener(this);
            }
        }
    }

    /**
     * Get the amount of time since the layout was last powered up,
     * in milliseconds. If the layout has not been powered up as far as
     * JMRI knows it returns a very long time indeed.
     *
     * @return long int
     */
    public long timeSinceLastLayoutPowerOn() {
        if (lastTimeLayoutPowerOn == null) {
            return Long.MAX_VALUE;
        }
        return Instant.now().toEpochMilli() - lastTimeLayoutPowerOn.toEpochMilli();
    }

    @Override
    @Nonnull
    public Block provide(@Nonnull String name) {
        return provideBlock(name);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockManager.class);

}

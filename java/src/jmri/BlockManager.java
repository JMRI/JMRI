package jmri;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class BlockManager extends AbstractManager
        implements java.beans.PropertyChangeListener, java.beans.VetoableChangeListener {

    public BlockManager() {
        super();
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.reporterManagerInstance().addVetoableChangeListener(this);
    }

    public int getXMLOrder() {
        return Manager.BLOCKS;
    }

    public String getSystemPrefix() {
        return "I";
    }

    public char typeLetter() {
        return 'B';
    }

    private boolean saveBlockPath = true;

    public boolean savePathInfo() {
        return saveBlockPath;
    }

    public void savePathInfo(boolean save) {
        saveBlockPath = save;
    }

    /**
     * Method to create a new Block if it does not exist Returns null if a Block
     * with the same systemName or userName already exists, or if there is
     * trouble creating a new Block.
     */
    public Block createNewBlock(String systemName, String userName) {
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
                log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
            }
        }
        try {
            r.setBlockSpeed("Global");
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }
        return r;
    }

    public Block createNewBlock(String userName) {
        int nextAutoBlockRef = lastAutoBlockRef + 1;
        StringBuilder b = new StringBuilder("IB:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoBlockRef);
        b.append(nextNumber);
        return createNewBlock(b.toString(), userName);
    }

    public Block provideBlock(String name) {
        Block b = getBlock(name);
        if (b != null) {
            return b;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            return createNewBlock(name, null);
        } else {
            return createNewBlock(makeSystemName(name), null);
        }
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoBlockRef = 0;

    /**
     * Method to get an existing Block. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     */
    public Block getBlock(String name) {
        Block r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    public Block getBySystemName(String name) {
        String key = name.toUpperCase();
        return (Block) _tsys.get(key);
    }

    public Block getByUserName(String key) {
        return (Block) _tuser.get(key);
    }

    public Block getByDisplayName(String key) {
        // First try to find it in the user list.
        // If that fails, look it up in the system list
        Block retv = this.getByUserName(key);
        if (retv == null) {
            retv = this.getBySystemName(key);
        }
        // If it's not in the system list, go ahead and return null
        return (retv);
    }

    static BlockManager _instance = null;

    static public BlockManager instance() {
        if (_instance == null) {
            _instance = new BlockManager();
        }
        return (_instance);
    }

    String defaultSpeed = "Normal";

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF", justification = "We are validating user input however the value is stored in its original format")
    public void setDefaultSpeed(String speed) throws JmriException {
        if (speed == null) {
            throw new JmriException("Value of requested default thrown speed can not be null");
        }
        if (defaultSpeed.equals(speed)) {
            return;
        }

        try {
            Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            try {
                jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
            } catch (Exception ex) {
                throw new JmriException("Value of requested default block speed is not valid");
            }
        }
        String oldSpeed = defaultSpeed;
        defaultSpeed = speed;
        firePropertyChange("DefaultBlockSpeedChange", oldSpeed, speed);
    }

    public String getDefaultSpeed() {
        return defaultSpeed;
    }

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameBlock");
    }
    
    /**
     * Returns a list of blocks which the supplied roster entry appears to
     * be occupying. A block is assumed to contain this roster entry if its value
     * is the RosterEntry itself, or a string with the entry's id or dcc address.
     * 
     * @param re the roster entry
     * @return list of block system names
     */
    public List<Block> getBlocksOccupiedByRosterEntry(RosterEntry re) {
        List<Block> blockList = new ArrayList<>();
        
        for (String sysName : getSystemNameList()) {
            Block b = getBySystemName(sysName);
            Object o = b.getValue();
            if (o != null) {
                if (o instanceof jmri.jmrit.roster.RosterEntry && o == re) {
                    blockList.add(b);
                } else if (o.toString().equals(re.getId()) || o.toString().equals(re.getDccAddress())){
                    blockList.add(b);
                }
            }
        }
        
        return blockList;
    }

    private final static Logger log = LoggerFactory.getLogger(BlockManager.class.getName());
}


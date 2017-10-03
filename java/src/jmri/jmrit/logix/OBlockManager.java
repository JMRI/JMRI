package jmri.jmrit.logix;

import javax.annotation.Nonnull;
import jmri.managers.AbstractManager;

/**
 * Basic Implementation of a OBlockManager.
 * <P>
 * Note that this does not enforce any particular system naming convention.
 * <P>
 * Note this is a concrete class, there are now 2 types of Blocks (LayoutBlocks
 * use a Block member. LBlocks use inheritance. Perhaps now the proxyManager
 * strategy of interface/implementation pairs like other Managers should be
 * implemented.
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
 * @author Pete Cressman Copyright (C) 2009
 */
public class OBlockManager extends AbstractManager<OBlock>
        implements java.beans.PropertyChangeListener, jmri.InstanceManagerAutoDefault {

    public OBlockManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.OBLOCKS;
    }

    @Nonnull@Override
 public String getSystemPrefix() {
        return "O";
    }

    @Override
    public char typeLetter() {
        return 'B';
    }

    /**
     * Method to create a new OBlock if it does not exist Returns null if a
     * OBlock with the same systemName or userName already exists, or if there
     * is trouble creating a new OBlock.
     */
    public OBlock createNewOBlock(String systemName, String userName) {
        // Check that OBlock does not already exist
        OBlock r;
        if (userName != null && (userName.trim().length() > 0)) {
            r = getByUserName(userName);
            if (r != null) {
                return null;
            }
        }
        String sName = systemName.toUpperCase();
        if (!sName.startsWith("OB")) {
            return null;
        }
        if (sName.length() < 3) {
            return null;
        }
        r = getBySystemName(sName);
        if (r != null) {
            return null;
        }
        // OBlock does not exist, create a new OBlock
        r = new OBlock(sName, userName);
        // save in the maps
        register(r);
        return r;
    }

    /**
     * Method to get an existing OBlock. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     */
    public OBlock getOBlock(String name) {
        OBlock r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    public OBlock getBySystemName(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        String key = name.toUpperCase();
        return _tsys.get(key);
    }

    public OBlock getByUserName(String key) {
        if (key == null || key.trim().length() == 0) {
            return null;
        }
        return  _tuser.get(key);
    }

    @Nonnull public OBlock provideOBlock(String name) throws IllegalArgumentException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name \""+name+"\" invalid");
        }
        OBlock ob = getByUserName(name);
        if (ob == null) {
            ob = getBySystemName(name);
        }
        if (ob == null) {
            ob = createNewOBlock(name, null);
            if (ob == null) throw new IllegalArgumentException("could not create OBlock \""+name+"\"");
            register(ob);
        }
        return ob;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameOBlock");
    }
}

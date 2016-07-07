package jmri.jmrit.logix;

import java.io.File;
import jmri.managers.AbstractManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a WarrantManager.
 * <P>
 * Note this is a concrete class.
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
 * @author Pete Cressman Copyright (C) 2009
 */
public class WarrantManager extends AbstractManager
        implements java.beans.PropertyChangeListener, jmri.InstanceManagerAutoDefault {

    static private WarrantPreferences warrantPreferences = null;

    public WarrantManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.WARRANTS;
    }

    public String getSystemPrefix() {
        return "I";
    }

    public char typeLetter() {
        return 'W';
    }

    /**
     * Method to create a new Warrant if it does not exist Returns null if a
     * Warrant with the same systemName or userName already exists, or if there
     * is trouble creating a new Warrant.
     */
    public Warrant createNewWarrant(String systemName, String userName, boolean SCWa, long TTP) {
        log.debug("createNewWarrant "+systemName+" SCWa="+SCWa);
        // Check that Warrant does not already exist
        Warrant r;
        if (userName != null && userName.trim().length() > 0) {
            r = getByUserName(userName);
            if (r == null) {
                r = getBySystemName(systemName);
            }
            if (r != null) {
                log.warn("Warrant " + r.getDisplayName() + "  exits.");
                return null;
            }
        }
        String sName = systemName.trim().toUpperCase();
        if ((sName.compareTo(systemName) != 0) || !sName.startsWith("IW") || sName.length() < 3) {
            log.error("Warrant system name \"" + systemName + "\" must be upper case  begining with \"IW\".");
            return null;
        }
        // Warrant does not exist, create a new Warrant
        if (SCWa) {
            r = new SCWarrant(sName, userName, TTP);
        } else {
            r = new Warrant(sName, userName);
        }
        // save in the maps
        register(r);
        return r;
    }

    /**
     * Method to get an existing Warrant. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     */
    public Warrant getWarrant(String name) {
        Warrant r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    public Warrant getBySystemName(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        String key = name.toUpperCase();
        return (Warrant) _tsys.get(key);
    }

    public Warrant getByUserName(String key) {
        if (key == null || key.trim().length() == 0) {
            return null;
        }
        return (Warrant) _tuser.get(key);
    }

    public Warrant provideWarrant(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        Warrant w = getByUserName(name);
        if (w == null) {
            w = getBySystemName(name);
        }
        if (w == null) {
            w = createNewWarrant(name, null, false, 0);
        }
        return w;
    }

    static WarrantManager _instance = null;

    static public WarrantManager instance() {
        if (_instance == null) {
            _instance = new WarrantManager();
        }
        return (_instance);
    }

    static public WarrantPreferences warrantPreferencesInstance() {
        if (warrantPreferences == null) {
            if (jmri.InstanceManager.getOptionalDefault(jmri.jmrit.logix.WarrantPreferences.class) == null) {
                jmri.InstanceManager.store(new jmri.jmrit.logix.WarrantPreferences(FileUtil.getUserFilesPath()
                        + "signal" + File.separator + "WarrantPreferences.xml"), jmri.jmrit.logix.WarrantPreferences.class);
            }
            warrantPreferences = jmri.InstanceManager.getDefault(jmri.jmrit.logix.WarrantPreferences.class);
        }
        return warrantPreferences;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameWarrant");
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantManager.class.getName());
}

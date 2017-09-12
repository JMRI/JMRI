package jmri.jmrit.logix;

import java.util.HashMap;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.managers.AbstractManager;
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
public class WarrantManager extends AbstractManager<Warrant>
        implements java.beans.PropertyChangeListener, jmri.InstanceManagerAutoDefault {
    
    private HashMap<String, RosterSpeedProfile> _mergeProfiles;
    private HashMap<String, RosterSpeedProfile> _sessionProfiles;

    public WarrantManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.WARRANTS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'W';
    }

    /**
     * Method to create a new Warrant if it does not exist Returns null if a
     * Warrant with the same systemName or userName already exists, or if there
     * is trouble creating a new Warrant.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return an existing warrant if found or a new warrant
     */
    public Warrant createNewWarrant(String systemName, String userName, boolean SCWa, long TTP) {
        log.debug("createNewWarrant " + systemName + " SCWa="+SCWa);
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
     *
     * @param name the system name or user name
     * @return the warrant if found or null
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
        return _tsys.get(key);
    }

    public Warrant getByUserName(String key) {
        if (key == null || key.trim().length() == 0) {
            return null;
        }
        return _tuser.get(key);
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

    /**
     * Get the default WarrantManager.
     *
     * @return the default WarrantManager, creating it if necessary
     */
    public static WarrantManager getDefault() {
        return InstanceManager.getOptionalDefault(WarrantManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(WarrantManager.class, new WarrantManager());
        });
    }

    /**
     * Get the default WarrantManager.
     *
     * @return the default WarrantManager, creating it if necessary
     * @deprecated since 4.7.1; use {@link #getDefault()} instead
     */
    @Deprecated
    static public WarrantManager instance() {
        return getDefault();
    }

    /**
     * Get the default warrant preferences.
     *
     * @return the default preferences, created if necessary
     * @deprecated since 4.7.1; use
     * {@link jmri.jmrit.logix.WarrantPreferences#getDefault()} instead
     */
    @Deprecated
    static public WarrantPreferences warrantPreferencesInstance() {
        return WarrantPreferences.getDefault();
    }

    @Override
    public String getBeanTypeHandled() {
        return jmri.jmrit.logix.Bundle.getMessage("BeanNameWarrant");
    }
    
    protected void setSpeedProfiles(String id, RosterSpeedProfile merge, RosterSpeedProfile session) {
        if (_mergeProfiles == null) {
            _mergeProfiles = new HashMap<String, RosterSpeedProfile>();
            _sessionProfiles = new HashMap<String, RosterSpeedProfile>();
            if (jmri.InstanceManager.getNullableDefault(jmri.ShutDownManager.class) != null) {
                ShutDownTask shutDownTask = new WarrantShutdownTask("WarrantRosterSpeedProfileCheck");
                        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(shutDownTask);
            } else {
                log.error("No ShutDownManager for WarrantRosterSpeedProfileCheck");
            }
        }
        if (id != null && merge != null) {
            _mergeProfiles.put(id, merge);
            _sessionProfiles.put(id, session);
        }
    }
    
    protected RosterSpeedProfile getMergeProfile(String id) {
        if (_mergeProfiles == null) {
            return null;
        }
        return _mergeProfiles.get(id);
    }
    protected RosterSpeedProfile getSessionProfile(String id) {
        if (_sessionProfiles == null) {
            return null;
        }
        return _sessionProfiles.get(id);
    }
    
    protected HashMap<String, RosterSpeedProfile> getMergeProfiles() {
        return _mergeProfiles;
    }
    protected HashMap<String, RosterSpeedProfile> getSessionProfiles() {
        return _sessionProfiles;
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantManager.class);
}

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
 * <p>
 * Note this is a concrete class.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class WarrantManager extends AbstractManager<Warrant>
        implements jmri.InstanceManagerAutoDefault {
    
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
        if (!systemName.startsWith("IW") || systemName.length() < 3) {
            log.error("Warrant system name \"" + systemName + "\" must begin with \"IW\".");
            return null;
        }
        // Warrant does not exist, create a new Warrant
        if (SCWa) {
            r = new SCWarrant(systemName, userName, TTP);
        } else {
            r = new Warrant(systemName, userName);
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
        return _tsys.get(name);
    }

    public Warrant getByUserName(String key) {
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

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameWarrants" : "BeanNameWarrant");
    }
    
    protected void setSpeedProfiles(String id, RosterSpeedProfile merge, RosterSpeedProfile session) {
        if (_mergeProfiles == null) {
            _mergeProfiles = new HashMap<>();
            _sessionProfiles = new HashMap<>();
            ShutDownTask shutDownTask = new WarrantShutdownTask("WarrantRosterSpeedProfileCheck");
            jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(shutDownTask);
        }
        if (id != null) {
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

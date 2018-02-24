package jmri.managers;

import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a ReporterManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractReporterManager extends AbstractManager<Reporter>
        implements ReporterManager {

    @Override
    public int getXMLOrder() {
        return Manager.REPORTERS;
    }

    @Override
    public char typeLetter() {
        return 'R';
    }

    @Override
    public Reporter provideReporter(String sName) {
        Reporter t = getReporter(sName);
        if (t != null) {
            return t;
        }
        if (sName.startsWith(getSystemPrefix() + typeLetter())) {
            return newReporter(sName, null);
        } else {
            return newReporter(makeSystemName(sName), null);
        }
    }

    @Override
    public Reporter getReporter(String name) {
        Reporter t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public Reporter getBySystemName(String name) {
        return _tsys.get(name);
    }

    @Override
    public Reporter getByUserName(String key) {
        return _tuser.get(key);
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameReporter");
    }

    @Override
    public Reporter getByDisplayName(String key) {
        // First try to find it in the user list.
        // If that fails, look it up in the system list
        Reporter retv = this.getByUserName(key);
        if (retv == null) {
            retv = this.getBySystemName(key);
        }
        // If it's not in the system list, go ahead and return null
        return (retv);
    }

    @Override
    public Reporter newReporter(String systemName, String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new Reporter:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        // return existing if there is one
        Reporter r;
        if ((userName != null) && ((r = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != r) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + r.getSystemName() + ")");
            }
            return r;
        }
        if ((r = getBySystemName(systemName)) != null) {
            if ((r.getUserName() == null) && (userName != null)) {
                r.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found reporter via system name (" + systemName
                        + ") with non-null user name (" + userName + ")");
            }
            return r;
        }

        // doesn't exist, make a new one
        r = createNewReporter(systemName, userName);

        // save in the maps
        register(r);

        return r;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return Never null
     */
    abstract protected Reporter createNewReporter(String systemName, String userName);

    /**
     * A temporary method that determines if it is possible to add a range of
     * turnouts in numerical order eg. 10 to 30
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Reporter r = getBySystemName(prefix + typeLetter() + curAddress);
        if (r == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        r = getBySystemName(prefix + typeLetter() + iName);
        if (r != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
                r = getBySystemName(prefix + typeLetter() + iName);
                if (r == null) {
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    /**
     * Provide a manager-agnostic tooltip for the Add new item beantable pane.
     *
     * @return the tooltip
     */
    @Override
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractReporterManager.class);

}

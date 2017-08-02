package jmri.managers;

import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.Memory;
import jmri.MemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a MemoryManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractMemoryManager extends AbstractManager
        implements MemoryManager {

    @Override
    public int getXMLOrder() {
        return Manager.MEMORIES;
    }

    @Override
    public char typeLetter() {
        return 'M';
    }

    @Override
    public Memory provideMemory(String sName) {
        Memory t = getMemory(sName);
        if (t != null) {
            return t;
        }
        if (sName.startsWith("" + getSystemPrefix() + typeLetter())) {
            return newMemory(sName, null);
        } else {
            return newMemory(makeSystemName(sName), null);
        }
    }

    @Override
    public Memory getMemory(String name) {
        Memory t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public Memory getBySystemName(String name) {
        return (Memory) _tsys.get(name);
    }

    @Override
    public Memory getByUserName(String key) {
        return (Memory) _tuser.get(key);
    }

    @Override
    public Memory newMemory(String systemName, String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new Memory:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        // return existing if there is one
        Memory s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            // handle user name from request
            if (userName != null) {
                // check if already on set in Object, might be inconsistent
                if (!userName.equals(s.getUserName())) {
                    // this is a problem
                    log.warn("newMemory request for system name \"{}\" user name \"{}\" found memory with existing user name \"{}\"",
                            systemName, userName, s.getUserName());
                } else {
                    s.setUserName(userName);
                }
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewMemory(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) {
            throw new IllegalArgumentException();
        }

        // save in the maps
        register(s);

        /*The following keeps trace of the last created auto system name.  
         currently we do not reuse numbers, although there is nothing to stop the 
         user from manually recreating them*/
        if (systemName.startsWith("IM:AUTO:")) {
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                if (autoNumber > lastAutoMemoryRef) {
                    lastAutoMemoryRef = autoNumber;
                }
            } catch (NumberFormatException e) {
                log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
            }
        }
        return s;
    }

    @Override
    public Memory newMemory(String userName) {
        int nextAutoMemoryRef = lastAutoMemoryRef + 1;
        StringBuilder b = new StringBuilder("IM:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoMemoryRef);
        b.append(nextNumber);
        return newMemory(b.toString(), userName);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoMemoryRef = 0;

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @param systemName Memory system name
     * @param userName   Memory user name
     * @return a new Memory
     */
    @Nonnull
    abstract protected Memory createNewMemory(String systemName, String userName);

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameMemory");
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMemoryManager.class.getName());
}

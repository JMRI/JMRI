// AbstractMemoryManager.java

package jmri;

/**
 * Abstract partial implementation of a MemoryManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public abstract class AbstractMemoryManager extends AbstractManager
    implements MemoryManager {

    public char typeLetter() { return 'M'; }

    public Memory provideMemory(String name) {
        Memory t = getMemory(name);
        if (t!=null) return t;
        if (name.startsWith(""+systemLetter()+typeLetter()))
            return newMemory(name, null);
        else
            return newMemory(makeSystemName(name), null);
    }

    public Memory getMemory(String name) {
        Memory t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Memory getBySystemName(String key) {
        return (Memory)_tsys.get(key);
    }

    public Memory getByUserName(String key) {
        return (Memory)_tuser.get(key);
    }

    public Memory newMemory(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("new Memory:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null) log.error("SystemName cannot be null. UserName was "
                                        +( (userName==null) ? "null" : userName));
        // is system name in correct format?
        if (!systemName.startsWith(""+systemLetter()+typeLetter())) {
            log.error("Invalid system name for Memory: "+systemName
                            +" needed "+systemLetter()+typeLetter());
            return null;
        }

        // return existing if there is one
        Memory s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
            if (userName != null) log.warn("Found Memory via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewMemory(systemName, userName);

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Memory createNewMemory(String systemName, String userName);

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMemoryManager.class.getName());
}

/* @(#)AbstractMemoryManager.java */

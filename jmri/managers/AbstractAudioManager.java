// AbstractAudioManager.java

package jmri.managers;

import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.implementation.AbstractManager;

/**
 * Abstract partial implementation of an AudioManager.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author  Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.3 $
 */
public abstract class AbstractAudioManager extends AbstractManager
    implements AudioManager {

    public char typeLetter() { return 'A'; }

    public Audio provideAudio(String name) throws AudioException {
        Audio t = getAudio(name);
        if (t!=null) return t;
		String sName = name.toUpperCase();
        if (sName.startsWith(""+systemLetter()+typeLetter()))
            return newAudio(sName, null);
        else
            return newAudio(makeSystemName(sName), null);
    }

    public Audio getAudio(String name) {
        Audio t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Audio getBySystemName(String key) {
        String name = key.toUpperCase();
        return (Audio)_tsys.get(name);
    }

    public Audio getByUserName(String key) {
        return (Audio)_tuser.get(key);
    }

    public Audio newAudio(String sysName, String userName) throws AudioException {
        String systemName = sysName.toUpperCase();
        if (log.isDebugEnabled()) log.debug("new Audio:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null){ 
        	log.error("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        	return null;
        }
        // is system name in correct format?
        if ((!systemName.startsWith(""+systemLetter()+typeLetter()+Audio.BUFFER))
           &&(!systemName.startsWith(""+systemLetter()+typeLetter()+Audio.SOURCE))
           &&(!systemName.startsWith(""+systemLetter()+typeLetter()+Audio.LISTENER))
            ){
            log.error("Invalid system name for Audio: "+systemName
                            +" needed either "+systemLetter()+typeLetter()+Audio.BUFFER
                            +" or "+systemLetter()+typeLetter()+Audio.SOURCE
                            +" or "+systemLetter()+typeLetter()+Audio.LISTENER);
            throw new AudioException("Invalid system name for Audio: "+systemName
                            +" needed either "+systemLetter()+typeLetter()+Audio.BUFFER
                            +" or "+systemLetter()+typeLetter()+Audio.SOURCE
                            +" or "+systemLetter()+typeLetter()+Audio.LISTENER);
        }

        // return existing if there is one
        Audio s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found audio via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewAudio(systemName, userName);

        // save in the maps
        if (!(s==null)) register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName Audio object user name
     * @return never null
     * @throws AudioException if error occurs during creation
     */
    abstract protected Audio createNewAudio(String systemName, String userName) throws AudioException;

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractAudioManager.class.getName());
}

/* @(#)AbstractAudioManager.java */

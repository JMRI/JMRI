package jmri.managers;

import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of an AudioManager.
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
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudioManager extends AbstractManager
        implements AudioManager {

    @Override
    public char typeLetter() {
        return 'A';
    }

    @Override
    public Audio provideAudio(String name) throws AudioException {
        Audio t = getAudio(name);
        if (t != null) {
            return t;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            return newAudio(name, null);
        } else {
            return newAudio(makeSystemName(name), null);
        }
    }

    @Override
    public Audio getAudio(String name) {
        Audio t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public Audio getBySystemName(String key) {
        //return (Audio)_tsys.get(key);
        Audio rv = (Audio) _tsys.get(key);
        if (rv == null) {
            rv = (Audio) _tsys.get(key.toUpperCase());
        }
        return (rv);
    }

    @Override
    public Audio getByUserName(String key) {
        //return key==null?null:(Audio)_tuser.get(key);
        if (key == null) {
            return (null);
        }
        Audio rv = (Audio) _tuser.get(key);
        if (rv == null) {
            rv = this.getBySystemName(key);
        }
        return (rv);
    }

    @SuppressFBWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification="defensive programming check of @Nonnull argument")
    private void checkSystemName(@Nonnull String systemName, @CheckForNull String userName) {
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
            throw new IllegalArgumentException("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
        }
    }

    @Override
    public Audio newAudio(String systemName, String userName) throws AudioException {
        if (log.isDebugEnabled()) {
            log.debug("new Audio:"
                    + ((systemName == null) ? "null" : systemName) // NOI18N
                    + ";" + ((userName == null) ? "null" : userName)); // NOI18N
        }
        checkSystemName(systemName, userName);

        // is system name in correct format?
        if ((!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.BUFFER))
                && (!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.SOURCE))
                && (!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.LISTENER))) {
            log.error("Invalid system name for Audio: " + systemName
                    + " needed either " + getSystemPrefix() + typeLetter() + Audio.BUFFER // NOI18N
                    + " or " + getSystemPrefix() + typeLetter() + Audio.SOURCE // NOI18N
                    + " or " + getSystemPrefix() + typeLetter() + Audio.LISTENER);        // NOI18N
            throw new AudioException("Invalid system name for Audio: " + systemName
                    + " needed either " + getSystemPrefix() + typeLetter() + Audio.BUFFER
                    + " or " + getSystemPrefix() + typeLetter() + Audio.SOURCE
                    + " or " + getSystemPrefix() + typeLetter() + Audio.LISTENER);
        }

        // return existing if there is one
        Audio s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            log.debug("Found existing Audio (" + s.getSystemName() + "). Returning existing (1).");
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found audio via system name (" + systemName
                        + ") with non-null user name (" + userName + ")"); // NOI18N
            }
            log.debug("Found existing Audio (" + s.getSystemName() + "). Returning existing (2).");
            return s;
        }

        log.debug("Existing audio not found. Creating new. (" + systemName + ")");
        // doesn't exist, make a new one
        s = createNewAudio(systemName, userName);

        // save in the maps
        if (!(s == null)) {
            register(s);
        }

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName   Audio object user name
     * @return never null
     * @throws AudioException if error occurs during creation
     */
    abstract protected Audio createNewAudio(String systemName, String userName) throws AudioException;

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAudio");
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioManager.class.getName());
}

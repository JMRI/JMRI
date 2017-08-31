package jmri.managers;

import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of an AudioManager.
 * <p>
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
public abstract class AbstractAudioManager extends AbstractManager<Audio>
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

    @Override
    public Audio newAudio(@Nonnull String systemName, String userName) throws AudioException {
        Objects.requireNonNull(systemName, "SystemName cannot be null.");
        log.debug("new Audio:{};{}", systemName, userName);

        // is system name in correct format?
        if ((!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.BUFFER))
                && (!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.SOURCE))
                && (!systemName.startsWith("" + getSystemPrefix() + typeLetter() + Audio.LISTENER))) {
            log.error("Invalid system name for Audio: {} needed either {}{}{} or {}{}{} or {}{}{}",
                    systemName,
                    getSystemPrefix(), typeLetter(), Audio.BUFFER,
                    getSystemPrefix(), typeLetter(), Audio.SOURCE,
                    getSystemPrefix(), typeLetter(), Audio.LISTENER);
            throw new AudioException("Invalid system name for Audio: " + systemName
                    + " needed either " + getSystemPrefix() + typeLetter() + Audio.BUFFER
                    + " or " + getSystemPrefix() + typeLetter() + Audio.SOURCE
                    + " or " + getSystemPrefix() + typeLetter() + Audio.LISTENER);
        }

        // return existing if there is one
        Audio s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName());
            }
            log.debug("Found existing Audio ({}). Returning existing (1).", s.getSystemName());
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found audio via system name ({}) with non-null user name ({})", systemName, userName);
            }
            log.debug("Found existing Audio ({}). Returning existing (2).", s.getSystemName());
            return s;
        }

        log.debug("Existing audio not found. Creating new. ({})", systemName);
        // doesn't exist, make a new one
        s = createNewAudio(systemName, userName);

        // save in the maps
        if (s != null) {
            register(s);
        } else {
            // must have failed to create
            throw new IllegalArgumentException("can't create audio with System Name \"" + systemName + "\"");
        }

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @param systemName Audio object system name (for example IAS1, IAB4)
     * @param userName   Audio object user name
     * @return never null
     * @throws AudioException if error occurs during creation
     */
    abstract protected Audio createNewAudio(String systemName, String userName) throws AudioException;

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAudio");
    }

    @Override
    public final void cleanUp() {
        this.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioManager.class.getName());
}

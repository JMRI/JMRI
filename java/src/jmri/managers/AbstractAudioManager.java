package jmri.managers;

import javax.annotation.Nonnull;
import java.util.Objects;

import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.SignalSystem;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of an AudioManager.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudioManager extends AbstractManager<Audio>
        implements AudioManager {

    public AbstractAudioManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'A';
    }

    /** {@inheritDoc} */
    @Override
    public Audio provideAudio(@Nonnull String name) throws AudioException {
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

    /** {@inheritDoc} */
    @Override
    public Audio getAudio(@Nonnull String name) {
        Audio t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Audio getByUserName(String key) {
        //return key==null?null:_tuser.get(key);
        if (key == null) {
            return (null);
        }
        Audio rv = _tuser.get(key);
        if (rv == null) {
            rv = this.getBySystemName(key);
        }
        return (rv);
    }

    /** {@inheritDoc} */
    @Override
    public Audio newAudio(@Nonnull String systemName, String userName) throws AudioException {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was "+ ((userName == null) ? "null" : userName));  // NOI18N

        log.debug("new Audio: {}; {}", systemName, userName); // NOI18N
 
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
            log.debug("Found existing Audio ({}). Returning existing (1).", s.getSystemName());  // NOI18N
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found audio via system name (" + systemName
                        + ") with non-null user name (" + userName + ")"); // NOI18N
            }
            log.debug("Found existing Audio ({}). Returning existing (2).", s.getSystemName());
            return s;
        }

        log.debug("Existing audio not found. Creating new. ({})", systemName);  // NOI18N
        // doesn't exist, make a new one
        s = createNewAudio(systemName, userName);

        // save in the maps
        if (s != null) {
            register(s);
        } else {
            // must have failed to create
            throw new IllegalArgumentException("can't create audio with System Name \""+systemName+"\"");  // NOI18N
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
    abstract protected Audio createNewAudio(@Nonnull String systemName, String userName) throws AudioException;

    /** {@inheritDoc} */
    @Override
    @Nonnull 
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameAudios" : "BeanNameAudio");  // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Audio> getNamedBeanClass() {
        return Audio.class;
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioManager.class);
}

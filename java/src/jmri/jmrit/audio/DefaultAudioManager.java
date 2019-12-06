package jmri.jmrit.audio;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import jmri.Audio;
import jmri.AudioException;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide the concrete implementation for the Internal Audio Manager.
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
 *
 * @author Matthew Harris copyright (c) 2009
 */
public class DefaultAudioManager extends AbstractAudioManager {

    private static int countListeners = 0;
    private static int countSources = 0;
    private static int countBuffers = 0;

    public DefaultAudioManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Reference to the currently active AudioFactory. 
     * Because of underlying (external to Java) implementation details,
     * JMRI only ever has one AudioFactory, so we make this static.
     */
    private static AudioFactory activeAudioFactory = null;

    private static boolean initialised = false;

    private final TreeSet<Audio> listeners = new TreeSet<>(new jmri.util.NamedBeanComparator<>());
    private final TreeSet<Audio> buffers = new TreeSet<>(new jmri.util.NamedBeanComparator<>());
    private final TreeSet<Audio> sources = new TreeSet<>(new jmri.util.NamedBeanComparator<>());

    public final ShutDownTask audioShutDownTask = new QuietShutDownTask("AudioFactory Shutdown") {
        @Override
        public boolean execute() {
            InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
            return true;
        }
    };

    @Override
    public int getXMLOrder() {
        return jmri.Manager.AUDIO;
    }

    @Override
    protected synchronized Audio createNewAudio(String systemName, String userName) throws AudioException {

        if (activeAudioFactory == null) {
            log.debug("Initialise in createNewAudio");
            init();
        }

        Audio a = null;

        log.debug("sysName: " + systemName + " userName: " + userName);
        if (userName != null && _tuser.containsKey(userName)) {
            throw new AudioException("Duplicate name");
        }

        switch (systemName.charAt(2)) {

            case Audio.BUFFER: {
                if (countBuffers >= MAX_BUFFERS) {
                    log.error("Maximum number of buffers reached (" + countBuffers + ") " + MAX_BUFFERS);
                    throw new AudioException("Maximum number of buffers reached (" + countBuffers + ") " + MAX_BUFFERS);
                }
                countBuffers++;
                a = activeAudioFactory.createNewBuffer(systemName, userName);
                buffers.add(a);
                break;
            }
            case Audio.LISTENER: {
                if (countListeners >= MAX_LISTENERS) {
                    log.error("Maximum number of Listeners reached (" + countListeners + ") " + MAX_LISTENERS);
                    throw new AudioException("Maximum number of Listeners reached (" + countListeners + ") " + MAX_LISTENERS);
                }
                countListeners++;
                a = activeAudioFactory.createNewListener(systemName, userName);
                listeners.add(a);
                break;
            }
            case Audio.SOURCE: {
                if (countSources >= MAX_SOURCES) {
                    log.error("Maximum number of Sources reached (" + countSources + ") " + MAX_SOURCES);
                    throw new AudioException("Maximum number of Sources reached (" + countSources + ") " + MAX_SOURCES);
                }
                countSources++;
                a = activeAudioFactory.createNewSource(systemName, userName);
                sources.add(a);
                break;
            }
            default:
                throw new IllegalArgumentException();
        }

        return a;
    }

    @Override
    @Deprecated
    public List<String> getSystemNameList(char subType) {
        Set<Audio> tempSet = getNamedBeanSet();
        List<String> out = new ArrayList<>();
        tempSet.stream().forEach((audio) -> {
            if (audio.getSubType() == subType) {
                out.add(audio.getSystemName());
            }
        });
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<Audio> getNamedBeanSet(char subType) {
        switch (subType) {
            case Audio.BUFFER: {
                return Collections.unmodifiableSortedSet(buffers);
            }
            case Audio.LISTENER: {
                return Collections.unmodifiableSortedSet(listeners);
            }
            case Audio.SOURCE: {
                return Collections.unmodifiableSortedSet(sources);
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Attempt to create and initialise an AudioFactory, working
     * down a preference hierarchy. Result is in activeAudioFactory.
     * Uses null implementation to always succeed
     */
    private void createFactory() {
        // was a specific implementation requested?
        // define as jmri.jmrit.audio.NullAudioFactory to get headless CI form in testing
        String className = System.getProperty("jmri.jmrit.audio.DefaultAudioManager.implementation");
        // if present, determines the active factory class
        if (className != null) {
            log.debug("Try to initialise {} from property", className);
            try {
                Class<?> c = Class.forName(className);
                if (AudioFactory.class.isAssignableFrom(c)) {
                    activeAudioFactory = (AudioFactory) c.getConstructor().newInstance();
                    if (activeAudioFactory.init()) {
                        // all OK
                        return;
                    } else {
                        log.error("Specified jmri.jmrit.audio.DefaultAudioManager.implementation value {} did not initialize, continuing", className);
                    }
                } else {
                    log.error("Specified jmri.jmrit.audio.DefaultAudioManager.implementation value {} is not a jmri.AudioFactory subclass, continuing", className);
                }
            } catch (
                    ClassNotFoundException |
                    InstantiationException |
                    IllegalAccessException |
                    java.lang.reflect.InvocationTargetException |
                    NoSuchMethodException |
                    SecurityException e) {
                log.error("Unable to instantiate AudioFactory class {} with default constructor", className);
                // and proceed to fallback choices
            }
        }
        
//      // Try to initialise LWJGL
//      log.debug("Try to initialise LWJGLAudioFactory");
//      activeAudioFactory = new LWJGLAudioFactory();
//      if (activeAudioFactory.init()) return;
//
        // Next try JOAL
        log.debug("Try to initialise JoalAudioFactory");
        activeAudioFactory = new JoalAudioFactory();
        if (activeAudioFactory.init()) return;

        // fall-back to JavaSound
        log.debug("Try to initialise JavaSoundAudioFactory");
        activeAudioFactory = new JavaSoundAudioFactory();
        if (activeAudioFactory.init()) return;

        // Finally, if JavaSound fails, fall-back to a Null sound system
        log.debug("Try to initialise NullAudioFactory");
        activeAudioFactory = new NullAudioFactory();
        activeAudioFactory.init();
        // assumed to succeed.
    }
    
    /**
     * Method used to initialise the manager and make connections
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // OK to write to static variables as we only do so if not initialised
    @Override
    public synchronized void init() {
        if (!initialised) {
        
            // create Factory of appropriate type
            createFactory();
            
            // Create default Listener and save in map
            try {
                Audio s = createNewAudio("IAL$", "Default Audio Listener");
                register(s);
            } catch (AudioException ex) {
                log.error("Error creating Default Audio Listener: " + ex);
            }

            // Register a shutdown task to ensure clean exit
            InstanceManager.getDefault(jmri.ShutDownManager.class).register(audioShutDownTask);

            initialised = true;
            if (log.isDebugEnabled()) {
                log.debug("Initialised AudioFactory type: " + activeAudioFactory.getClass().getSimpleName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Synchronized method to ensure correct counter manipulation")
    public synchronized void deregister(Audio s) {
        // Decrement the relevant Audio object counter
        switch (s.getSubType()) {
            case (Audio.BUFFER): {
                buffers.remove(s);
                countBuffers--;
                log.debug("Remove buffer; count: {}", countBuffers);
                break;
            }
            case (Audio.SOURCE): {
                sources.remove(s);
                countSources--;
                log.debug("Remove source; count: {}", countSources);
                break;
            }
            case (Audio.LISTENER): {
                listeners.remove(s);
                countListeners--;
                log.debug("Remove listener; count: {}", countListeners);
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        super.deregister(s);
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "OK to write to static variables to record static library status")
    @Override
    public void cleanup() {
        // Shutdown AudioFactory and close the output device
        log.info("Shutting down active AudioFactory");
        InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(audioShutDownTask);
        if (activeAudioFactory != null) activeAudioFactory.cleanup();
        // Reset counters
        countBuffers = 0;
        countSources = 0;
        countListeners = 0;
        // Record that we're no longer initialised
        initialised = false;
    }

    @Override
    public void dispose() {
        buffers.clear();
        sources.clear();
        listeners.clear();
        super.dispose();
    }

    @Override
    public AudioFactory getActiveAudioFactory() {
        return activeAudioFactory;
    }

    /**
     * Return the current instance of this object.
     * <p>
     * If not existing, create a new instance.
     *
     * @return reference to currently active AudioManager
     * @deprecated since 4.17.3; use {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static DefaultAudioManager instance() {
        return InstanceManager.getDefault(DefaultAudioManager.class);
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultAudioManager.class);

}

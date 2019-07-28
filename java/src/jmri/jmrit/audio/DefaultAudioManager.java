package jmri.jmrit.audio;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jmri.Audio;
import jmri.AudioException;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
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

    /**
     * Reference to the currently active AudioFactory. 
     * Because of underlying (external to Java) implementation details,
     * JMRI only ever has one AudioFactory, so we make this static.
     */
    private static AudioFactory activeAudioFactory = null;

    private static boolean initialised = false;

    ShutDownTask audioShutDownTask;

    @Override
    public int getXMLOrder() {
        return jmri.Manager.AUDIO;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
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
                break;
            }
            case Audio.LISTENER: {
                if (countListeners >= MAX_LISTENERS) {
                    log.error("Maximum number of Listeners reached (" + countListeners + ") " + MAX_LISTENERS);
                    throw new AudioException("Maximum number of Listeners reached (" + countListeners + ") " + MAX_LISTENERS);
                }
                countListeners++;
                a = activeAudioFactory.createNewListener(systemName, userName);
                break;
            }
            case Audio.SOURCE: {
                if (countSources >= MAX_SOURCES) {
                    log.error("Maximum number of Sources reached (" + countSources + ") " + MAX_SOURCES);
                    throw new AudioException("Maximum number of Sources reached (" + countSources + ") " + MAX_SOURCES);
                }
                countSources++;
                a = activeAudioFactory.createNewSource(systemName, userName);
                break;
            }
            default:
                throw new IllegalArgumentException();
        }

        return a;
    }

    @Override
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

    /**
     * Method used to initialise the manager
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // OK to write to static variables as we only do so if not initialised
    @Override
    public synchronized void init() {
        if (!initialised) {
//            // First try to initialise LWJGL
//            activeAudioFactory = new LWJGLAudioFactory();
//            log.debug("Try to initialise LWJGLAudioFactory");
//
//            // If LWJGL fails, fall-back to JOAL
//            if (!activeAudioFactory.init()) {
            activeAudioFactory = new JoalAudioFactory();
            log.debug("Try to initialise JoalAudioFactory");

            // If JOAL fails, fall-back to JavaSound
            if (!activeAudioFactory.init()) {
                activeAudioFactory = new JavaSoundAudioFactory();
                log.debug("Try to initialise JavaSoundAudioFactory");

                // Finally, if JavaSound fails, fall-back to a Null sound system
                if (!activeAudioFactory.init()) {
                    activeAudioFactory = new NullAudioFactory();
                    log.debug("Try to initialise NullAudioFactory");
                    activeAudioFactory.init();
                }
            }
//            }

            // Create default Listener and save in map
            try {
                Audio s = createNewAudio("IAL$", "Default Audio Listener");
                register(s);
            } catch (AudioException ex) {
                log.error("Error creating Default Audio Listener: " + ex);
            }

            // Finally, create and register a shutdown task to ensure clean exit
            if (audioShutDownTask == null) {
                audioShutDownTask = new QuietShutDownTask("AudioFactory Shutdown") {
                    @Override
                    public boolean execute() {
                        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
                        return true;
                    }
                };
            }
            InstanceManager.getDefault(jmri.ShutDownManager.class).register(audioShutDownTask);

            initialised = true;
            if (log.isDebugEnabled()) {
                log.debug("Initialised AudioFactory type: " + activeAudioFactory.getClass().getSimpleName());
            }
        }
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Synchronized method to ensure correct counter manipulation")
    public synchronized void deregister(Audio s) {
        super.deregister(s);
        // Decrement the relevant Audio object counter
        switch (s.getSubType()) {
            case (Audio.BUFFER): {
                countBuffers--;
                break;
            }
            case (Audio.SOURCE): {
                countSources--;
                break;
            }
            case (Audio.LISTENER): {
                countListeners--;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void cleanup() {
        // Shutdown AudioFactory and close the output device
        log.info("Shutting down active AudioFactory");
        activeAudioFactory.cleanup();
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
     */
    public static DefaultAudioManager instance() {
        if (_instance == null) {
            _instance = new DefaultAudioManager();
        }
        return _instance;
    }

    private volatile static DefaultAudioManager _instance;

    private static final Logger log = LoggerFactory.getLogger(DefaultAudioManager.class);

}

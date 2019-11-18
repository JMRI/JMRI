package jmri.jmrit.audio;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.SortedSet;
import java.util.TreeSet;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the null audio system specific AudioFactory.
 *
 * It is a dummy factory which provides the necessary object generation but does
 * not produce any sound. This will normally only be used when running on a
 * system that has no sound-card installed.
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
public class NullAudioFactory extends AbstractAudioFactory {

    private static boolean initialised = false;

    private NullAudioListener activeAudioListener;

    @Override
    public boolean init() {
        if (initialised) {
            return true;
        }

        log.info("Initialised Null audio system - no sounds will be available.");

        super.init();
        setInit(true);
        return true;
    }

    private synchronized static void setInit(boolean newVal) {
        initialised = newVal;
    }

    @Override
    public String toString() {
        return "NullAudioFactory:"
                + " vendor - JMRI Community"
                + " version - " + jmri.Version.name(); // NOI18N
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "OK to write to static variables to record static library status")
    @Override
    public void cleanup() {
        // Stop the command thread
        super.cleanup();

        // Get the active AudioManager
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);

        // Retrieve list of AudioSource objects and remove the sources
        SortedSet<Audio> sources = new TreeSet<>(am.getNamedBeanSet(Audio.SOURCE));
        for (Audio source: sources) {
            if (log.isDebugEnabled()) {
                log.debug("Removing NullAudioSource: {}", source.getSystemName());
            }
            // Cast to NullAudioSource and cleanup
            ((NullAudioSource) source).cleanup();
        }

        // Now, retrieve list of AudioBuffer objects and remove the buffers
        SortedSet<Audio> buffers = new TreeSet<>(am.getNamedBeanSet(Audio.BUFFER));
        for (Audio buffer : buffers) {
            if (log.isDebugEnabled()) {
                log.debug("Removing NullAudioBuffer: {}", buffer.getSystemName());
            }
            // Cast to NullAudioBuffer and cleanup
            ((NullAudioBuffer) buffer).cleanup();
        }

        // Lastly, retrieve list of AudioListener objects and remove listener.
        SortedSet<Audio> listeners = new TreeSet<>(am.getNamedBeanSet(Audio.LISTENER));
        for (Audio listener : listeners) {
            if (log.isDebugEnabled()) {
                log.debug("Removing NullAudioListener: {}", listener.getSystemName());
            }
            // Cast to NullAudioListener and cleanup
            ((NullAudioListener) listener).cleanup();
        }

        // Finally, shutdown NullAudio and close the output device
        log.debug("Shutting down NullAudio");
        // Do nothing
        initialised = false;
    }

    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    public AudioBuffer createNewBuffer(String systemName, String userName) {
        return new NullAudioBuffer(systemName, userName);
    }

    @Override
    public AudioListener createNewListener(String systemName, String userName) {
        activeAudioListener = new NullAudioListener(systemName, userName);
        return activeAudioListener;
    }

    @Override
    public AudioListener getActiveAudioListener() {
        return activeAudioListener;
    }

    @Override
    public AudioSource createNewSource(String systemName, String userName) {
        return new NullAudioSource(systemName, userName);
    }

    private static final Logger log = LoggerFactory.getLogger(NullAudioFactory.class);

}

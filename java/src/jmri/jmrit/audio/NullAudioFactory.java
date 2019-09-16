package jmri.jmrit.audio;

import java.util.List;
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

    @Override
    public void cleanup() {
        // Stop the command thread
        super.cleanup();

        // Get the active AudioManager
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);

        // Retrieve list of Audio Objects and remove the sources
        for (Audio audio : am.getNamedBeanSet()) {
            if (audio.getSubType() == Audio.SOURCE) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing NullAudioSource: " + audio.getSystemName());
                }
                // Cast to NullAudioSource and cleanup
                ((NullAudioSource) audio).cleanup();
            }
        }

        // Now, re-retrieve list of Audio objects and remove the buffers
        for (Audio audio : am.getNamedBeanSet()) {
            if (audio.getSubType() == Audio.BUFFER) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing NullAudioBuffer: " + audio.getSystemName());
                }
                // Cast to NullAudioBuffer and cleanup
                ((NullAudioBuffer) audio).cleanup();
            }
        }

        // Lastly, re-retrieve list and remove listener.
        for (Audio audio : am.getNamedBeanSet()) {
            if (audio.getSubType() == Audio.LISTENER) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing NullAudioListener: " + audio.getSystemName());
                }
                // Cast to NullAudioListener and cleanup
                ((NullAudioListener) audio).cleanup();
            }
        }

        // Finally, shutdown NullAudio and close the output device
        log.debug("Shutting down NullAudio");
        // Do nothing
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

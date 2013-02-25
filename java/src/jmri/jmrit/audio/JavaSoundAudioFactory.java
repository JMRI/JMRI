// JavaSoundAudioFactory.java

package jmri.jmrit.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;

/**
 * This is the JavaSound audio system specific AudioFactory.
 * <p>
 * The JavaSound sound system supports, where available, 2-channel stereo.
 * <p>
 * The implemented Audio objects provide an approximation of a 3D positionable
 * audio model through the use of calculated panning and gain based on the 3D
 * position of the individual sound sources.
 * <p>
 * This factory initialises JavaSound, provides new JavaSound-specific Audio
 * objects and deals with clean-up operations.
 * <p>
 * For more information about the JavaSound API, visit
 * <a href="http://java.sun.com/products/java-media/sound/">http://java.sun.com/products/java-media/sound/</a>
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public class JavaSoundAudioFactory extends AbstractAudioFactory {

    private static boolean _initialised = false;

    private volatile static Mixer _mixer;

    private JavaSoundAudioListener _activeAudioListener;

    @Override
    public boolean init() {
        if(_initialised) {
            return true;
        }

        // Initialise JavaSound
        if (_mixer == null) {
            // Iterate through possible mixers until we find the one we require
            for ( Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                if (mixerInfo.getName().equals("Java Sound Audio Engine")) {
                    _mixer = AudioSystem.getMixer(mixerInfo);
                    break;
                }
            }
        }
        // Check to see if a suitable mixer has been found
        if (_mixer == null) {
            if (log.isDebugEnabled()) log.debug("No JavaSound audio system found.");
            return false;
        } else {
            if (log.isInfoEnabled())
                log.info("Initialised JavaSound:"
                          + " vendor - " + _mixer.getMixerInfo().getVendor()
                          + " version - " + _mixer.getMixerInfo().getVersion());
        }

        super.init();
        _initialised = true;
        return true;
    }

    @Override
    public String toString() {
        return "JavaSoundAudioFactory:"
                + " vendor - " + _mixer.getMixerInfo().getVendor()
                + " version - " + _mixer.getMixerInfo().getVersion();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // OK to write to static variable _mixer as we are cleaning up
    public void cleanup() {
        // Stop the command thread
        super.cleanup();

        // Get the active AudioManager
        AudioManager am = InstanceManager.audioManagerInstance();

        // Retrieve list of Audio Objects and remove the sources
        List<String> audios = am.getSystemNameList();
        for (String audioName: audios) {
            Audio audio = am.getAudio(audioName);
            if (audio.getSubType()==Audio.SOURCE) {
                if (log.isDebugEnabled()) log.debug("Removing JavaSoundAudioSource: "+ audioName);
                // Cast to JavaSoundAudioSource and cleanup
                ((JavaSoundAudioSource) audio).cleanUp();
            }
        }

        // Now, re-retrieve list of Audio objects and remove the buffers
        audios = am.getSystemNameList();
        for (String audioName: audios) {
            Audio audio = am.getAudio(audioName);
            if (audio.getSubType()==Audio.BUFFER) {
                if (log.isDebugEnabled()) log.debug("Removing JavaSoundAudioBuffer: "+ audioName);
                // Cast to JavaSoundAudioBuffer and cleanup
                ((JavaSoundAudioBuffer) audio).cleanUp();
            }
        }

        // Lastly, re-retrieve list and remove listener.
        audios = am.getSystemNameList();
        for (String audioName: audios) {
            Audio audio = am.getAudio(audioName);
            if (audio.getSubType()==Audio.LISTENER) {
                if (log.isDebugEnabled()) log.debug("Removing JavaSoundAudioListener: "+ audioName);
                // Cast to JavaSoundAudioListener and cleanup
                ((JavaSoundAudioListener) audio).cleanUp();
            }
        }

        // Finally, shutdown JavaSound and close the output device
        log.debug("Shutting down JavaSound");
        _mixer = null;
    }

    @Override
    public AudioBuffer createNewBuffer(String systemName, String userName) {
        return new JavaSoundAudioBuffer(systemName, userName);
    }

    @Override
    public AudioListener createNewListener(String systemName, String userName) {
        _activeAudioListener = new JavaSoundAudioListener(systemName, userName);
        return _activeAudioListener;
    }

    @Override
    public AudioListener getActiveAudioListener() {
        return _activeAudioListener;
    }

    @Override
    public AudioSource createNewSource(String systemName, String userName) {
        return new JavaSoundAudioSource(systemName, userName);
    }

    /**
     * Return reference to the current JavaSound mixer object
     *
     * @return current JavaSound mixer
     */
    public static synchronized Mixer getMixer() {
        return _mixer;
    }

    private static final Logger log = LoggerFactory.getLogger(JavaSoundAudioFactory.class.getName());

}

/* $(#)JavaSoundAudioFactory.java */

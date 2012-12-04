// NullAudioFactory.java

package jmri.jmrit.audio;

import java.util.List;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;

/**
 * This is the null audio system specific AudioFactory.
 * 
 * It is a dummy factory which provides the necessary object generation but
 * does not produce any sound. This will normally only be used when running on
 * a system that has no sound-card installed.
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
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public class NullAudioFactory extends AbstractAudioFactory {

    private static boolean _initialised = false;

    private NullAudioListener activeAudioListener;

    @Override
    public boolean init() {
        if(_initialised) {
            return true;
        }

        log.warn("Initialised Null audio system - no sounds will be available.");

        super.init();
        _initialised = true;
        return true;
    }

    @Override
    public String toString() {
        return "NullAudioFactory:"
                + " vendor - JMRI Community"
                + " version - " + jmri.Version.name();
    }

    @Override
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
                if (log.isDebugEnabled()) log.debug("Removing NullAudioSource: "+ audioName);
                // Cast to NullAudioSource and cleanup
                ((NullAudioSource) audio).cleanUp();
            }
        }

        // Now, re-retrieve list of Audio objects and remove the buffers
        audios = am.getSystemNameList();
        for (String audioName: audios) {
            Audio audio = am.getAudio(audioName);
            if (audio.getSubType()==Audio.BUFFER) {
                if (log.isDebugEnabled()) log.debug("Removing NullAudioBuffer: "+ audioName);
                // Cast to NullAudioBuffer and cleanup
                ((NullAudioBuffer) audio).cleanUp();
            }
        }

        // Lastly, re-retrieve list and remove listener.
        audios = am.getSystemNameList();
        for (String audioName: audios) {
            Audio audio = am.getAudio(audioName);
            if (audio.getSubType()==Audio.LISTENER) {
                if (log.isDebugEnabled()) log.debug("Removing NullAudioListener: "+ audioName);
                // Cast to NullAudioListener and cleanup
                ((NullAudioListener) audio).cleanUp();
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
        activeAudioListener =  new NullAudioListener(systemName, userName);
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

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NullAudioFactory.class.getName());

}

/* $(#)NullAudioFactory.java */
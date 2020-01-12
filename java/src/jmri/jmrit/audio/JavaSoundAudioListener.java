package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaSound implementation of the Audio Listener sub-class.
 * <p>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 * <p>
 * For more information about the JavaSound API, visit
 * <a href="http://java.sun.com/products/java-media/sound/">http://java.sun.com/products/java-media/sound/</a>
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
public class JavaSoundAudioListener extends AbstractAudioListener {

    /**
     * Constructor for new JavaSoundAudioListener with system name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     */
    public JavaSoundAudioListener(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) {
            log.debug("New JavaSoundAudioListener: " + systemName);
        }
    }

    /**
     * Constructor for new JavaSoundAudioListener with system name and user name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     * @param userName   AudioListener object user name
     */
    public JavaSoundAudioListener(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) {
            log.debug("New JavaSoundAudioListener: " + userName + " (" + systemName + ")");
        }
    }

    @Override
    protected void changePosition(Vector3f pos) {
        recalculateSources();
    }

    @Override
    public void setGain(float gain) {
        super.setGain(gain);
        recalculateSources();
    }

    /**
     * Private method to loop through all sources and recalculate gain & pan
     */
    private void recalculateSources() {
        // Loop through each AudioSource and recalculate their gain & pan
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);
        for (Audio audio : am.getNamedBeanSet()) {
            if (audio.getSubType() == Audio.SOURCE
                    && audio instanceof JavaSoundAudioSource) {
                ((JavaSoundAudioSource) audio).calculateGain();
                ((JavaSoundAudioSource) audio).calculatePan();
                if (log.isDebugEnabled()) {
                    log.debug("Recalculating gain & pan for JavaSoundAudioSource " + audio.getSystemName());
                }
            }
        }
    }

    @Override
    protected void cleanup() {
        // no clean-up needed for Listener
        if (log.isDebugEnabled()) {
            log.debug("Cleanup JavaSoundAudioListener (" + this.getSystemName() + ")");
        }
        this.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(JavaSoundAudioListener.class);

}

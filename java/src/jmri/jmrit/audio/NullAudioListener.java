package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null implementation of the Audio Listener sub-class.
 * <p>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
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
public class NullAudioListener extends AbstractAudioListener {

    /**
     * Constructor for new NullAudioListener with system name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     */
    public NullAudioListener(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) {
            log.debug("New NullAudioListener: " + systemName);
        }
    }

    /**
     * Constructor for new NullAudioListener with system name and user name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     * @param userName   AudioListener object user name
     */
    public NullAudioListener(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) {
            log.debug("New NullAudioListener: " + userName + " (" + systemName + ")");
        }
    }

    @Override
    protected void changePosition(Vector3f pos) {
        // Do nothing
    }

    @Override
    protected void cleanup() {
        if (log.isDebugEnabled()) {
            log.debug("Cleanup NullAudioBuffer (" + this.getSystemName() + ")");
        }
        this.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(NullAudioListener.class);

}

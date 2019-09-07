package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null audio system implementation of the Audio Source sub-class.
 * <p>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
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
public class NullAudioSource extends AbstractAudioSource {

    /**
     * True if we've been initialised.
     */
    private boolean initialised = false;

    /**
     * Constructor for new NullAudioSource with system name.
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public NullAudioSource(String systemName) {
       this(systemName,null);
    }

    /**
     * Constructor for new NullAudioSource with system name and user name.
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName   AudioSource object user name
     */
    public NullAudioSource(String systemName, String userName) {
        super(systemName, userName);
        log.debug("New NullAudioSource: {} ({})", userName, systemName);
        initialised = init();
    }

    @Override
    boolean bindAudioBuffer(AudioBuffer audioBuffer) {
        // Don't actually need to do anything specific here
        log.debug("Bind NullAudioSource ({}) to NullAudioBuffer ({})", this.getSystemName(), audioBuffer.getSystemName());
        return true;
    }

    /**
     * Initialise this AudioSource.
     *
     * @return True if initialised
     */
    private boolean init() {
        return true;
    }

    @Override
    protected void changePosition(Vector3f pos) {
        // Do nothing
    }

    @Override
    protected void doPlay() {
        log.debug("Play NullAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doRewind();
            doResume();
        }
    }

    @Override
    protected void doStop() {
        log.debug("Stop NullAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doRewind();
        }
    }

    @Override
    protected void doPause() {
        log.debug("Pause NullAudioSource ({})", this.getSystemName());
        this.setState(STATE_STOPPED);
    }

    @Override
    protected void doResume() {
        log.debug("Resume NullAudioSource ({})", this.getSystemName());
        this.setState(STATE_PLAYING);
    }

    @Override
    protected void doRewind() {
        log.debug("Rewind NullAudioSource ({})", this.getSystemName());
        this.setState(STATE_STOPPED);
    }

    @Override
    protected void doFadeIn() {
        log.debug("Fade-in JoalAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doPlay();
        }
    }

    @Override
    protected void doFadeOut() {
        log.debug("Fade-out JoalAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doStop();
        }
    }

    @Override
    protected void cleanup() {
        log.debug("Cleanup NullAudioSource ({})", this.getSystemName());
        this.dispose();
    }

    @Override
    protected void calculateGain() {
        // do nothing
    }

    private static final Logger log = LoggerFactory.getLogger(NullAudioSource.class);

}

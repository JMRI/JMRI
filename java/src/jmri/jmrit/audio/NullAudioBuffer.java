package jmri.jmrit.audio;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null implementation of the Audio Buffer sub-class.
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
 * @author Matthew Harris copyright (c) 2009, 2011
 */
public class NullAudioBuffer extends AbstractAudioBuffer {

    /**
     * Constructor for new NullAudioBuffer with system name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     */
    public NullAudioBuffer(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) {
            log.debug("New NullAudioBuffer: " + systemName);
        }
    }

    /**
     * Constructor for new NullAudioBuffer with system name and user name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     * @param userName   AudioBuffer object user name
     */
    public NullAudioBuffer(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) {
            log.debug("New NullAudioBuffer: " + userName + " (" + systemName + ")");
        }
    }

    @Override
    public String toString() {
        if (this.getState() != STATE_LOADED) {
            return "Empty buffer";
        } else {
            return this.getURL() + " (" + parseFormat() + ", " + "?? Hz)";
        }
    }

    @Override
    protected boolean loadBuffer(InputStream stream) {
        // No need to do this for the NullAudioBuffer - it's always successful ;-)
        return true;
    }

    @Override
    protected boolean loadBuffer() {
        // No need to do this for the NullAudioBuffer - it's always successful ;-)
        return true;
    }

    @Override
    protected void generateLoopBuffers(int which) {
        // No need to do anything for the NullAudioBuffer
    }

    @Override
    protected boolean generateStreamingBuffers() {
        // No need to do this for the NullAudioBuffer - it's always successful ;-)
        return true;
    }

    @Override
    protected void removeStreamingBuffers() {
        // No need to do anything for the NullAudioBuffer
    }

    @Override
    public int getFormat() {
        return FORMAT_UNKNOWN;
    }

    @Override
    public long getLength() {
        // Nothing stored for the NullAudioBuffer - always zero
        return 0;
    }

    @Override
    public int getFrequency() {
        // Nothing stored for the NullAudioBuffer - always zero
        return 0;
    }

    /**
     * Internal method to return a string representation of the audio format
     *
     * @return string representation
     */
    private String parseFormat() {
        return "unknown format";
    }

    @Override
    protected void cleanup() {
        if (log.isDebugEnabled()) {
            log.debug("Cleanup NullAudioBuffer (" + this.getSystemName() + ")");
        }
        this.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(NullAudioBuffer.class);

}

// AbstractAudioBuffer.java

package jmri.jmrit.audio;

import jmri.implementation.AbstractAudio;
import jmri.util.FileUtil;

/**
 * Base implementation of the AudioBuffer class.
 * <p>
 * Specific implementations will extend this base class.
 * <p>
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
 * @version $Revision: 1.3 $
 */
public abstract class AbstractAudioBuffer extends AbstractAudio implements AudioBuffer {

    /**
     * Holds the location of the sound sample used in this buffer
     */
    private String _url;

    /**
     * Start loop point for this buffer represented as a number of samples
     */
    private long _startLoopPoint;

    /**
     * End loop point for this buffer represented as a number of samples
     */
    private long _endLoopPoint;

    /**
     * Abstract constructor for new AudioBuffer with system name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     */
    public AbstractAudioBuffer(String systemName) {
        super(systemName);
        this.setState(STATE_EMPTY);
    }

    /**
     * Abstract constructor for new AudioBuffer with system name and user name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     * @param userName AudioBuffer object user name
     */
    public AbstractAudioBuffer(String systemName, String userName) {
        super(systemName, userName);
        this.setState(STATE_EMPTY);
    }

    public char getSubType() {
        return BUFFER;
    }

    public String getURL() {
        return this._url;
    }

    public void setURL(String url) {
        this._url = FileUtil.getPortableFilename(url);

        // Run the loadBuffer method on the main AWT thread to avoid any
        // potential issues with interrupted exceptions if run on the audio
        // command thread
        loadBuffer();
        if (log.isDebugEnabled())
            log.debug("Set url of Buffer " + this.getSystemName() + " to " + url);
    }

    /**
     * Method used to load the actual sound data into the buffer
     * @return True if successful; False if not
     */
    abstract protected boolean loadBuffer();

    public void setStartLoopPoint(long startLoopPoint) {
        this._startLoopPoint = startLoopPoint;
        if (log.isDebugEnabled())
            log.debug("Set start loop point of Buffer " + this.getSystemName() + " to " + startLoopPoint);
    }

    public long getStartLoopPoint() {
        return this._startLoopPoint;
    }

    public void setEndLoopPoint(long endLoopPoint) {
        this._endLoopPoint = endLoopPoint;
        if (log.isDebugEnabled())
            log.debug("Set end loop point of Buffer " + this.getSystemName() + " to " + endLoopPoint);
    }

    public long getEndLoopPoint() {
        return this._endLoopPoint;
    }

    public void stateChanged(int oldState) {
        // Move along... nothing to see here...
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractAudioBuffer.class.getName());
}

/* $(#)AbstractAudioBuffer.java */
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
 * @version $Revision: 1.4 $
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
     * Flag to determine if this buffer is to be streamed from file
     */
    private boolean _streamed = false;

    /**
     * Flag to determine if streaming has been forced
     */
    private boolean _streamedForced = false;

//    /**
//     *
//     */
//    private WaveFileReader _waveFile;

    /**
     * Identifier of start loop point
     */
    protected static final int LOOP_POINT_START = 0x01;

    /**
     * Identifier of end loop point
     */
    protected static final int LOOP_POINT_END   = 0x02;

    /**
     * Identifier of both loop points
     */
    protected static final int LOOP_POINT_BOTH  = 0x03;

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
        this.setStartLoopPoint(startLoopPoint, true);
    }

    /**
     * Internal method used to set the start loop point of this buffer
     * with optional generation of loop buffers
     * 
     * @param startLoopPoint position of start loop point in samples
     * @param generateLoopBuffers True if loop buffers to be generated
     */
    protected void setStartLoopPoint(long startLoopPoint, boolean generateLoopBuffers) {
        this._startLoopPoint = startLoopPoint;
        if (generateLoopBuffers) generateLoopBuffers(LOOP_POINT_START);
        if (log.isDebugEnabled())
            log.debug("Set start loop point of Buffer " + this.getSystemName() + " to " + startLoopPoint);
    }

    public long getStartLoopPoint() {
        return this._startLoopPoint;
    }

    public void setEndLoopPoint(long endLoopPoint) {
        this.setEndLoopPoint(endLoopPoint, true);
    }

    /**
     * Internal method used to set the end loop point of this buffer
     * with optional generation of loop buffers
     * 
     * @param endLoopPoint position of end loop point in samples
     * @param generateLoopBuffers True if loop buffers to be generated
     */
    protected void setEndLoopPoint(long endLoopPoint, boolean generateLoopBuffers) {
        this._endLoopPoint = endLoopPoint;
        if (generateLoopBuffers) generateLoopBuffers(LOOP_POINT_END);
        if (log.isDebugEnabled())
            log.debug("Set end loop point of Buffer " + this.getSystemName() + " to " + endLoopPoint);
    }

    public long getEndLoopPoint() {
        return this._endLoopPoint;
    }

    public void setStreamed(boolean streamed) {
        if (streamed) {
            log.warn("Streaming not yet supported!!");
            streamed = !streamed;
        }
        boolean changed = this._streamed!=streamed;
        this._streamed = this._streamedForced==true?true:streamed;
        if (log.isDebugEnabled())
            log.debug("Set streamed property of Buffer " + this.getSystemName() + " to " + streamed + "; changed = " + changed);
        if (streamed && changed) {
            generateStreamingBuffers();
        } else if (!streamed && changed) {
            removeStreamingBuffers();
        }
    }

    public boolean isStreamed() {
        return this._streamed;
    }

    /**
     * Protected method used internally to modify the forced streaming flag
     * @param streamedForced True if required; False if not
     */
    protected void setStreamedForced(boolean streamedForced) {
        boolean changed = this._streamedForced==false && streamedForced==true;
        this._streamedForced = streamedForced;
        if (log.isDebugEnabled())
            log.debug("Set streamedForced property of Buffer " + this.getSystemName() + " to " + streamedForced + "; changed = " + changed);
        this.setStreamed(streamedForced==true?true:this._streamed);
        if (changed) {
            this.generateLoopBuffers(LOOP_POINT_BOTH);
        }
    }

    public boolean isStreamedForced() {
        return this._streamedForced;
    }

    /**
     * Method used to generate any necessary loop buffers.
     * @param which the loop buffer to generate:
     *  <br>{@link #LOOP_POINT_START} for the start loop buffer
     *  <br>{@link #LOOP_POINT_END} for the end loop buffer
     *  <br>{@link #LOOP_POINT_BOTH} for both loop buffers
     */
    abstract protected void generateLoopBuffers(int which);

    /**
     * Internal method used to generate buffers for streaming
     * @return True if successful; False if not
     */
    abstract protected boolean generateStreamingBuffers();

    /**
     * Internal method used to remove streaming buffers
     */
    abstract protected void removeStreamingBuffers();

    public void stateChanged(int oldState) {
        // Move along... nothing to see here...
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractAudioBuffer.class.getName());
}

/* $(#)AbstractAudioBuffer.java */
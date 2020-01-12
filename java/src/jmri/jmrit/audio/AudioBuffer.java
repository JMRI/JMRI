package jmri.jmrit.audio;

import java.io.InputStream;
import java.nio.ByteBuffer;
import jmri.Audio;

/**
 * Represent an AudioBuffer, a place to store or control sound information.
 * <p>
 * The AbstractAudio class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in the jmrix package, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each AudioBuffer object has a two names. The "user" name is entirely free
 * form, and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (for example LocoNet or NCE) and address within that system.
 * <br>
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
public interface AudioBuffer extends Audio {

    /**
     * Definition of unknown audio format
     */
    public static final int FORMAT_UNKNOWN = 0x00;
    /**
     * Definition of 8-bit mono audio format
     */
    public static final int FORMAT_8BIT_MONO = 0x11;
    /**
     * Definition of 16-bit mono audio format
     */
    public static final int FORMAT_16BIT_MONO = 0x12;
    /**
     * Definition of 8-bit stereo audio format
     */
    public static final int FORMAT_8BIT_STEREO = 0x21;
    /**
     * Definition of 16-bit stereo audio format
     */
    public static final int FORMAT_16BIT_STEREO = 0x22;

    /**
     * Definition of 8-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_QUAD = 0x41;
    /**
     * Definition of 16-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_QUAD = 0x42;
    /**
     * Definition of 8-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_5DOT1 = 0x61;
    /**
     * Definition of 16-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_5DOT1 = 0x62;
    /**
     * Definition of 8-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_6DOT1 = 0x71;
    /**
     * Definition of 16-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_6DOT1 = 0x72;
    /**
     * Definition of 8-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_7DOT1 = 0x81;
    /**
     * Definition of 16-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_7DOT1 = 0x82;

    /**
     * Return the url of the sound sample
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return url
     */
    public String getURL();

    /**
     * Sets the url of the sound sample
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @param pUrl URL for location containing sound sample data
     */
    public void setURL(String pUrl);

    /**
     * Sets the input stream of the sound sample
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @param stream InputStream containing sound sample data
     */
    public void setInputStream(InputStream stream);

    /**
     * Retrieves the format of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return constant representing format
     */
    public int getFormat();

    /**
     * Retrieves the length of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return length of sound sample in frames
     * @see #getFrameSize()
     */
    public long getLength();

    /**
     * Retrieves the frequency of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return frequency of sound sample in Hz
     */
    public int getFrequency();

    /**
     * Retrieves the length of a sound sample frame stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return length of sound sample frame in bytes
     */
    public int getFrameSize();

    /**
     * Sets the start loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @param startLoopPoint position of start loop point in samples
     */
    public void setStartLoopPoint(long startLoopPoint);

    /**
     * Retrieves the start loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return position of start loop point in samples
     */
    public long getStartLoopPoint();

    /**
     * Sets the end loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @param endLoopPoint position of end loop point in samples
     */
    public void setEndLoopPoint(long endLoopPoint);

    /**
     * Retrieves the end loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return position of end loop point in samples
     */
    public long getEndLoopPoint();

    /**
     * Sets that this buffer is to be streamed as opposed to loaded in full. Can
     * only be turned off when {@link #isStreamedForced() isStreamedForced} is
     * not set.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @param streamed buffer is streamed from file or loaded in full
     * @see #isStreamedForced()
     */
    public void setStreamed(boolean streamed);

    /**
     * Retrieves the current streaming setting of this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return current streaming setting
     */
    public boolean isStreamed();

    /**
     * Determines if this buffer can be loaded in full or if it must be streamed
     * from the file. Forced streaming is usually restricted to larger sound
     * samples that are otherwise too large to fit directly into memory.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return True if buffer must be streamed; False it can be loaded in full
     */
    public boolean isStreamedForced();

    public boolean loadBuffer(ByteBuffer b, int format, int frequency);
}

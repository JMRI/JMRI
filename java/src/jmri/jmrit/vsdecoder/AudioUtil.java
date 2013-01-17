package jmri.jmrit.vsdecoder;

/**
 * Utility class for doing "VSD-special" things with
 * the JMRI Audio classes.
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
 * @author Mark Underwood  copyright (c) 2009, 2013
 * @version $Revision$
 */

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import jmri.util.FileUtil;
import net.java.games.joal.AL;
import net.java.games.joal.ALException;
import net.java.games.joal.util.ALut;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import jmri.Audio;
import jmri.AudioException;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.JoalAudioBuffer;

public class AudioUtil {
        //------------------------
    // New Methods to allow creating a set of (sub)Buffers built off a single
    // input stream.  Should probably look closely at this and see if it could
    // be pushed up to AbstractAudioBuffer.  Or better yet, moved OVER to AbstractAudioFactory.


    /** Split the WAV data from an InputStream into a series of AudioByteBuffers of size (approximately) 
     *  between max_time and min_time, split on zero crossings.
     *
     *  The method will try to create Buffers of max_time length (plus whatever's needed to hit the next
     *  zero crossing), until there's not enough bytes left in the InputStream.  The last Buffer will be
     *  of at least min_time length, else the remaining data (less than min_time length) will be discarded.
     *  Buffers will be cut on zero-crossing boundaries.
     *
     *  This method is meant to be used in tandem with getAudioBufferList(), at least for now.
     *
     *  @param stream : Input Stream, assumed to be WAV-format data
     *  @param max_time : maximum (target) length of each split Buffer, in milliseconds
     *  @param min_time : minimum length of buffer to return, in milliseconds.  Any buffer that would be 
     *                    smaller than this will be discarded.
     * 
     *  @return (List<AudioByteBuffer>) List of AudioByteBuffers containing the (split-up) data from stream
     */
    static private List<AudioByteBuffer> splitInputStream(InputStream stream, int max_time, int min_time) {
	List<AudioByteBuffer> rlist = new ArrayList<AudioByteBuffer>();
	int[] format = new int[1];
	ByteBuffer[] data = new ByteBuffer[1];
	int[] size = new int[1];
	int[] freq = new int[1];
	int[] loop = new int[1];

	// Pull the WAV data into the "data" buffer.
        try {
            ALut.alutLoadWAVFile(stream, format, data, size, freq, loop);
        }
        catch (ALException e) {
            log.warn("Error loading JoalAudioBuffer: " + e.getMessage());
            return null;
        }
	
	// OK, for now, we're only going to support 8-bit and 16-bit Mono data.
	// I'll have to figure out later how to extend this to multiple data formats.
	if ((format[0] != AL.AL_FORMAT_MONO8) && (format[0] != AL.AL_FORMAT_MONO16)) {
	    log.warn("Invalid Format for splitting! Failing out." + parseFormat(format[0]));
	    return(null);
	}

	while (data[0].remaining() > 0) {
	    log.debug("while loop. Source: "+ data[0]);
	    AudioByteBuffer ab = new AudioByteBuffer(getSubBuffer(data[0], max_time, min_time, format[0], freq[0]), format[0], freq[0], loop[0]);
	    // getSubBuffer returning null isn't necessarily an error.  It could mean there weren't enough bytes left, so we truncated.
	    // In this case, we wil have already gotten (via get()) the remaining bytes from data[0], so we should exit the while loop
	    // normally.
	    if (ab.data != null) {
		ab.data.rewind();
		rlist.add(ab);
	    }
	}
	return(rlist);
    }

    /** Take a list of AudioByteBuffers and provide a 1:1 corresponding List of AudioBuffers
     *
     *  @param prefix : prefix to use when generating AudioBuffer system names.
     *  @param blist  : list of AudioByteBuffers to convert.
     *
     *  @return (List<AudioBuffer>) List of AudioBuffers
     */
    static public List<AudioBuffer> getAudioBufferList(String prefix, List<AudioByteBuffer> blist) {
	// Sanity check the prefix, since if it's wrong we'll get a casting error below.
	if (prefix.charAt(2) != Audio.BUFFER) {
	    log.warn("Not a Buffer request! " + prefix);
	    return(null);
	}

	List<AudioBuffer> rlist = new ArrayList<AudioBuffer>();
	
	int i=0; // Index used for the sub-buffer system names
	for (AudioByteBuffer b : blist) {
		try {
		    AudioBuffer buf = (AudioBuffer) jmri.InstanceManager.audioManagerInstance().provideAudio(prefix + "_sbuf" + i);
		    i++;
		    if (buf == null) { log.debug("provideAudio returned null!"); return(null); } // might be redundant with the try/catch.
		    
		    buf.loadBuffer(b.data, b.format, b.frequency);
		    /*
		    al.alBufferData(buf.getDataStorageBuffer()[0], format[0], b, b.limit(), freq[0]);
		    buf.setStartLoopPoint(0, false);
		    buf.setEndLoopPoint(b.limit(), false);
		    buf.generateLoopBuffers(LOOP_POINT_BOTH);
		    
		    buf.setState(STATE_LOADED);
		    */
		    if (log.isDebugEnabled()) {
			log.debug("Loaded buffer: " + buf.getSystemName());
			log.debug(" from file: " + buf.getURL());
			log.debug(" format: " + b.format + ", " + b.frequency + " Hz");
			log.debug(" length: " + b.data.limit());
		    }
		    
		    rlist.add(buf);
		} catch (AudioException e) {
		    log.warn("Error on provideAudio! " + e.toString());
		    return(null);
		}

	}
	return(rlist);
    }

    public static List<AudioBuffer> getAudioBufferList(String prefix, InputStream stream, int max_time, int min_time) {
	List<AudioBuffer> rlist = null;
	List<AudioByteBuffer> blist = splitInputStream(stream, max_time, min_time);
	if (blist != null) {
	    rlist = getAudioBufferList(prefix, blist);
	}
	return(rlist);
    }

    // This is here only because the AbstractAudioBuffer.getFrameSize() doesn't look
    // at the AL versions of the format strings.  And because it must be static.
    private static int frameSize(int format) {
	int frameSize = 1;
	switch(format) {
	case AudioBuffer.FORMAT_16BIT_7DOT1:
	    frameSize = 16; break;
	case AudioBuffer.FORMAT_8BIT_7DOT1:
	    frameSize = 8; break;
	case AudioBuffer.FORMAT_16BIT_6DOT1:
	    frameSize = 14; break;
	case AudioBuffer.FORMAT_8BIT_6DOT1:
	    frameSize = 7; break;
	case AudioBuffer.FORMAT_16BIT_5DOT1:
	    frameSize = 12; break;
	case AudioBuffer.FORMAT_8BIT_5DOT1:
	    frameSize = 6; break;
	case AudioBuffer.FORMAT_16BIT_QUAD:
	    frameSize = 8; break;
	case AudioBuffer.FORMAT_8BIT_QUAD:
	case AudioBuffer.FORMAT_16BIT_STEREO:
	    frameSize = 4; break;
	case AL.AL_FORMAT_MONO16:
	case AL.AL_FORMAT_STEREO8:
	    frameSize = 2; break;
	case AL.AL_FORMAT_MONO8:
	default:
	    // Note this will be wrong for all the modes we don't support.
	    frameSize = 1;
	}
	return(frameSize);
    }

    private static String parseFormat(int fmt) {
        switch (fmt) {
	case AL.AL_FORMAT_MONO8:
	    return "8-bit mono";
	case AL.AL_FORMAT_MONO16:
	    return "16-bit mono";
	case AL.AL_FORMAT_STEREO8:
	    return "8-bit stereo";
	case AL.AL_FORMAT_STEREO16:
	    return "16-bit stereo";
	default:
	    return ("Something Multichannel: val=" + fmt);
        }
    }

    /** Calculates the number of bytes offset that corresponds to the
     *  given time interval, with the given data format and sample
     *  frequency.
     *
     *  bytes = frame_size * time_ms * sample_frequency / 1000
     *
     *  @param fmt : audio data format
     *  @param freq : sample frequency in Hz
     *  @param time_ms : time interval in milliseconds
     *
     *  @return (int) number of bytes.
     */
    private static int calcTimeIndex(int fmt, int freq, int time_ms) {
	// freq == samples per second.  time_us = microseconds to calculate.
	// samples = time_us * freq / 1e3.
	// This will be approximate due to integer rounding.
	int rv = frameSize(fmt) *((time_ms * freq) / 1000);
	log.debug("calcTimeIndex: freq = " + freq + " time_us = " + time_ms + " rv = " + rv);
	return(rv);
    }

    /** Looks at the (last) three samples in buf (with sample size defined by @format)
     * and determines whether they represent a positive-going zero crossing event.
     *
     * Works only for AL.AL_FORMAT_MONO8 or AL.AL_FORMAT_MONO16. Returns false otherwise. 
     *
     * @param buf : (minimum) 3-sample buffer of WAV data
     * @param len : size of buf.  Minimum 3 bytes for 8-bit, 6 bytes for 16-bit mono data.
     * @param format : AL.format identifier.
     * @param order : ByteOrder of data in buf
     *
     * @return true if a zero crossing is detected.
     */
    private static Boolean isZeroCross(byte[] buf, int len, int format, ByteOrder order) {
	if (format == AL.AL_FORMAT_MONO8) {
	    if (len < 3)
		return(false);
	    else
		return((buf[len-3] < 128) && (buf[len-2] < 128) && (buf[len-1] >= 128));
	} else if (format == AL.AL_FORMAT_MONO16) {
	    if (len < 6) return(false);
	    short[] sbuf = new short[len/2];
	    // Below assumes little-endian
	    ByteBuffer bb = ByteBuffer.wrap(buf);
	    bb.order(order);
	    //bb.reset();
	    sbuf[0] = bb.getShort();
	    sbuf[1] = bb.getShort();
	    sbuf[2] = bb.getShort();
	    return((sbuf[0] < 0) && (sbuf[1] < 0) && (sbuf[2] >= 0));
	} else {
	    return(false);
	}
    }

    /** Extract a sub-buffer of (at least) specified size, extended to nearest zero crossing, from
     *  the given ByteBuffer.
     *
     *  Returns null if there are fewer than (min_time * sample rate) samples in "source"
     *  Returns between min_time and (max_time + samples to next zero crossing) samples
     *  if enough bytes are available.
     *
     *  @param source : ByteBuffer of source data.
     *  @param max_time : time interval (ms) to slice the source buffer.
     *  @param min_time : minimum size (ms) of the output buffer.
     *  @param format : audio format of source data
     *  @param freq : sample frequency of source data (in Hz)
     *
     * @return ByteBuffer of data copied from "source".  Note: source position will be incremented by
     *         the number of bytes copied.
     */
    private static ByteBuffer getSubBuffer(ByteBuffer source, int max_time, int min_time, int format, int freq) {
	int time_size = calcTimeIndex(format, freq, max_time);
	int bufcount = 0;
	int frameSize = frameSize(format);
	ByteBuffer retbuf = null;
	byte[] retbytes = new byte[source.remaining()+1];

	log.debug("Creating sub buffer.  interval = " + max_time + " freq = " + freq + " time_size = " + time_size + " sample size= " + frameSize(format));
	log.debug("\tBefore: Source = " + source);
	
	if (time_size < source.remaining()) {
	    log.debug("Extracting slice.  Remaining = " + source.remaining());
	    // Enough bytes remaining to pull out a chunk.
	    // First, copy over time_size bytes
	    source.get(retbytes, 0, time_size);
	    bufcount = time_size;
	    // Now, find the zero crossing and add bytes up to it
	    // Loop until we run out of samples or find a zero crossing
	    while ((!isZeroCross(Arrays.copyOfRange(retbytes, bufcount-6, bufcount), 6, format, source.order())) && (source.remaining() >= frameSize)) {
		source.get(retbytes, bufcount, frameSize);
		bufcount += frameSize;
	    }
	} else {
	    log.debug("Not enough bytes.  Copying remaining bytes = " + source.remaining());
	    // Not enough bytes remaning to pull out a chunk. Just copy/return the rest of the buffer.
	    bufcount = source.remaining();
	    source.get(retbytes, 0, bufcount);
	}
	// Now create the ByteBuffer for return... IF there's enough bytes to mess with.  If the size of the array
	// is smaller than the specified minimum time interval, return null.
	if (bufcount > calcTimeIndex(format, freq, min_time)) {
		retbuf = ByteBuffer.allocate(bufcount);
		retbuf.order(source.order()); // set new buffer's byte order to match source buffer.
		retbuf.put(retbytes, 0, bufcount);
		log.debug("\tAfter: source= " + source + "bufcount=" + bufcount + " retbuf= " + retbuf);
	} else {
	    log.warn("Remaining bytes less than minimum time interval.  Discarding.");
	    return(null);
	}
	return(retbuf);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioUtil.class.getName());

}

/** Private class used to associate audio information (frequency, format, etc.)
 *  with a ByteBuffer.
 */
class AudioByteBuffer {
    public ByteBuffer data;
    public int format;
    public int frequency;
    public int loop;
    
    public AudioByteBuffer(ByteBuffer d, int fmt, int freq, int lp) {
	data = d;
	format = fmt;
	frequency = freq;
	loop = lp;
    }
}


package jmri.jmrit.audio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaSound implementation of the Audio Buffer sub-class.
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
 *
 * @author Matthew Harris copyright (c) 2009, 2011
 */
public class JavaSoundAudioBuffer extends AbstractAudioBuffer {

    /**
     * Holds the AudioFormat of this buffer
     */
    private transient AudioFormat audioFormat;

    /**
     * Byte array used to store the actual data read from the file
     */
    private byte[] dataStorageBuffer;

    /**
     * Frequency of this AudioBuffer. Used to calculate pitch changes
     */
    private int freq;

    private long size;

    /**
     * Reference to the AudioInputStream used to read sound data from the file
     */
    private transient AudioInputStream audioInputStream;

    /**
     * Holds the initialised status of this AudioBuffer
     */
    private boolean initialised = false;

    /**
     * Constructor for new JavaSoundAudioBuffer with system name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     */
    public JavaSoundAudioBuffer(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) {
            log.debug("New JavaSoundAudioBuffer: " + systemName);
        }
        initialised = init();
    }

    /**
     * Constructor for new JavaSoundAudioBuffer with system name and user name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     * @param userName   AudioBuffer object user name
     */
    public JavaSoundAudioBuffer(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) {
            log.debug("New JavaSoundAudioBuffer: " + userName + " (" + systemName + ")");
        }
        initialised = init();
    }

    /**
     * Performs any necessary initialisation of this AudioBuffer
     *
     * @return True if successful
     */
    private boolean init() {
        this.audioFormat = null;
        dataStorageBuffer = null;
        this.freq = 0;
        this.size = 0;
        this.setStartLoopPoint(0, false);
        this.setEndLoopPoint(0, false);
        this.setState(STATE_EMPTY);
        return true;
    }

    /**
     * Return reference to the DataStorageBuffer byte array
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     *
     * @return buffer[] reference to DataStorageBuffer
     */
    protected byte[] getDataStorageBuffer() {
        return dataStorageBuffer;
    }

    /**
     * Retrieves the format of the sound sample stored in this buffer as an
     * AudioFormat object
     *
     * @return audio format as an AudioFormat object
     */
    protected AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public String toString() {
        if (this.getState() != STATE_LOADED) {
            return "Empty buffer";
        } else {
            return this.getURL() + " (" + parseFormat() + ", " + this.freq + " Hz)";
        }
    }

    @Override
    protected boolean loadBuffer(InputStream stream) {
        if (!initialised) {
            return false;
        }

        // Reinitialise
        init();

        // Create the input stream for the audio file
        try {
            audioInputStream = AudioSystem.getAudioInputStream(stream);
        } catch (UnsupportedAudioFileException ex) {
            log.error("Unsupported audio file format when loading buffer:" + ex);
            return false;
        } catch (IOException ex) {
            log.error("Error loading buffer:" + ex);
            return false;
        }

        return (this.processBuffer());
    }

    @Override
    protected boolean loadBuffer() {
        if (!initialised) {
            return false;
        }

        // Reinitialise
        init();

        // Retrieve filename of specified .wav file
        File file = new File(FileUtil.getExternalFilename(this.getURL()));

        // Create the input stream for the audio file
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException ex) {
            log.error("Unsupported audio file format when loading buffer:" + ex);
            return false;
        } catch (IOException ex) {
            log.error("Error loading buffer:" + ex);
            return false;
        }

        return (this.processBuffer());
    }

    private boolean processBuffer() {

        // Temporary storage buffer
        byte[] buffer;

        // Get the AudioFormat
        audioFormat = audioInputStream.getFormat();
        this.freq = (int) audioFormat.getSampleRate();

        // Determine the required buffer size in bytes
        // number of channels * length in frames * sample size in bits / 8 bits in a byte
        int dataSize = audioFormat.getChannels()
                * (int) audioInputStream.getFrameLength()
                * audioFormat.getSampleSizeInBits() / 8;
        if (log.isDebugEnabled()) {
            log.debug("Size of JavaSoundAudioBuffer (" + this.getSystemName() + ") = " + dataSize);
        }
        if (dataSize > 0) {
            // Allocate buffer space
            buffer = new byte[dataSize];

            // Load into data buffer
            int bytesRead;
            int totalBytesRead = 0;
            try {
                // Read until end of audioInputStream reached
                log.debug("Start to load JavaSoundBuffer...");
                while ((bytesRead
                        = audioInputStream.read(buffer,
                                totalBytesRead,
                                buffer.length - totalBytesRead))
                        != -1 && totalBytesRead < buffer.length) {
                    log.debug("read " + bytesRead + " bytes of total " + dataSize);
                    totalBytesRead += bytesRead;
                }
            } catch (IOException ex) {
                log.error("Error when reading JavaSoundAudioBuffer (" + this.getSystemName() + ") " + ex);
                return false;
            }

            // Done. All OK.
            log.debug("...finished loading JavaSoundBuffer");
        } else {
            // Not loaded anything
            log.warn("Unable to determine length of JavaSoundAudioBuffer (" + this.getSystemName() + ")");
            log.warn(" - buffer has not been loaded.");
            return false;
        }

        // Done loading - need to convert byte endian order
        this.dataStorageBuffer = convertAudioEndianness(buffer, audioFormat.getSampleSizeInBits() == 16);

        // Set initial loop points
        this.setStartLoopPoint(0, false);
        this.setEndLoopPoint(audioInputStream.getFrameLength(), false);
        this.generateLoopBuffers(LOOP_POINT_BOTH);

        // Store length of sample
        this.size = audioInputStream.getFrameLength();

        this.setState(STATE_LOADED);
        if (log.isDebugEnabled()) {
            log.debug("Loaded buffer: " + this.getSystemName());
            log.debug(" from file: " + this.getURL());
            log.debug(" format: " + parseFormat() + ", " + freq + " Hz");
            log.debug(" length: " + audioInputStream.getFrameLength());
        }
        return true;

    }

    @Override
    protected void generateLoopBuffers(int which) {
        // TODO: Actually write this bit
        //if ((which==LOOP_POINT_START)||(which==LOOP_POINT_BOTH)) {
        //}
        //if ((which==LOOP_POINT_END)||(which==LOOP_POINT_BOTH)) {
        //}
        if (log.isDebugEnabled()) {
            log.debug("Method generateLoopBuffers() called for JavaSoundAudioBuffer " + this.getSystemName());
        }
    }

    @Override
    protected boolean generateStreamingBuffers() {
        // TODO: Actually write this bit
        if (log.isDebugEnabled()) {
            log.debug("Method generateStreamingBuffers() called for JavaSoundAudioBuffer " + this.getSystemName());
        }
        return true;
    }

    @Override
    protected void removeStreamingBuffers() {
        // TODO: Actually write this bit
        if (log.isDebugEnabled()) {
            log.debug("Method removeStreamingBuffers() called for JavaSoundAudioBuffer " + this.getSystemName());
        }
    }

    @Override
    public int getFormat() {
        if (audioFormat != null) {
            if (audioFormat.getChannels() == 1 && audioFormat.getSampleSizeInBits() == 8) {
                return FORMAT_8BIT_MONO;
            } else if (audioFormat.getChannels() == 1 && audioFormat.getSampleSizeInBits() == 16) {
                return FORMAT_16BIT_MONO;
            } else if (audioFormat.getChannels() == 2 && audioFormat.getSampleSizeInBits() == 8) {
                return FORMAT_8BIT_STEREO;
            } else if (audioFormat.getChannels() == 2 && audioFormat.getSampleSizeInBits() == 16) {
                return FORMAT_16BIT_STEREO;
            } else {
                return FORMAT_UNKNOWN;
            }
        }
        return FORMAT_UNKNOWN;
    }

    @Override
    public long getLength() {
        return this.size;
    }

    @Override
    public int getFrequency() {
        return this.freq;
    }

    /**
     * Internal method to return a string representation of the audio format
     *
     * @return string representation
     */
    private String parseFormat() {
        switch (this.getFormat()) {
            case FORMAT_8BIT_MONO:
                return "8-bit mono";
            case FORMAT_16BIT_MONO:
                return "16-bit mono";
            case FORMAT_8BIT_STEREO:
                return "8-bit stereo";
            case FORMAT_16BIT_STEREO:
                return "16-bit stereo";
            default:
                return "unknown format";
        }
    }

    /**
     * Converts the endianness of an AudioBuffer to the format required by the
     * JRE.
     *
     * @param audioData      byte array containing the read PCM data
     * @param twoByteSamples true if 16-bits per sample
     * @return byte array containing converted PCM data
     */
    private static byte[] convertAudioEndianness(byte[] audioData, boolean twoByteSamples) {

        // Create ByteBuffer for output and set endianness
        ByteBuffer out = ByteBuffer.allocate(audioData.length);
        out.order(ByteOrder.nativeOrder());

        // Wrap the audioData into a ByteBuffer for input and set endianness
        // (always Little Endian for a WAV file)
        ByteBuffer in = ByteBuffer.wrap(audioData);
        in.order(ByteOrder.LITTLE_ENDIAN);

        // Check if we have double-byte samples (i.e. 16-bit)
        if (twoByteSamples) {
            // If so, create ShortBuffer views of the in and out ByteBuffers
            // for further processing
            ShortBuffer outShort = out.asShortBuffer();
            ShortBuffer inShort = in.asShortBuffer();

            // Loop through appending data to the output buffer
            while (inShort.hasRemaining()) {
                outShort.put(inShort.get());
            }

        } else {
            // Otherwise, just loop through appending data to the output buffer
            while (in.hasRemaining()) {
                out.put(in.get());
            }
        }

        // Rewind the ByteBuffer
        out.rewind();

        // Convert output to an array if necessary
        if (!out.hasArray()) {
            // Allocate space
            byte[] array = new byte[out.capacity()];
            // fill the array
            out.get(array);
            // clear the ByteBuffer
            out.clear();

            return array;
        }

        return out.array();
    }

    @Override
    protected void cleanup() {
        if (log.isDebugEnabled()) {
            log.debug("Cleanup JavaSoundAudioBuffer (" + this.getSystemName() + ")");
        }
        this.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(JavaSoundAudioBuffer.class);

}

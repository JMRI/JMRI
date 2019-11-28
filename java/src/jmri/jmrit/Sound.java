package jmri.jmrit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import jmri.util.FileUtil;

/**
 * Provide simple way to load and play sounds in JMRI.
 * <p>
 * This is placed in the jmri.jmrit package by process of elimination. It
 * doesn't belong in the base jmri package, as it's not a basic interface. Nor
 * is it a specific implementation of a basic interface, which would put it in
 * jmri.jmrix. It seems most like a "tool using JMRI", or perhaps a tool for use
 * with JMRI, so it was placed in jmri.jmrit.
 *
 * @see jmri.jmrit.sound
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2006
 * @author Dave Duchamp Copyright (C) 2011 - add streaming play of large files
 */
public class Sound {

    // files over this size will be streamed
    public static final long LARGE_SIZE = 100000;
    private final URL url;
    private boolean streaming = false;
    private boolean streamingStop = false;
    private AtomicReference<Clip> clipRef = new AtomicReference<>();
    private boolean autoClose = true;

    /**
     * Create a Sound object using the media file at path
     *
     * @param path path, portable or absolute, to the media
     * @throws NullPointerException if path cannot be converted into a URL by
     *                              {@link jmri.util.FileUtilSupport#findURL(java.lang.String)}
     */
    public Sound(@Nonnull String path) throws NullPointerException {
        this(FileUtil.findURL(path));
    }

    /**
     * Create a Sound object using the media file
     *
     * @param file reference to the media
     * @throws java.net.MalformedURLException if file cannot be converted into a
     *                                        valid URL
     */
    public Sound(@Nonnull File file) throws MalformedURLException {
        this(file.toURI().toURL());
    }

    /**
     * Create a Sound object using the media URL
     *
     * @param url path to the media
     * @throws NullPointerException if URL is null
     */
    public Sound(@Nonnull URL url) throws NullPointerException {
        if (url == null) {
            throw new NullPointerException();
        }
        this.url = url;
        try {
            streaming = this.needStreaming();
            if (!streaming) {
                clipRef.updateAndGet(clip -> {
                    return openClip();
                });
            }
        } catch (URISyntaxException ex) {
            streaming = false;
        } catch (IOException ex) {
            log.error("Unable to open {}", url);
        }
    }
    
    private Clip openClip() {
        Clip newClip = null;
        try {
            newClip = AudioSystem.getClip(null);
            newClip.addLineListener(event -> {
                if (LineEvent.Type.STOP.equals(event.getType())) {
                    if (autoClose) {
                        clipRef.updateAndGet(clip -> {
                            if (clip != null) {
                                clip.close();
                            }
                            return null;
                        });
                    }
                }
            });
            newClip.open(AudioSystem.getAudioInputStream(url));
        } catch (IOException ex) {
            log.error("Unable to open {}", url);
        } catch (LineUnavailableException ex) {
            log.error("Unable to provide audio playback", ex);
        } catch (UnsupportedAudioFileException ex) {
            log.error("{} is not a recognised audio format", url);
        }
        
        return newClip;
    }
    
    /**
     * Set if the clip be closed automatically.
     * @param autoClose true if closed automatically
     */
    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }
    
    /**
     * Get if the clip is closed automatically.
     * @return true if closed automatically
     */
    public boolean getAutoClose() {
        return autoClose;
    }
    
    /**
     * Closes the sound.
     */
    public void close() {
        if (streaming) {
            streamingStop = true;
        } else {
            clipRef.updateAndGet(clip -> {
                if (clip != null) {
                    clip.close();
                }
                return null;
            });
        }
    }
    
    @Override
    public void finalize() throws Throwable {
        try {
            if (!streaming) {
                clipRef.updateAndGet(clip -> {
                    if (clip != null) {
                        clip.close();
                    }
                    return null;
                });
            }
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Play the sound once.
     */
    public void play() {
        if (streaming) {
            Runnable streamSound = new StreamingSound(this.url);
            Thread tStream = new Thread(streamSound);
            tStream.start();
        } else {
            clipRef.updateAndGet(clip -> {
                if (clip == null) {
                    clip = openClip();
                }
                if (clip != null) {
                    clip.start();
                }
                return clip;
            });
        }
    }

    /**
     * Play the sound as an endless loop
     */
    public void loop() {
        this.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Play the sound in a loop count times. Use
     * {@link javax.sound.sampled.Clip#LOOP_CONTINUOUSLY} to create an endless
     * loop.
     *
     * @param count the number of times to loop
     */
    public void loop(int count) {
        if (streaming) {
            log.warn("Streaming this audio file, loop() not allowed");
        } else {
            clipRef.updateAndGet(clip -> {
                if (clip == null) {
                    clip = openClip();
                }
                if (clip != null) {
                    clip.loop(count);
                }
                return clip;
            });
        }
    }

    /**
     * Stop playing a loop.
     */
    public void stop() {
        if (streaming) {
            streamingStop = true;
        } else {
            clipRef.updateAndGet(clip -> {
                if (clip != null) {
                    clip.stop();
                }
                return clip;
            });
        }
    }

    private boolean needStreaming() throws URISyntaxException, IOException {
        if (url != null) {
            if ("file".equals(this.url.getProtocol())) {
                return (new File(this.url.toURI()).length() > LARGE_SIZE);
            } else {
                return this.url.openConnection().getContentLengthLong() > LARGE_SIZE;
            }
        }
        return false;
    }

    /**
     * Play a sound from a buffer
     *
     * @param wavData data to play
     */
    public static void playSoundBuffer(byte[] wavData) {

        // get characteristics from buffer
        float sampleRate = 11200.0f;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = (sampleSizeInBits > 8);
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        SourceDataLine line;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error.
            log.warn("line not supported: " + info);
            return;
        }
        // Obtain and open the line.
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            // Handle the error.
            log.error("error opening line: " + ex);
            return;
        }
        line.start();
        // write(byte[] b, int off, int len) 
        line.write(wavData, 0, wavData.length);

    }

    public static class WavBuffer {

        public WavBuffer(byte[] content) {
            buffer = Arrays.copyOf(content, content.length);

            // find fmt chunk and set offset
            int index = 12; // skip RIFF header
            while (index < buffer.length) {
                // new chunk
                if (buffer[index] == 0x66
                        && buffer[index + 1] == 0x6D
                        && buffer[index + 2] == 0x74
                        && buffer[index + 3] == 0x20) {
                    // found it
                    fmtOffset = index;
                    return;
                } else {
                    // skip
                    index = index + 8
                            + buffer[index + 4]
                            + buffer[index + 5] * 256
                            + buffer[index + 6] * 256 * 256
                            + buffer[index + 7] * 256 * 256 * 256;
                    log.debug("index now {}", index);
                }
            }
            log.error("Didn't find fmt chunk");

        }

        // we maintain this, but don't use it for anything yet
        @SuppressFBWarnings(value = "URF_UNREAD_FIELD")
        int fmtOffset;

        byte[] buffer;

        float getSampleRate() {
            return 11200.0f;
        }

        int getSampleSizeInBits() {
            return 8;
        }

        int getChannels() {
            return 1;
        }

        boolean getBigEndian() {
            return false;
        }

        boolean getSigned() {
            return (getSampleSizeInBits() > 8);
        }
    }

    public class StreamingSound implements Runnable {

        private final URL url;
        private AudioInputStream stream = null;
        private AudioFormat format = null;
        private SourceDataLine line = null;
        private jmri.Sensor streamingSensor = null;

        /**
         * A runnable to stream in sound and play it This method does not read
         * in an entire large sound file at one time, but instead reads in
         * smaller chunks as needed.
         *
         * @param url the URL containing audio media
         */
        public StreamingSound(URL url) {
            this.url = url;
        }

        @Override
        public void run() {
            // Note: some of the following is based on code from 
            //      "Killer Game Programming in Java" by A. Davidson.
            // Set up the audio input stream from the sound file
            try {
                // link an audio stream to the sampled sound's file
                stream = AudioSystem.getAudioInputStream(url);
                format = stream.getFormat();
                log.debug("Audio format: " + format);
                // convert ULAW/ALAW formats to PCM format
                if ((format.getEncoding() == AudioFormat.Encoding.ULAW)
                        || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                    AudioFormat newFormat
                            = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                    format.getSampleRate(),
                                    format.getSampleSizeInBits() * 2,
                                    format.getChannels(),
                                    format.getFrameSize() * 2,
                                    format.getFrameRate(), true);  // big endian
                    // update stream and format details
                    stream = AudioSystem.getAudioInputStream(newFormat, stream);
                    log.info("Converted Audio format: {}", newFormat);
                    format = newFormat;
                    log.debug("new converted Audio format: " + format);
                }
            } catch (UnsupportedAudioFileException e) {
                log.error("AudioFileException " + e.getMessage());
                return;
            } catch (IOException e) {
                log.error("IOException " + e.getMessage());
                return;
            }
            streamingStop = false;
            if (streamingSensor == null) {
                streamingSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor("ISSOUNDSTREAMING");
            }

            setSensor(jmri.Sensor.ACTIVE);

            if (!streamingStop) {
                // set up the SourceDataLine going to the JVM's mixer
                try {
                    // gather information for line creation
                    DataLine.Info info
                            = new DataLine.Info(SourceDataLine.class, format);
                    if (!AudioSystem.isLineSupported(info)) {
                        log.error("Audio play() does not support: " + format);
                        return;
                    }
                    // get a line of the required format
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format);
                } catch (Exception e) {
                    log.error("Exception while creating Audio out " + e.getMessage());
                    return;
                }
            }
            if (streamingStop) {
                line.close();
                setSensor(jmri.Sensor.INACTIVE);
                return;
            }
            // Read  the sound file in chunks of bytes into buffer, and
            //   pass them on through the SourceDataLine 
            int numRead;
            byte[] buffer = new byte[line.getBufferSize()];
            log.debug("streaming sound buffer size = " + line.getBufferSize());
            line.start();
            // read and play chunks of the audio
            try {
                int offset;
                while ((numRead = stream.read(buffer, 0, buffer.length)) >= 0) {
                    offset = 0;
                    while (offset < numRead) {
                        offset += line.write(buffer, offset, numRead - offset);
                    }
                }
            } catch (IOException e) {
                log.error("IOException while reading sound file " + e.getMessage());
            }
            // wait until all data is played, then close the line
            line.drain();
            line.stop();
            line.close();
            setSensor(jmri.Sensor.INACTIVE);
        }

        private void setSensor(int mode) {
            if (streamingSensor != null) {
                try {
                    streamingSensor.setState(mode);
                } catch (jmri.JmriException ex) {
                    log.error("Exception while setting ISSOUNDSTREAMING sensor {} to {}", streamingSensor.getDisplayName(), mode);
                }
            }
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Sound.class);
}

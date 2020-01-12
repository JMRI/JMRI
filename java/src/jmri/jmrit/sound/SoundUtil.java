package jmri.jmrit.sound;

import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide simple way to load and play Java 2 sounds in JMRI.
 * <p>
 * This is placed in the jmri.jmrit.sound package by process of elimination. It
 * doesn't belong in the base jmri package, as it's not a basic interface. Nor
 * is it a specific implementation of a basic interface, which would put it in
 * jmri.jmrix.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2006
 */
public class SoundUtil {

    /**
     * Play a sound from a buffer
     *
     */
    public static void playSoundBuffer(byte[] wavData) {

        // get characteristics from buffer
        jmri.jmrit.sound.WavBuffer wb = new jmri.jmrit.sound.WavBuffer(wavData);
        float sampleRate = wb.getSampleRate();
        int sampleSizeInBits = wb.getSampleSizeInBits();
        int channels = wb.getChannels();
        boolean signed = wb.getSigned();
        boolean bigEndian = wb.getBigEndian();

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

    private static final int BUFFER_LENGTH = 4096;

    public static byte[] bufferFromFile(String filename,
            float sampleRate, int sampleSizeInBits, int channels,
            boolean signed, boolean bigEndian) throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {

        File sourceFile = new File(filename);

        // Get the type of the source file. We need this information 
        // later to write the audio data to a file of the same type. 
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
        //AudioFileFormat.Type targetFileType = fileFormat.getType(); 
        AudioFormat audioFormat = fileFormat.getFormat();

        // get desired output format
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        // get a conversion stream
        // (Errors not checked yet)
        AudioInputStream stream = AudioSystem.getAudioInputStream(sourceFile);
        AudioInputStream inputAIS = AudioSystem.getAudioInputStream(format, stream);

        // Read the audio data into a memory buffer. 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBufferSize = BUFFER_LENGTH * audioFormat.getFrameSize();
        byte[] abBuffer = new byte[nBufferSize];
        while (true) {
            if (log.isDebugEnabled()) {
                log.debug("trying to read (bytes): " + abBuffer.length);
            }
            int nBytesRead = inputAIS.read(abBuffer);

            if (log.isDebugEnabled()) {
                log.debug("read (bytes): " + nBytesRead);
            }
            if (nBytesRead == -1) {
                break;
            }
            baos.write(abBuffer, 0, nBytesRead);
        }

        // Create byte array
        byte[] abAudioData = baos.toByteArray();
        return abAudioData;
    }

    private final static Logger log = LoggerFactory.getLogger(SoundUtil.class);
}

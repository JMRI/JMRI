package jmri.jmrit.sound;

import java.applet.AudioClip;
import java.net.URL;
import java.net.MalformedURLException;

import javax.sound.sampled.*;

/**
 * Wrap a byte array to provide WAV file functionality
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */

public class WavBuffer {

    public WavBuffer(byte[] content) {
        buffer = content;
        
        // find fmt chunk and set offset
        int index = 12; // skip RIFF header
        while (index<buffer.length) {
            // new chunk
            if (buffer[index]==0x66 && 
                buffer[index+1]==0x6D &&
                buffer[index+2]==0x74 &&
                buffer[index+3]==0x20) {
                    // found it, header in place
                    fmtOffset = index;
                     return;
            } else {
                // skip
                index = index + 8
                        +fourByte(index+4);
            }
        }
        log.error("Didn't find fmt chunk");
        
    }
    
    int fmtOffset;
    
    byte[] buffer;
    
    public float getSampleRate() {
        return fourByte(fmtOffset+12);
    }
    public int getSampleSizeInBits() {
        return twoByte(fmtOffset+22);
    }
    public int getChannels() {
        return twoByte(fmtOffset+10);
    }
    public boolean getBigEndian() {return false;}
    public boolean getSigned() { return (getSampleSizeInBits()>8); }

    int twoByte(int index) {return buffer[index]+buffer[index+1]*256;}
    
    int fourByte(int index) {
        return  buffer[index]
                +buffer[index+1]*256
                +buffer[index+2]*256*256
                +buffer[index+3]*256*256*256;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(WavBuffer.class.getName());
}

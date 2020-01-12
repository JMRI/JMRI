package jmri.jmrix.loconet.spjfile;

import jmri.jmrit.Sound;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.loconet.spjfile package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class SpjFileTest {

    @Test
    public void testCreate() {
        new SpjFile(new java.io.File("ac4400.spj"));
    }

    SpjFile testFile = null;

    void loadFile() throws java.io.IOException {
        if (testFile == null) {
            testFile = new SpjFile(new java.io.File("java/test/jmri/jmrix/loconet/spjfile/test.spj"));
            testFile.read();
        }
    }
    
    @Test
    public void testPlayWav() throws java.io.IOException {
        loadFile();

        // and write
        // play 1st wav header
        int n = testFile.numHeaders();
        for (int i = 1; i < n; i++) {
            if (testFile.headers[i].isWAV()) {
                byte[] buffer = testFile.headers[i].getByteArray();
                playSoundBuffer(buffer);
                break;
            }
        }
        jmri.util.JUnitAppender.suppressWarnMessage("line not supported: interface SourceDataLine supporting format PCM_UNSIGNED 11200.0 Hz, 8 bit, mono, 1 bytes/frame, ");
    }

    public void playSoundBuffer(byte[] data) {
        Sound.playSoundBuffer(data);
    }

    @Test
    public void testGetMapEntries() throws java.io.IOException {
        loadFile();

        Assert.assertEquals("1", "DIESEL_START_BELL", testFile.getMapEntry(1));
        Assert.assertEquals("2", "DIESEL_START", testFile.getMapEntry(2));
        Assert.assertEquals("31", "USER_F28", testFile.getMapEntry(31));
    }


    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpjFileTest.class);
}

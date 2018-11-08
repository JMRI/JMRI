package jmri.jmrit.sound;

import jmri.util.FileUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SoundUtil class.
 * <p>
 * Note: This makes noise!
 *
 * @author	Bob Jacobsen Copyright 2006
 * @author Randall Wood (C) 2016
 */
public class SoundUtilTest {

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    @Test
    public void testLargeBuffer() throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {
        String name = FileUtil.getAbsoluteFilename("program:resources/sounds/Button.wav");
        byte[] results = SoundUtil.bufferFromFile(name,
                44100.0f, 16, 1, true, false);
        Assert.assertEquals("length", 89872, results.length);
        Assert.assertEquals("byte 0", 0x09, 0xFF & results[0]);
        Assert.assertEquals("byte 1", 0x00, 0xFF & results[1]);
        Assert.assertEquals("byte 2", 0x0B, 0xFF & results[2]);
        Assert.assertEquals("byte 3", 0x00, 0xFF & results[3]);
    }
}

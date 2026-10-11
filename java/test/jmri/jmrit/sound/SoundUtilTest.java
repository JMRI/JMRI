package jmri.jmrit.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.FileUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SoundUtil class.
 * <p>
 * Note: This makes noise!
 *
 * @author Bob Jacobsen Copyright 2006
 * @author Randall Wood (C) 2016
 */
public class SoundUtilTest {

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    @Test
    public void testLargeBuffer() throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {
        String name = FileUtil.getAbsoluteFilename("program:resources/sounds/Button.wav");
        byte[] results = SoundUtil.bufferFromFile(name,
                44100.0f, 16, 1, true, false);
        assertEquals( 89872, results.length, "length");
        assertEquals( 0x09, 0xFF & results[0], "byte 0");
        assertEquals( 0x00, 0xFF & results[1], "byte 1");
        assertEquals( 0x0B, 0xFF & results[2], "byte 2");
        assertEquals( 0x00, 0xFF & results[3], "byte 3");
    }
}

package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SoundToneTest {

    @Test
    public void testCTor() {
        SoundTone t = new SoundTone();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testTone() {
        try {
            SoundTone.tone(440, 200, .2);
        } catch (javax.sound.sampled.LineUnavailableException lue) {
            Assert.assertNotNull("LineUnavailableException Thrown", null);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JavaSoundAudioListener
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioListenerTest {

    @Test
    public void testCtor() {
        JavaSoundAudioListener l = new JavaSoundAudioListener("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        JavaSoundAudioListener l = new JavaSoundAudioListener("testsysname", "testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

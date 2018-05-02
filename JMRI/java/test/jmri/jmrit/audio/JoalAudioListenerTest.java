package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioListener
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioListenerTest {

    @Test
    public void testCtor() {
        JoalAudioListener l = new JoalAudioListener("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        JoalAudioListener l = new JoalAudioListener("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

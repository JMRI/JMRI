package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioListener
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioListenerTest {

    private JoalAudioFactory factory;

    @Test
    public void testCtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioListener l = new JoalAudioListener("test", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioListener l = new JoalAudioListener("testsysname", "testusername", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        factory = new JoalAudioFactory();
    }

    @After
    public void tearDown() {
        factory.cleanup();
        JUnitUtil.tearDown();
    }
}

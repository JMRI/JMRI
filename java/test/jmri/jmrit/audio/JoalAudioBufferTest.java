package jmri.jmrit.audio;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioBuffer
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioBufferTest {

    private JoalAudioFactory factory;

    @Test
    public void testCtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioBuffer l = new JoalAudioBuffer("test", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioBuffer l = new JoalAudioBuffer("testsysname", "testusername", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        factory = new JoalAudioFactory();
    }

    @After
    public void tearDown() {
        factory.cleanup();
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}

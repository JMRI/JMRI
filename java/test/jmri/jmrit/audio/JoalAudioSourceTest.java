package jmri.jmrit.audio;

import apps.tests.Log4JFixture;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioSourceTest {

    private JoalAudioFactory factory;

    @Test
    public void testCtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioSource l = new JoalAudioSource("test", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        Assume.assumeTrue("Unable to initialize JoalAudioFactory", factory.init());
        JoalAudioSource l = new JoalAudioSource("testsysname", "testusername", factory.getAL());
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        factory = new JoalAudioFactory();
        InstanceManager.setDefault(AudioFactory.class, factory);
        factory.init();
    }

    @After
    public void tearDown() {
        factory.cleanup();
        JUnitUtil.tearDown();
    }
}

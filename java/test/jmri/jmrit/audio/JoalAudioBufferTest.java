package jmri.jmrit.audio;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioBuffer
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioBufferTest {

    @Test
    @Ignore("Causes NPE when run, needs additional setup")
    public void testCtor() {
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    @Ignore("Causes NPE when run, needs additional setup")
    public void testC2Stringtor() {
        JoalAudioBuffer l = new JoalAudioBuffer("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}

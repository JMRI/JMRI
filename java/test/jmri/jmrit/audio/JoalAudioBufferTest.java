package jmri.jmrit.audio;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

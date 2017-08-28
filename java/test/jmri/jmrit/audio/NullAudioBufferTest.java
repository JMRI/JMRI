package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NullAudioBuffer
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class NullAudioBufferTest {

    @Test
    public void testCtor() {
        NullAudioBuffer l = new NullAudioBuffer("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        NullAudioBuffer l = new NullAudioBuffer("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

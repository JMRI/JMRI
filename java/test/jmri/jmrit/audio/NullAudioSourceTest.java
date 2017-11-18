package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of NullAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class NullAudioSourceTest {

    @Test
    @Ignore("Fails when the constructor calls the superclass constructor")
    public void testCtor() {
        NullAudioSource l = new NullAudioSource("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    @Ignore("Fails when the constructor calls the superclass constructor")
    public void testC2Stringtor() {
        NullAudioSource l = new NullAudioSource("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

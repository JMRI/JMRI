package jmri.jmrit.audio;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioSourceTest {

    @Test
    @Ignore("Fails when the constructor calls the superclass constructor")
    public void testCtor() {
        JoalAudioSource l = new JoalAudioSource("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    @Ignore("Fails when the constructor calls the superclass constructor")
    public void testC2Stringtor() {
        JoalAudioSource l = new JoalAudioSource("testsysname","testusername");
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

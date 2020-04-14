package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioFactory
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioFactoryTest {

    @Test
    public void testCtor() {
        JoalAudioFactory l = new JoalAudioFactory();
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

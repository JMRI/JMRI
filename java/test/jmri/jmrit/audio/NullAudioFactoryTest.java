package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NullAudioFactory
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class NullAudioFactoryTest {

    @Test
    public void testCtor() {
        NullAudioFactory l = new NullAudioFactory();
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

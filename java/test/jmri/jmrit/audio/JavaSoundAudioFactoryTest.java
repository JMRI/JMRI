package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JavaSoundAudioFactory
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioFactoryTest {

    @Test
    public void testCtor() {
        JavaSoundAudioFactory l = new JavaSoundAudioFactory();
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

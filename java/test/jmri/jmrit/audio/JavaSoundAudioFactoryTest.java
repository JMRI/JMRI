package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of JavaSoundAudioFactory
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioFactoryTest {

    @Test
    public void testCtor() {
        JavaSoundAudioFactory l = new JavaSoundAudioFactory();
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}

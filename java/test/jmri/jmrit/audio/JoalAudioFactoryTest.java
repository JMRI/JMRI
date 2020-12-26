package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of JoalAudioFactory
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JoalAudioFactoryTest {

    @Test
    public void testCtor() {
        JoalAudioFactory l = new JoalAudioFactory();
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

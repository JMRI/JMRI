package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of NullAudioFactory
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NullAudioFactoryTest {

    @Test
    public void testCtor() {
        NullAudioFactory l = new NullAudioFactory();
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}

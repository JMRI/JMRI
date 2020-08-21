package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of NullAudioBuffer
 *
 * @author Paul Bender Copyright (C) 2017
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}

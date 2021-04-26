package jmri.jmrit.audio.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DefaultAudioManagerXmlTest.java
 *
 * Test for the DefaultAudioManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DefaultAudioManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DefaultAudioManagerXml constructor",new DefaultAudioManagerXml());
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


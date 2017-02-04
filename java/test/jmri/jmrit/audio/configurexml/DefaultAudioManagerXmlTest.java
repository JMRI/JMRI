package jmri.jmrit.audio.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DefaultAudioManagerXmlTest.java
 *
 * Description: tests for the DefaultAudioManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DefaultAudioManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DefaultAudioManagerXml constructor",new DefaultAudioManagerXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}


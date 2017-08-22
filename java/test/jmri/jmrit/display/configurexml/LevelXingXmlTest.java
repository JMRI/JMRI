package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.layoutEditor.configurexml.LevelXingXml;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LevelXingXmlTest.java
 *
 * Description: tests for the LevelXingXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LevelXingXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LevelXingXml constructor", new LevelXingXml());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }
}

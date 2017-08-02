package jmri.util.docbook.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RevHistoryXmlTest.java
 *
 * Description: tests for the RevHistoryXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RevHistoryXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RevHistoryXml constructor",new RevHistoryXml());
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


package jmri.jmrix.acela.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * AcelaSignalHeadXmlTest.java
 *
 * Description: tests for the AcelaSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaSignalHeadXml constructor",new AcelaSignalHeadXml());
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


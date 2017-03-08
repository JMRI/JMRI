package jmri.jmrix.acela.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AcelaSensorManagerXmlTest.java
 *
 * Description: tests for the AcelaSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaSensorManagerXml constructor",new AcelaSensorManagerXml());
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


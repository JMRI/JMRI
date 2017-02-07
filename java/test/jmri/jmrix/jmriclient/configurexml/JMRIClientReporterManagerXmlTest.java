package jmri.jmrix.jmriclient.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientReporterManagerXmlTest.java
 *
 * Description: tests for the JMRIClientReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientReporterManagerXml constructor",new JMRIClientReporterManagerXml());
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


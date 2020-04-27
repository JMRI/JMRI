package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientReporterManagerXmlTest.java
 *
 * Test for the JMRIClientReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientReporterManagerXml constructor",new JMRIClientReporterManagerXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


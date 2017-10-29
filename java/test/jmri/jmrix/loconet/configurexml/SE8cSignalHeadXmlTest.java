package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SE8cSignalHeadXmlTest.java
 *
 * Description: tests for the SE8cSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SE8cSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SE8cSignalHeadXml constructor",new SE8cSignalHeadXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


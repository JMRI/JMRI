package jmri.jmrix.grapevine.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialSignalHeadXmlTest.java
 *
 * Description: tests for the SerialSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSignalHeadXml constructor",new SerialSignalHeadXml());
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


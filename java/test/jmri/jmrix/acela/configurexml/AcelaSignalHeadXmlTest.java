package jmri.jmrix.acela.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
        JUnitUtil.setUp();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, new AcelaSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


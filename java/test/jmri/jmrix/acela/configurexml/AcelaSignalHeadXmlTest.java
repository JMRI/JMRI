package jmri.jmrix.acela.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * AcelaSignalHeadXmlTest.java
 *
 * Test for the AcelaSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaSignalHeadXml constructor",new AcelaSignalHeadXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, new AcelaSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


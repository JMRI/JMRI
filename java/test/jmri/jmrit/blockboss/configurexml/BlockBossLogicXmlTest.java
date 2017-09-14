package jmri.jmrit.blockboss.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * BlockBossLogicXmlTest.java
 *
 * Description: tests for the BlockBossLogicXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class BlockBossLogicXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("BlockBossLogicXml constructor",new BlockBossLogicXml());
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


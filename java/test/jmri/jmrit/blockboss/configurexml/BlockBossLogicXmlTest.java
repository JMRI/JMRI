package jmri.jmrit.blockboss.configurexml;

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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}


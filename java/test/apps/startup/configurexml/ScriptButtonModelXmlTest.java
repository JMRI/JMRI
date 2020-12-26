package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * ScriptButtonModelXmlTest.java
 *
 * Test for the ScriptButtonModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ScriptButtonModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ScriptButtonModelXml constructor",new ScriptButtonModelXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


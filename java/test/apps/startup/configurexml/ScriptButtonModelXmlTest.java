package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
      Assertions.assertNotNull(new ScriptButtonModelXml(), "ScriptButtonModelXml constructor");
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


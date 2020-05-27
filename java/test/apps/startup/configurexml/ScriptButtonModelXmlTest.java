package apps.startup.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}


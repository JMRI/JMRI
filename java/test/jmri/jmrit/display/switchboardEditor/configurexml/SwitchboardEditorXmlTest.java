package jmri.jmrit.display.switchboardEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SwitchboardEditorXml class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SwitchboardEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SwitchboardEditorXml constructor",new SwitchboardEditorXml());
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


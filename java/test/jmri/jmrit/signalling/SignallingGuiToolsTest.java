package jmri.jmrit.signalling;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SignallingGuiToolsTest {

    // the class under test is a collection of static methods for dealing with
    // signals in GUIs.

    @Test
    @Ignore("needs more thought")
    public void testRemoveAlreadyAssignedSignalmastLogic() {
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SignallingGuiToolsTest.class.getName());

}

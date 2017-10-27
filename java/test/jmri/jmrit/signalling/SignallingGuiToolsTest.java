package jmri.jmrit.signalling;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingGuiToolsTest.class);

}

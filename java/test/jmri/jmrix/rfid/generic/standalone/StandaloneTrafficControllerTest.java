package jmri.jmrix.rfid.generic.standalone;

import org.junit.After;
import org.junit.Before;

/**
 * StandaloneTrafficControllerTest.java
 *
 * Description:	tests for the StandaloneTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneTrafficControllerTest extends jmri.jmrix.rfid.RfidTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo());
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}

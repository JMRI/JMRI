package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo());
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}

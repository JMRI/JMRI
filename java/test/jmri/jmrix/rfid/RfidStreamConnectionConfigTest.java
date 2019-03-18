package jmri.jmrix.rfid;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class RfidStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new RfidStreamConnectionConfig();
    }

    @After
    public void tearDown() {
	cc = null;
        jmri.util.JUnitUtil.tearDown();
    }

}

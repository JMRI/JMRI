package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * RfidReplyTest.java
 * <p>
 * Test for the jmri.jmrix.rfid.RfidReply class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RfidReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    RfidTrafficController tc = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new RfidTrafficController() {
            @Override
            public void sendInitString() {
            }
        };
        m = new RfidReply(tc) {
            @Override
            public String toMonitorString() {
                return "";
            }
        };
    }

    @After
    public void tearDown() {
        tc = null;
        m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}

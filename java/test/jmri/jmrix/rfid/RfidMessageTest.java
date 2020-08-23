package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * RfidMessageTest.java
 * <p>
 * Test for the jmri.jmrix.rfid.RfidMessage class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RfidMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new RfidMessage(20) {
            @Override
            public String toMonitorString() {
                return "";
            }
        };
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}

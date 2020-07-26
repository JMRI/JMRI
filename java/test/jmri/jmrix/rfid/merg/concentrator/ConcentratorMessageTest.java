package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * ConcentratorMessageTest.java
 * <p>
 * Test for the jmri.jmrix.rfid.merge.concentrator.ConcentratorMessage class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new ConcentratorMessage(20) {
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

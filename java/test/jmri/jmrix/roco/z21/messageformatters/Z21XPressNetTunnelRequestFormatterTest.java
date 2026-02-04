package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21XPressNetTunnelRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XPressNetTunnelRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testTranslateXPressNetTunnelRequest(){

        Z21Message msg = new Z21Message(new XNetMessage("01 04 05"));
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("XpressNet Tunnel Message: 01 04 05",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21XPressNetTunnelRequestFormatter();
    }

}

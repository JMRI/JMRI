package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the XNetLocoInfoMUAddressFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoMUAddressFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testFormatter() {
        XNetReply r = new XNetReply("E2 14 C1 37");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Locomotive Information Response: Multi Unit Base Address,Forward,in 128 Speed Step Mode,Speed Step: 64. Address is Free for Operation. ",formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetLocoInfoMUAddressFormatter();
    }

}

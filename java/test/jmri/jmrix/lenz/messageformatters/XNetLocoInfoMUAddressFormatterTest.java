package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoInfoMUAddressFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoMUAddressFormatterTest {

    @Test
    void testFormatter() {
        XNetLocoInfoMUAddressFormatter formatter = new XNetLocoInfoMUAddressFormatter();
        XNetReply r = new XNetReply("E2 14 C1 37");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Locomotive Information Response: Multi Unit Base Address,Forward,in 128 Speed Step Mode,Speed Step: 64. Address is Free for Operation. ",formatter.formatMessage(r));
    }
}

package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLI101AddressReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLI101AddressReplyFormatterTest {

    @Test
    public void testMonitorStringLIAddressReply(){
        XNetLI101AddressReplyFormatter formatter = new XNetLI101AddressReplyFormatter();
        XNetReply r = new XNetReply("F2 01 01 F2");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyLIAddress",1),formatter.formatMessage(r));
    }

}

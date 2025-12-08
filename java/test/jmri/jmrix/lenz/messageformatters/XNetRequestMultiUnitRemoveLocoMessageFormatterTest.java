package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetRequestMultiUnitRemoveLocoMessageFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetRequestMultiUnitRemoveLocoMessageFormatterTest {

    @Test
    public void testFormatter() {
        XNetRequestMultiUnitRemoveLocoMessageFormatter formatter = new XNetRequestMultiUnitRemoveLocoMessageFormatter();
        XNetMessage msg = XNetMessage.getRemoveLocoFromConsistMsg(42,1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals( "Mobile Decoder Operations Request: Remove Locomotive: 1234 from Multi Unit Consist: 42", formatter.formatMessage(msg));

    }
}

package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetMultiUnitSearchRequestMessageFormatter class
 * .
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetMultiUnitSearchRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void handleSearchForwardRequest() {
        XNetMessage msg = XNetMessage.getDBSearchMsgConsistAddress(42, true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Forward from Consist Address: 42", formatter.formatMessage(msg));
    }

    @Test
    void handleSearchBackwardRequest() {
        XNetMessage msg = XNetMessage.getDBSearchMsgConsistAddress(42,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Backward from Consist Address: 42", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetMultiUnitSearchRequestMessageFormatter();
    }

}

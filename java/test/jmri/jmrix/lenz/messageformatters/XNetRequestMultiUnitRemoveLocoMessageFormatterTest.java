package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetRequestMultiUnitRemoveLocoMessageFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetRequestMultiUnitRemoveLocoMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter() {

        XNetMessage msg = XNetMessage.getRemoveLocoFromConsistMsg(42,1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals( "Mobile Decoder Operations Request: Remove Locomotive: 1234 from Multi Unit Consist: 42", formatter.formatMessage(msg));

    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetRequestMultiUnitRemoveLocoMessageFormatter();
    }

}

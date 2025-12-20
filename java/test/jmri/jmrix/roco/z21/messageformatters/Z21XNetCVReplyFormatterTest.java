package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.messageformatters.Z21XNetCVReplyFormatter class
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetCVReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Message message = new Z21XNetReply("64 14 00 14 05 61");
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals( Bundle.getMessage("Z21LAN_X_CV_RESULT", 21, 5), formatter.formatMessage(message), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21XNetCVReplyFormatter();
    }

}

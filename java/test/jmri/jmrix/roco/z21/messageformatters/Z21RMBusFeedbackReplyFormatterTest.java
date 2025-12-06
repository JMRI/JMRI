package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21RMBusFeedbackReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusFeedbackReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testRMBusFeedbackGroup0Formatter() {

        byte msg[] = {(byte) 0x0F, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply message = new Z21Reply(msg, 15);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("RM Feedback Status for group 0"
                + "\n\tModule 1 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 2 Contact 1 On;Contact 2 On;Contact 3 On;Contact 4 On;Contact 5 On;Contact 6 On;Contact 7 On;Contact 8 On"
                + "\n\tModule 3 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 4 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 5 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 6 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 7 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 8 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 9 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 10 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                , formatter.formatMessage(message));

    }
        @Test
        public void testRMBusFeedbackGroup1Formatter() {

        byte msg2[] = {(byte) 0x0F, (byte) 0x00, (byte) 0x80, (byte) 0x00,
                (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00};
        Z21Reply message = new Z21Reply(msg2, 15);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("RM Feedback Status for group 1"
                + "\n\tModule 11 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 12 Contact 1 On;Contact 2 On;Contact 3 On;Contact 4 On;Contact 5 On;Contact 6 On;Contact 7 On;Contact 8 On"
                + "\n\tModule 13 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 14 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 15 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 16 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 17 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 18 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 19 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                + "\n\tModule 20 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off"
                , formatter.formatMessage(message));

    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21RMBusFeedbackReplyFormatter();
    }

}

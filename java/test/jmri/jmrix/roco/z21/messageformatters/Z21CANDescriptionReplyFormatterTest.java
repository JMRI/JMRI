package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21CANDescriptionReplyFormatter class
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANDescriptionReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){
        byte msg[] = {(byte) 0x16, (byte) 0x00, (byte) 0xC8, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 'T', (byte) 'e', (byte) 's', (byte) 't', (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0',
                (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0', (byte) '\0'};
        Z21Reply reply = new Z21Reply(msg, 22);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Description for abcd is Test", formatter.formatMessage(reply));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANDescriptionReplyFormatter();
    }

}

package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21CANGetDescriptionRequestFormatter class
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANSetDescriptionRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Z21Message m = Z21Message.getLanCanSetDescription(0xabcd,"Test");
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Set Description for abcd to Test", formatter.formatMessage(m));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANSetDescriptionRequestFormatter();
    }

}

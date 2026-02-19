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
public class Z21CANGetDescriptionRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Z21Message m = Z21Message.getLanCanGetDescription(0xabcd);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Get Description Request for abcd", formatter.formatMessage(m));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANGetDescriptionRequestFormatter();
    }

}

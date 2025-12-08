package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetDHandMUErrorMessageFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetDHandMUErrorMessageFormatterTest {

    XNetDHandMUErrorMessageFormatter formatter;

    @BeforeEach
    public void setUp() {
        formatter = new XNetDHandMUErrorMessageFormatter();
    }

    @Test
    public void testToMonitorStringErrorNotOperated(){
        XNetReply r = new XNetReply("E1 81 60");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorNotOperated"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorInUse(){
        XNetReply r = new XNetReply("E1 82 63");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorInUse"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorAlreadyDH(){
        XNetReply r = new XNetReply("E1 83 62");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorAlreadyDH"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorNonZeroSpeed(){
        XNetReply r = new XNetReply("E1 84 65");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorNonZeroSpeed"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorLocoNotMUed(){
        XNetReply r = new XNetReply("E1 85 64");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorLocoNotMU"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorAddressNotMUBase(){
        XNetReply r = new XNetReply("E1 86 67");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorLocoNotMUBase"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorCanNotDelete(){
        XNetReply r = new XNetReply("E1 87 66");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorCanNotDelete"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorCSStackFull(){
        XNetReply r = new XNetReply("E1 88 69");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorStackFull"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHErrorOther(){
        XNetReply r = new XNetReply("E1 89 69");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyDHErrorOther", 9), formatter.formatMessage(r));
    }

}


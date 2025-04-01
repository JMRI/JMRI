package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetStackSearchReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */

public class XNetStackSearchReplyFormatterTest {

    XNetStackSearchReplyFormatter formatter;

    @BeforeEach
    public void setUp(){
        formatter = new XNetStackSearchReplyFormatter();
    }

    @Test
    public void testToMonitorStringSearchResponseNormalLoco(){
        XNetReply r = new XNetReply("E3 30 C1 04 11");
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("XNetReplySearchNormalLabel") + " 260";
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(targetString,formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringSearchResponseDoubleHeaderLoco(){
        XNetReply r = new XNetReply("E3 31 C1 04 17");
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("XNetReplySearchDHLabel") + " 260";
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringSearchResponseMUBaseLoco(){
        XNetReply r = new XNetReply("E3 32 00 04 C5");
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("XNetReplySearchMUBaseLabel") + " 4";
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringSearchResponseMULoco(){
        XNetReply r = new XNetReply("E3 33 C1 04 15");
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("XNetReplySearchMULabel") + " 260";
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringSearchResponseFail(){
        XNetReply r = new XNetReply("E3 34 C1 04 15");
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("XNetReplySearchFailedLabel") + " 260";
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

}

package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XNetCSSoftwareVersionReplyFormatterTest {

    @Test
    public void testHandlesLZ100Message() {
        XNetCSSoftwareVersionReplyFormatter formatter = new XNetCSSoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("63 21 36 00 55");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersion",3.6,Bundle.getMessage("CSTypeLZ100")), formatter.formatMessage(r));
    }

    @Test
    public void testHandlesLH200Message() {
        XNetCSSoftwareVersionReplyFormatter formatter = new XNetCSSoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("63 21 36 01 55");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersion",3.6,Bundle.getMessage("CSTypeLH200")), formatter.formatMessage(r));
    }

    @Test
    public void testHandlesCompactMessage() {
        XNetCSSoftwareVersionReplyFormatter formatter = new XNetCSSoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("63 21 36 02 55");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersion",3.6,Bundle.getMessage("CSTypeCompact")), formatter.formatMessage(r));
    }

    @Test
    public void testHandlesMultiMausMessage() {
        XNetCSSoftwareVersionReplyFormatter formatter = new XNetCSSoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("63 21 36 10 55");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersion",3.6,Bundle.getMessage("CSTypeMultiMaus")), formatter.formatMessage(r));
    }

    @Test
    public void testHandlesOtherCSMessage() {
        XNetCSSoftwareVersionReplyFormatter formatter = new XNetCSSoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("63 21 36 20 55");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersion",3.6,"32"), formatter.formatMessage(r));
    }

}

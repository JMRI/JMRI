package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
public class XNetMultiUnitSearchRequestMessageFormatterTest {

    @Test
    void handleSearchForwardRequest() {
        XNetMultiUnitSearchRequestMessageFormatter formatter = new XNetMultiUnitSearchRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getDBSearchMsgConsistAddress(42, true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Forward from Consist Address: 42", formatter.formatMessage(msg));
    }

    @Test
    void handleSearchBackwardRequest() {
        XNetMultiUnitSearchRequestMessageFormatter formatter = new XNetMultiUnitSearchRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getDBSearchMsgConsistAddress(42,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Backward from Consist Address: 42", formatter.formatMessage(msg));
    }
}

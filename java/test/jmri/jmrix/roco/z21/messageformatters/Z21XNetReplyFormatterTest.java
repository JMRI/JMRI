package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21XNetReply;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class Z21XNetReplyFormatterTest {

    @Test
    public void testFormatter(){
        Z21XNetReplyFormatter formatter = new Z21XNetReplyFormatter();
        Message message = new Z21XNetReply("64 14 00 14 05 61");
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals( Bundle.getMessage("Z21LAN_X_CV_RESULT", 21, 5), formatter.formatMessage(message), "Monitor String");
    }
}

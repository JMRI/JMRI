package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
/**
 * Format Feedback Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFeedbackRequestCommandMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.lenz.XNetMessage && ((jmri.jmrix.lenz.XNetMessage) m).getElement(0) == XNetConstants.ACC_INFO_REQ;
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        String nibblekey=(((m.getElement(2) & 0x01) == 0x01) ? "FeedbackEncoderUpperNibble" : "FeedbackEncoderLowerNibble");
        return Bundle.getMessage("XNetMessageFeedbackRequest",
                m.getElement(1),
                Bundle.getMessage(nibblekey));
    }
}

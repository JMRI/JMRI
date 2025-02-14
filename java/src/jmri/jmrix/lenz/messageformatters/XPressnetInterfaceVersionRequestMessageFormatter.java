package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

public class XPressnetInterfaceVersionRequestMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.LI_VERSION_REQUEST;
    }

    @Override
    public String formatMessage(Message m) {
        if (m.getElement(0) == XNetConstants.LI_VERSION_REQUEST)
            return Bundle.getMessage("XNetMessageRequestLIVersion");
        throw new IllegalArgumentException("Unknown request message sent LI Version Request Message Formatter");
    }

}

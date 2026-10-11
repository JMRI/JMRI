package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for Loco Function Status High Reply messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoFunctionStatusHighReplyFormatter implements XPressNetMessageFormatter {

    private static final String RS_TYPE = "rsType";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE &&
                m.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyLocoLabel") + " " +
                Bundle.getMessage(RS_TYPE) + " " +
               Bundle.getMessage("XNetReplyF13StatusLabel") + " "+
               XNetLocoInfoReplyUtilities.parseFunctionHighStatus(m.getElement(2), m.getElement(3));
    }

}

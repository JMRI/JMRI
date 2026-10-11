package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format Double Header Request Messages.
 */

public class XNetDoubleHeaderRequestMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                (m.getElement(0) == XNetConstants.LOCO_DOUBLEHEAD
                && m.getElement(1) == XNetConstants.LOCO_DOUBLEHEAD_BYTE2);
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m))
            throw new IllegalArgumentException("Unknown Double Header Request: " + m.toString());
        int loco1 = LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3));
        int loco2 = LenzCommandStation.calcLocoAddress(m.getElement(4), m.getElement(5));
        if (loco2 == 0) {
            return Bundle.getMessage("XNetMessageDisolveDoubleHeaderRequest",loco1);
        } else {
            return Bundle.getMessage("XNetMessageBuildDoubleHeaderRequest",loco1,loco2);
        }
    }

}

package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.util.StringUtil;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificReply extends SerialReply {

    // create a new one
    public SpecificReply(SerialTrafficController tc) {
        super(tc);
        setBinary(true);
    }

    public SpecificReply(String s, SerialTrafficController tc) {
        super(tc, s);
        setBinary(true);
    }

    public SpecificReply(SerialReply l, SerialTrafficController tc) {
        super(tc, l);
        setBinary(true);
    }

    @Override
    public String toMonitorString() {
        // check for valid length
        StringBuilder sb = new StringBuilder();
        if (getNumDataElements() == 1) {
            int msg = getElement(0);
            switch (msg & 0xFF) {
                case Constants.POLL_REQ:
                    sb.append("Data Available\n");
                    break;
                case Constants.TIME_REQ_CP11:
                    sb.append("CP11 time request\n");
                    break;
                case Constants.TIME_REQ_CP10:
                    sb.append("CP10 time request\n");
                    break;
                case Constants.FILTER_FAIL:
                    sb.append("Input Filter Failed\n");
                    break;
                case Constants.READY_REQ:
                    sb.append("Interface Ready\n");
                    break;
                default:
                    sb.append("One byte, probably CRC\n");
                    break;
            }
            return sb.toString();
        } else if ((getNumDataElements() == 2) && ((getElement(1) & 0xFF) == Constants.READY_REQ)) {
            sb.append("CRC 0x");
            sb.append(jmri.util.StringUtil.twoHexFromInt(getElement(0)));
            sb.append(" and Interface Ready\n");
            return sb.toString();
        } else if ((getElement(0) & 0xFF) == Constants.POLL_REQ) {
            // must be received data
            sb.append("Receive data, ");
            sb.append(getElement(1) & 0xFF);
            sb.append(" bytes; ");
            int last = (getElement(1) & 0xFF) + 1;
            int bits = (getElement(2) & 0xFF);
            for (int i = 3; i <= last; i++) {
                if (i != 3) {
                    sb.append("; ");  // separate all but last command
                }
                if ((bits & 0x01) != 0) {
                    sb.append(X10Sequence.formatCommandByte(getElement(i) & 0xFF));
                } else {
                    sb.append(X10Sequence.formatAddressByte(getElement(i) & 0xFF));
                }
                bits = bits >> 1;  // shift over before next byte
            }
            sb.append("\n");
            return sb.toString();
        } else if ((getElement(0) & 0xFF) == 0x5B) {
            if (getNumDataElements() == 3) {
                sb.append("EPROM Address Poll: ");
                sb.append(StringUtil.twoHexFromInt(getElement(1) & 0xFF));
                sb.append(":");
                sb.append(StringUtil.twoHexFromInt(getElement(2) & 0xFF));
                sb.append("\n");
                return sb.toString();
            } else {
                sb.append("EPROM Address Poll, invalid length: ");
                sb.append(getNumDataElements());
                return sb.toString();
            }
        } else {
            // don't know, just show
            sb.append("Unknown reply of length ");
            sb.append(getNumDataElements());
            sb.append(" ");
            sb.append(toString()).append("\n");
            sb.append("\n");
            return sb.toString();
        }
    }

}



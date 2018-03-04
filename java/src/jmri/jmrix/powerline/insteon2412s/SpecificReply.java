package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008, 2009 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificReply extends jmri.jmrix.powerline.SerialReply {

    // create a new one
    public SpecificReply(SerialTrafficController tc) {
        super(tc);
        setBinary(true);
    }

    public SpecificReply(String s, SerialTrafficController tc) {
        super(tc, s);
        setBinary(true);
    }

    @Override
    public String toMonitorString() {
        // check for valid length
        int len = getNumDataElements();
        StringBuilder text = new StringBuilder();
        if ((getElement(0) & 0xFF) != Constants.HEAD_STX) {
            text.append("INVALID HEADER: " + String.format("0x%1X", getElement(0) & 0xFF));
            text.append(" len: " + len);
        } else {
            switch (getElement(1) & 0xFF) {
                case Constants.FUNCTION_REQ_STD:
                    text.append("Send Cmd ");
                    if (len == 8 || len == 22) {
                        if ((getElement(5) & Constants.FLAG_BIT_STDEXT) == Constants.FLAG_STD) {
                            text.append(" Std");
                        } else if (len == 22) {
                            text.append(" Ext");
                        }
                        switch (getElement(5) & Constants.FLAG_MASK_MSGTYPE) {
                            case Constants.FLAG_TYPE_P2P:
                                text.append(" Direct");
                                break;
                            case Constants.FLAG_TYPE_ACK:
                                text.append(" ACK");
                                break;
                            case Constants.FLAG_TYPE_NAK:
                                text.append(" NAK");
                                break;
                            case Constants.FLAG_TYPE_GBCAST:
                                text.append(" Group Broadcast");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANUP:
                                text.append(" Group Broadcast Cleanup");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANACK:
                                text.append(" Group Broadcast Cleanup ACK");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANNAK:
                                text.append(" Group Broadcast Cleanup NAK");
                                break;
                            default:
                                log.warn("Unhandled msg type: {}, getElement(5) & Constants.FLAG_MASK_MSGTYPE");
                                break;
                        }
                        text.append(" message,");
                        text.append(String.format(" %d hops left", (getElement(5) & Constants.FLAG_MASK_HOPSLEFT >> Constants.FLAG_SHIFT_HOPSLEFT)));
                        text.append(String.format(" , %d max hops", (getElement(5) & Constants.FLAG_MASK_MAXHOPS)));
                        text.append(" addr " + String.format("%1$X.%2$X.%3$X", (getElement(2) & 0xFF), (getElement(3) & 0xFF), (getElement(4) & 0xFF)));
                        switch (getElement(6) & 0xFF) {
                            case Constants.CMD_LIGHT_ON_FAST:
                                text.append(" ON FAST ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_ON_RAMP:
                                text.append(" ON RAMP ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_OFF_FAST:
                                text.append(" OFF FAST ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_OFF_RAMP:
                                text.append(" OFF RAMP ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_CHG:
                                text.append(" CHG ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            default:
                                text.append(" Unknown cmd: " + StringUtil.twoHexFromInt(getElement(6) & 0xFF));
                                break;
                        }
                        if ((getElement(8) & 0xFF) == Constants.REPLY_NAK) {
                            text.append(" NAK - command not processed");
                        }
                    } else {
                        text.append(" !! Length wrong: " + len);
                    }
                    break;
                case Constants.POLL_REQ_BUTTON:
                    text.append("Poll Button ");
                    int button = ((getElement(2) & Constants.BUTTON_BITS_ID) >> 4) + 1;
                    text.append(button);
                    int op = getElement(2) & Constants.BUTTON_BITS_OP;
                    if (op == Constants.BUTTON_HELD) {
                        text.append(" HELD");
                    } else if (op == Constants.BUTTON_REL) {
                        text.append(" RELEASED");
                    } else if (op == Constants.BUTTON_TAP) {
                        text.append(" TAP");
                    }
                    break;
                case Constants.POLL_REQ_BUTTON_RESET:
                    text.append("Reset by Button at Power Cycle");
                    break;
                case Constants.FUNCTION_REQ_X10:
                    text.append("Send Cmd X10 ");
                    if ((getElement(3) & Constants.FLAG_BIT_X10_CMDUNIT) == Constants.FLAG_X10_RECV_CMD) {
                        text.append(X10Sequence.formatCommandByte(getElement(2) & 0xFF));
                    } else {
                        text.append(X10Sequence.formatAddressByte(getElement(2) & 0xFF));
                    }
                    if ((getElement(4) & 0xFF) == Constants.REPLY_NAK) {
                        text.append(" NAK - command not processed");
                    }
                    break;
                case Constants.POLL_REQ_X10:
                    text.append("Poll Cmd X10 ");
                    if ((getElement(3) & Constants.FLAG_BIT_X10_CMDUNIT) == Constants.FLAG_X10_RECV_CMD) {
                        text.append(X10Sequence.formatCommandByte(getElement(2) & 0xFF));
                    } else {
                        text.append(X10Sequence.formatAddressByte(getElement(2) & 0xFF));
                    }
                    break;
                default: {
                    text.append(" Unknown command: " + StringUtil.twoHexFromInt(getElement(1) & 0xFF));
                    text.append(" len: " + len);
                }
            }
        }
        return text + "\n";
    }
    private final static Logger log = LoggerFactory.getLogger(SpecificReply.class); // NOI18N

}



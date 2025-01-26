package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.FeedbackItem;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

import java.util.Optional;

/**
 * Format XPressNet feedback reply messages for display.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFeedbackReplyFormatter implements XPressNetMessageFormatter {
    private static final String COLUMN_STATE = "ColumnState";
    private static final String MAKE_LABEL = "MakeLabel";
    private static final String POWER_STATE_ON = "PowerStateOn";
    private static final String POWER_STATE_OFF = "PowerStateOff";
    private static final String BEAN_NAME_TURNOUT = "BeanNameTurnout";
    private static final String X_NET_REPLY_NOT_OPERATED = "XNetReplyNotOperated";
    private static final String X_NET_REPLY_THROWN_LEFT = "XNetReplyThrownLeft";
    private static final String X_NET_REPLY_THROWN_RIGHT = "XNetReplyThrownRight";
    private static final String X_NET_REPLY_INVALID = "XNetReplyInvalid";
    private static final String X_NET_REPLY_CONTACT_LABEL = "XNetReplyContactLabel";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && m.getElement(0) == XNetConstants.ACC_INFO_RESPONSE;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message not supported");
        }
        StringBuilder text = new StringBuilder().append(Bundle.getMessage("XNetReplyFeedbackLabel")).append(" ");
        XNetReply r = (XNetReply) m;
        int numDataBytes = r.getElement(0) & 0x0f;
        for (int i = 1; i < numDataBytes; i += 2) {
            switch (r.getFeedbackMessageType(i)) {
                case 0:
                    text.append(getTurnoutReplyMonitorString(i, "TurnoutWoFeedback",r));
                    break;
                case 1:
                    text.append(getTurnoutReplyMonitorString(i, "TurnoutWFeedback",r));
                    break;
                case 2:
                    text.append(getSensorFeedBackReplyMonitorString(r, i));
                    break;
                default:
                    text.append(r.getElement(i)).append(" ").append(r.getElement(i + 1));
            }
        }
        return text.toString();
    }

    private String getSensorFeedBackReplyMonitorString(XNetReply r, int i) {
        StringBuilder text = new StringBuilder();
        text.append(Bundle.getMessage("XNetReplyFeedbackEncoder")).append(" ").append(r.getFeedbackEncoderMsgAddr(i));
        boolean highnibble = ((r.getElement(i + 1) & 0x10) == 0x10);
        text.append(" ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 5 : 1);

        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                .append(((r.getElement(i + 1) & 0x01) == 0x01) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 6 : 2);

        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                .append(((r.getElement(i + 1) & 0x02) == 0x02) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 7 : 3);

        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                .append(((r.getElement(i + 1) & 0x04) == 0x04) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 8 : 4);

        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                .append(((r.getElement(i + 1) & 0x08) == 0x08) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
        text.append("; ");
        return text.toString();
    }

    private String getTurnoutReplyMonitorString(int startByte, String typeBundleKey,XNetReply r) {
        StringBuilder text = new StringBuilder();
        int turnoutMsgAddr = r.getTurnoutMsgAddr(startByte);
        Optional<FeedbackItem> feedBackOdd = r.selectTurnoutFeedback(turnoutMsgAddr);
        if(feedBackOdd.isPresent()){
            FeedbackItem feedbackItem = feedBackOdd.get();
            text.append(singleTurnoutMonitorMessage(Bundle.getMessage(typeBundleKey), turnoutMsgAddr, feedbackItem));
            text.append(";");
            FeedbackItem pairedItem = feedbackItem.pairedAccessoryItem();
            text.append(singleTurnoutMonitorMessage("", turnoutMsgAddr + 1, pairedItem));

        }
        return text.toString();
    }

    private String singleTurnoutMonitorMessage(String prefix, int turnoutMsgAddr, FeedbackItem feedbackItem) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(prefix).append(" ")
                .append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(BEAN_NAME_TURNOUT))).append(" ")
                .append(turnoutMsgAddr).append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ");
        switch (feedbackItem.getAccessoryStatus()){
            case 0:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_NOT_OPERATED)); // last items on line, no trailing space
                break;
            case 1:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_THROWN_LEFT));
                break;
            case 2:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_THROWN_RIGHT));
                break;
            default:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_INVALID));
        }
        if(feedbackItem.getType()==1){
            outputBuilder.append(" ");
            if(feedbackItem.isMotionComplete()){
                outputBuilder.append(Bundle.getMessage("XNetReplyMotionComplete"));
            } else {
                outputBuilder.append(Bundle.getMessage("XNetReplyMotionIncomplete"));
            }
        }
        return outputBuilder.toString();
    }

}


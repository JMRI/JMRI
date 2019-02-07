package jmri.jmrix.maple.serialmon;

import jmri.jmrix.maple.SerialListener;
import jmri.jmrix.maple.SerialMessage;
import jmri.jmrix.maple.SerialReply;
import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Frame displaying (and logging) serial command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private MapleSystemConnectionMemo _memo = null;

    public SerialMonFrame(MapleSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return "Maple Serial Command Monitor";
    } // TODO I18N

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addSerialListener(this);
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeSerialListener(this);
        super.dispose();
    }

    /**
     * Define system-specific help item
     */
    @Override
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.maple.serialmon.SerialMonFrame", true);
    }

    @Override
    public synchronized void message(SerialMessage l) { // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n", l.toString());
        } else if (l.isPoll()) {
            if ((l.getNumDataElements() <= 6) && (l.getElement(0) == 15)) {
                nextLine("Poll Reply - NAK (error)", l.toString()); // TODO I18N
            } else {
                nextLine("Poll node " + l.getUA() + "\n", l.toString());
            }
        } else if (l.isXmt()) {
            if (l.getNumDataElements() > 12) {
                // this is the write command
                int n = l.getNumItems();
                StringBuilder s = new StringBuilder(String.format("Transmit node=%d ADDR = %d N = %d OB=", l.getUA(), l.getAddress(), n));
                int i = 11;
                while (n > 0) {
                    for (int j = 0; (j < 8) && (n > 0); j++, n--) {
                        s.append((((l.getElement(i) & 0x01) != 0) ? "1" : "0"));
                        i++;
                    }
                    s.append(" ");
                }
                nextLine(s.append("\n").toString(), l.toString());
            } else {
                // this is the reply to the write command
                StringBuilder s = new StringBuilder("Transmit Reply - ");
                if (l.getElement(0) == 6) {
                    s.append("ACK (OK)");
                } else if (l.getElement(0) == 15) {
                    s.append("NAK (error)");
                }
                nextLine(s.append("\n").toString(), l.toString());
            }
        } else {
            nextLine("unrecognized cmd: \"" + l.toString() + "\"\n", "");
        }
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + "\n", l.toString());
        } else if (l.isRcv()) {
            String s = "Receive node=" + l.getUA() + " IB=";
            for (int i = 2; i < l.getNumDataElements(); i++) {
                s = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i), s) + " ";
            }
            nextLine(s + "\n", l.toString());
        } else if (l.getElement(0) == 0x15) {
            String s = "Negative reply " + l.toString();
            nextLine(s + "\n", l.toString());
        } else if (l.getElement(0) == 0x06) {
            String s = "Positive reply " + l.toString();
            nextLine(s + "\n", l.toString());
        } else {
            nextLine("unrecognized rep: \"" + l.toString() + "\"\n", "");
        }
    }

}

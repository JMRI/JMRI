package jmri.jmrix.cmri.serial.serialmon;

import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Frame displaying (and logging) CMRI serial command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private CMRISystemConnectionMemo _memo = null;

    public SerialMonFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return "CMRI Serial Command Monitor";
    }

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

    @Override
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
        } else if (l.isPoll()) {
            nextLine("Poll ua=" + l.getUA() + "\n", l.toString());
        } else if (l.isXmt()) {
            StringBuilder sb = new StringBuilder("Transmit ua=");
            sb.append(l.getUA());
            sb.append(" OB=");
            for (int i = 2; i < l.getNumDataElements(); i++) {
                sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                sb.append(" ");
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else if (l.isInit()) {
            StringBuilder sb = new StringBuilder("Init ua=");
            sb.append(l.getUA());
            sb.append(" type=");
            sb.append((char) l.getElement(2));
            int len = l.getNumDataElements();
            if (len >= 5) {
                sb.append(" DL=");
                sb.append(l.getElement(3) * 256 + l.getElement(4));
            }
            if (len >= 6) {
                sb.append(" NS=");
                sb.append(l.getElement(5));
                sb.append(" CT: ");
                for (int i = 6; i < l.getNumDataElements(); i++) {
                    sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                    sb.append(" ");
                }
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else {
            nextLine("unrecognized cmd: \"" + l.toString() + "\"\n", "");
        }
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
        } else if (l.isRcv()) {
            StringBuilder sb = new StringBuilder("Receive ua=");
            sb.append(l.getUA());
            sb.append(" IB=");
            for (int i = 2; i < l.getNumDataElements(); i++) {
                sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                sb.append(" ");
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else {
            nextLine("unrecognized rep: \"" + l.toString() + "\"\n", "");
        }
    }

}

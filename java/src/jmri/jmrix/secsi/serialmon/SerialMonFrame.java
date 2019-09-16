package jmri.jmrix.secsi.serialmon;

import jmri.jmrix.secsi.SerialListener;
import jmri.jmrix.secsi.SerialMessage;
import jmri.jmrix.secsi.SerialReply;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Frame displaying (and logging) serial command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private SecsiSystemConnectionMemo memo = null;

    public SerialMonFrame(SecsiSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("MonitorXTitle", "SECSI");
    }

    @Override
    protected void init() {
        // connect to TrafficController
        memo.getTrafficController().addSerialListener(this);
    }

    @Override
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.secsi.serialmon.SerialMonFrame", true);  // NOI18N
    }

    @Override
    public void dispose() {
        memo.getTrafficController().removeSerialListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 5) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n", // TODO I18N
                    l.toString());
        } else if (l.isPoll()) {
            nextLine("Poll addr=" + l.getAddr() + "\n", l.toString());
        } else if (l.isXmt()) {
            String s = "Transmit addr=" + l.getAddr()
                    + " byte " + (l.getElement(2) & 0x000000ff)
                    + " data = " + Integer.toHexString(l.getElement(3) & 0xff);
            nextLine(s + "\n", l.toString());
        } else {
            nextLine("Unrecognized cmd: \"" + l.toString() + "\"\n", l.toString());
        }
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() == 1) {
            if (l.getElement(0) == 0) {
                nextLine("NACK\n", l.toString());
            } else {
                nextLine("Ack from node " + l.getElement(0) + "\n", l.toString());
            }
        } else if (l.getNumDataElements() != 5) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + ":" + l.toString() + "\n",
                    l.toString());
        } else { // must be data reply
            StringBuilder s = new StringBuilder(String.format("Receive addr=%d IB=", l.getAddr()));
            for (int i = 2; i < 4; i++) {
                s.append(Integer.toHexString(l.getElement(i))).append(" ");
            }
            nextLine(s.append("\n").toString(), l.toString());
        }
    }

}

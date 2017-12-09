package jmri.jmrix.oaktree.serialmon;

import jmri.jmrix.oaktree.SerialListener;
import jmri.jmrix.oaktree.SerialMessage;
import jmri.jmrix.oaktree.SerialReply;
import jmri.jmrix.oaktree.SerialTrafficController;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;

/**
 * Frame displaying (and logging) serial command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private OakTreeSystemConnectionMemo _memo = null;

    public SerialMonFrame(OakTreeSystemConnectionMemo memo) {
        super();
        _memo=memo;
    }

    @Override
    protected String title() {
        return "Oak Tree Serial Command Monitor";
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
        if (l.getNumDataElements() < 5) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n",
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

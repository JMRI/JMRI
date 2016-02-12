// SerialMonFrame.java
package jmri.jmrix.secsi.serialmon;

import jmri.jmrix.secsi.SerialListener;
import jmri.jmrix.secsi.SerialMessage;
import jmri.jmrix.secsi.SerialReply;
import jmri.jmrix.secsi.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) serial command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 * @version $Revision$
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    /**
     *
     */
    private static final long serialVersionUID = -3112238221579394922L;

    public SerialMonFrame() {
        super();
    }

    protected String title() {
        return "SECSI Serial Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
        super.dispose();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "string concatenation, efficiency not as important as clarity here")
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 5) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "string concatenation, efficiency not as important as clarity here")
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() == 1) {
            if (l.getElement(0) == 0) {
                nextLine("NACK\n", l.toString());
            } else {
                nextLine("Ack from node " + l.getElement(0) + "\n", l.toString());
            }
            return;
        } else if (l.getNumDataElements() != 5) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + ":" + l.toString() + "\n",
                    l.toString());
            return;
        } else { // must be data reply
            String s = "Receive addr=" + l.getAddr() + " IB=";
            for (int i = 2; i < 4; i++) {
                s += Integer.toHexString(l.getElement(i)) + " ";
            }
            nextLine(s + "\n", l.toString());
            return;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonFrame.class.getName());

}

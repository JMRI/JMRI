// SerialMonFrame.java

package jmri.jmrix.oaktree.serialmon;

import jmri.jmrix.oaktree.SerialListener;
import jmri.jmrix.oaktree.SerialMessage;
import jmri.jmrix.oaktree.SerialReply;
import jmri.jmrix.oaktree.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001, 2006
 * @version         $Revision: 1.1 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Oak Tree Serial Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 5) {
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } else if (l.isPoll()) {
            nextLine("Poll addr="+l.getAddr()+"\n", l.toString());
        } else if (l.isXmt()) {
            String s = "Transmit addr="+l.getAddr()+" OB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i)&0x000000ff)+" ";
            nextLine(s+"\n", l.toString());
        } else if (l.isInit()) {
            String s = "Init addr="+l.getAddr()
                +" type="+((char)l.getElement(2));
            int len = l.getNumDataElements();
            if (len>=5)
                s +=" DL="+(l.getElement(3)*256+l.getElement(4));
            if (len>=6) {
                s+=" NS="+l.getElement(5)+" CT: ";
                for (int i=6; i<l.getNumDataElements(); i++)
                    s+=Integer.toHexString(l.getElement(i)&0x000000ff)+" ";
            }
            nextLine(s+"\n", l.toString());
        } else
            nextLine("Unrecognized cmd: \""+l.toString()+"\"\n", l.toString());
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } else if (l.isRcv()) {
            String s = "Receive addr="+l.getAddr()+" IB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i))+" ";
            nextLine(s+"\n", l.toString());
        } else
            nextLine("Unrecognized rep: \""+l.toString()+"\"\n", l.toString());
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}

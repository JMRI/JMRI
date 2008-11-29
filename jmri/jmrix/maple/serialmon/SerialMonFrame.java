// SerialMonFrame.java

package jmri.jmrix.maple.serialmon;

import jmri.jmrix.maple.SerialListener;
import jmri.jmrix.maple.SerialMessage;
import jmri.jmrix.maple.SerialReply;
import jmri.jmrix.maple.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version         $Revision: 1.1 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Maple Serial Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
        super.dispose();
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } else if (l.isPoll()) {
            nextLine("Poll node "+l.getUA()+"\n", l.toString());
        } else if (l.isXmt()) {
            String s = "Transmit node="+l.getUA()+" OB=";
            int i = 11;
            while (i<l.getNumDataElements()-3) {
                for (int j=0; j<8; j++) {
                    s+=(((l.getElement(i)&0x01)!=0)?"1":"0");
                    i++;
                }
                s+=" ";            }
            nextLine(s+"\n", l.toString());
        } else if (l.isInit()) {
            String s = "Init ua="+l.getUA()
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
            nextLine("unrecognized cmd: \""+l.toString()+"\"\n", "");
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } else if (l.isRcv()) {
            String s = "Receive node="+l.getUA()+" IB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i), s)+" ";
            nextLine(s+"\n", l.toString());
        } else if (l.getElement(0) == 0x15) {
            String s = "Negative reply "+l.toString();
            nextLine(s+"\n", l.toString());            
        } else if (l.getElement(0) == 0x06) {
            String s = "Positive reply "+l.toString();
            nextLine(s+"\n", l.toString());            
        } else
            nextLine("unrecognized rep: \""+l.toString()+"\"\n", "");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}

/*
 * SerialMonFrame.java
 *
 * Created on August 18, 2007, 10:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.serialmon;

/**
 *
 * @author tim
 */
import jmri.jmrix.tchtech.serial.SerialListener;
import jmri.jmrix.tchtech.serial.SerialMessage;
import jmri.jmrix.tchtech.serial.SerialReply;
import jmri.jmrix.tchtech.serial.SerialTrafficController;

/**
 * Frame displaying (and logging) TCH Technology SNIC serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version         $Revision: 1.2 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "TCH Technology Node Interface Card Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
    }

     public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } else if (l.isErr()) {
            nextLine("Error="+l.getNA()+"\n", l.toString());    
        } else if (l.isInq()) {
            nextLine("Inquire="+l.getNA()+"\n", l.toString());    
        } else if (l.isPoll()) {
            nextLine("Poll NA="+l.getNA()+"\n", l.toString());
        } else if (l.isXmt()) {
            String s = "Transmit Node="+l.getNA()+" OB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i)&0x000000ff)+" ";
            nextLine(s+"\n", l.toString());
        } else if (l.isInit()) {
            String s = "Set Attrib Node="+l.getNA()
                +" type="+((char)l.getElement(2));//2
            int len = l.getNumDataElements();
                         
            if (len>=6) {
                s+=" SA="+l.getElement(3)+" TT: ";//3
                for (int i=4; i<l.getNumDataElements(); i++)//1=6
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
            String s = "Receive NA="+l.getNA()+" IB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i), s)+" ";
            nextLine(s+"\n", l.toString());
        } else
            nextLine("unrecognized rep: \""+l.toString()+"\"\n", "");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}

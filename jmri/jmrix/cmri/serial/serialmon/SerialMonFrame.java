// SerialMonFrame.java

package jmri.jmrix.cmri.serial.serialmon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;

/**
 * Frame displaying (and logging) CMRI serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version         $Revision: 1.4 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {
    
    public SerialMonFrame() {
        super();
    }
    
    protected String title() { return "CMRI Serial Command Monitor"; }
    
    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }
    
    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
	}
    
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        if (l.isPoll()) {
            nextLine("Poll ua="+l.getUA()+"\n", l.toString()+"\n");
        } else if (l.isXmt()) {
            String s = "Transmit ua="+l.getUA()+" OB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i))+" ";
            nextLine(s+"\n", l.toString()+"\n");
        } else if (l.isInit()) {
            String s = "Init ua="+l.getUA()
                +" type="+((char)l.getElement(2))
                +" DL="+(l.getElement(3)*256+l.getElement(4));
            s+=" NS="+l.getElement(5)+" CT: ";
            for (int i=6; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i))+" ";
            nextLine(s+"\n", l.toString()+"\n");
        } else
            nextLine("unrecognized cmd: \""+l.toString()+"\"\n", "");
    }
    
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        if (l.isRcv()) {
            String s = "Receive ua="+l.getUA()+" IB=";
            for (int i=2; i<l.getNumDataElements(); i++)
                s+=Integer.toHexString(l.getElement(i))+" ";
            nextLine(s+"\n", l.toString()+"\n");
        } else
            nextLine("unrecognized rep: \""+l.toString()+"\"\n", "");
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());
    
}

// SerialMonFrame.java

package jmri.jmrix.maple.serialmon;

import org.apache.log4j.Logger;
import jmri.jmrix.maple.SerialListener;
import jmri.jmrix.maple.SerialMessage;
import jmri.jmrix.maple.SerialReply;
import jmri.jmrix.maple.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version         $Revision$
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

    protected void addHelpMenu() {
        addHelpMenu("package.jmri.jmrix.maple.serialmon.SerialMonFrame", true);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION", justification="string concatenation, efficiency not as important as clarity here")
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",
                            l.toString());
            return;
        } 
		else if (l.isPoll()) {
			if ( (l.getNumDataElements()<=6) && (l.getElement(0)==15) ) 
				nextLine("Poll Reply - NAK (error)", l.toString());
			else 
				nextLine("Poll node "+l.getUA()+"\n", l.toString());
        } 
		else if (l.isXmt()) {
			if (l.getNumDataElements()>12) {
				// this is the write command
				int n = l.getNumItems();
				String s = "Transmit node="+l.getUA()+" ADDR = "+l.getAddress()+" N = "+n+" OB=";
				int i = 11;
				while (n>0) {
					for (int j=0; (j<8)&&(n>0); j++,n--) {
						s+=(((l.getElement(i)&0x01)!=0)?"1":"0");
						i++;
					}
					s+=" ";
				}
				nextLine(s+"\n", l.toString());
			}
			else {
				// this is the reply to the write command
				String s = "Transmit Reply - ";
				if (l.getElement(0)==6) s = s+"ACK (OK)";
				else if (l.getElement(0)==15) s = s+"NAK (error)";
				nextLine(s+"\n", l.toString());
			}
        } 
        else
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

    static Logger log = Logger.getLogger(SerialMonFrame.class.getName());

}

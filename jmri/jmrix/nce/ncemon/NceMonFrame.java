/** 
 * NceMonFrame.java
 *
 * Description:		Frame displaying (and logging) NCE command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.ncemon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;

public class NceMonFrame extends jmri.jmrix.AbstractMonFrame implements NceListener {

	public NceMonFrame() {
		super();
	}

	protected String title() { return "NCE Command Monitor"; }
	
	protected void init() {
		// connect to TrafficController
		NceTrafficController.instance().addNceListener(this);
	}
  
	public void dispose() {
		NceTrafficController.instance().removeNceListener(this);
	}
			
	public synchronized void message(NceMessage l) {  // receive a message and log it
        if (l.isBinary())
          	nextLine("binary cmd: "+l.toString()+"\n", null);
        else
            nextLine("cmd: \""+l.toString()+"\"\n", null);

	}

	public synchronized void reply(NceReply l) {  // receive a reply message and log it
	    String raw = "";
	    for (int i=0;i<l.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i)&0xFF, raw);
        }
	        
        nextLine("rep: \""+l.toString()+"\"\n", raw);
	}
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMonFrame.class.getName());

}

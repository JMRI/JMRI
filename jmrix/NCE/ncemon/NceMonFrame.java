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
		// NceMessage rawMsg = new NceMessage(l);
		// rawMsg.setBinary();
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(NceReply l) {  // receive a reply message and log it
		NceReply rawMsg = new NceReply(l);
		rawMsg.setBinary(true);
		nextLine("rep: \""+l.toString()+"\"\n", "raw: \""+rawMsg.toString()+"\"\n");
	}
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMonFrame.class.getName());

}

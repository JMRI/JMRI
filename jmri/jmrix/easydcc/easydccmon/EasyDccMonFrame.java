/** 
 * EasyDccMonFrame.java
 *
 * Description:		Frame displaying (and logging) EasyDcc command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: EasyDccMonFrame.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc.easydccmon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;

public class EasyDccMonFrame extends jmri.jmrix.AbstractMonFrame implements EasyDccListener {

	public EasyDccMonFrame() {
		super();
	}

	protected String title() { return "EasyDcc Command Monitor"; }
	
	protected void init() {
		// connect to TrafficController
		EasyDccTrafficController.instance().addEasyDccListener(this);
	}
  
	public void dispose() {
		EasyDccTrafficController.instance().removeEasyDccListener(this);
	}
			
	public synchronized void message(EasyDccMessage l) {  // receive a message and log it
		// EasyDccMessage rawMsg = new EasyDccMessage(l);
		// rawMsg.setBinary();
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(EasyDccReply l) {  // receive a reply message and log it
		EasyDccReply rawMsg = new EasyDccReply(l);
		rawMsg.setBinary(true);
		nextLine("rep: \""+l.toString()+"\"\n", "raw: \""+rawMsg.toString()+"\"\n");
	}
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccMonFrame.class.getName());

}

/**
 * EasyDccMonFrame.java
 *
 * Description:		Frame displaying (and logging) EasyDcc command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: EasyDccMonFrame.java,v 1.2 2002-03-30 19:22:53 jacobsen Exp $
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
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(EasyDccReply l) {  // receive a reply message and log it
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccMonFrame.class.getName());

}

/**
 * SprogMonFrame.java
 *
 * Description:		Frame displaying (and logging) Sprog command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SprogMonFrame.java,v 1.1 2003-01-27 05:35:40 jacobsen Exp $
 */

package jmri.jmrix.sprog.sprogmon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;

public class SprogMonFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener {

	public SprogMonFrame() {
		super();
	}

	protected String title() { return "Sprog Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		SprogTrafficController.instance().addSprogListener(this);
	}

	public void dispose() {
		SprogTrafficController.instance().removeSprogListener(this);
	}

	public synchronized void message(SprogMessage l) {  // receive a message and log it
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(SprogReply l) {  // receive a reply message and log it
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMonFrame.class.getName());

}

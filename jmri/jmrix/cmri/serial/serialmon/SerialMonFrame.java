/**
 * SerialMonFrame.java
 *
 * Description:		Frame displaying (and logging) CMRI serial command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version
 */

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
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(SerialReply l) {  // receive a reply message and log it
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}

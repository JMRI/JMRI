// Mx1MonFrame.java

package jmri.jmrix.zimo.zimomon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.zimo.Mx1Listener;
import jmri.jmrix.zimo.Mx1TrafficController;
import jmri.jmrix.zimo.Mx1Message;

/**
 * Frame displaying (and logging) MX-1 messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version         $Revision: 1.1 $
 *
 * Adapted by Sip Bosch for use with MX-1
 */
 public class Mx1MonFrame extends jmri.jmrix.AbstractMonFrame implements Mx1Listener {

	public Mx1MonFrame() {
		super();
	}

	protected String title() { return "MX-1 Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		Mx1TrafficController.instance().removeMx1Listener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		Mx1TrafficController.instance().addMx1Listener(~0, this);
	}

	public synchronized void message(Mx1Message l) {  // receive a MX-1 message and log it
		// display the raw data if requested
		String raw = "packet: ";
		if ( rawCheckBox.isSelected() ) {
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				raw += Integer.toHexString(l.getElement(i))+" ";
			raw+="\n";
		}

		// display the decoded data
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(l.toString()+"\n", raw);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Mx1MonFrame.class.getName());

}

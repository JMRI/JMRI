/**
 * XNetMonFrame.java
 *
 * Description:		Frame displaying (and logging) XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version         $Revision: 1.1 $
 */

package jmri.jmrix.lenz.mon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetMessage;

public class XNetMonFrame extends jmri.jmrix.AbstractMonFrame implements XNetListener {

	public XNetMonFrame() {
		super();
	}

	protected String title() { return "XpressNet Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		XNetTrafficController.instance().removeXNetListener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		XNetTrafficController.instance().addXNetListener(~0, this);
	}

	public synchronized void message(XNetMessage l) {  // receive a XpressNet message and log it
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
		nextLine(l.toString(), raw);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMonFrame.class.getName());

}

// QsiMonFrame.java

package jmri.jmrix.qsi.qsimon;

import jmri.jmrix.qsi.QsiListener;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;
import jmri.jmrix.qsi.QsiTrafficController;

/**
 * Frame displaying (and logging) QSI command messages
 * @author			Bob Jacobsen   Copyright (C) 2007, 2008
 * @version			$Revision: 1.2 $
 */
public class QsiMonFrame extends jmri.jmrix.AbstractMonFrame implements QsiListener {

	public QsiMonFrame() {
		super();
	}

	protected String title() { return "QSI Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		QsiTrafficController.instance().addQsiListener(this);
	}

	public void dispose() {
		QsiTrafficController.instance().removeQsiListener(this);
	}

	public synchronized void message(QsiMessage l) {  // receive a message and log it
	    int opcode = l.getElement(0);
		nextLine("cmd: \""+opcode+"\"\n", l.toString());
	}
	public synchronized void reply(QsiReply l) {  // receive a reply message and log it
	    int opcode = l.getElement(0);
		nextLine("rep: \""+opcode+"\"\n", l.toString());
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(QsiMonFrame.class.getName());

}

// AcelaMonFrame.java

package jmri.jmrix.acela.acelamon;

import org.apache.log4j.Logger;
import jmri.jmrix.acela.AcelaListener;
import jmri.jmrix.acela.AcelaMessage;
import jmri.jmrix.acela.AcelaReply;
import jmri.jmrix.acela.AcelaTrafficController;

/**
 * Frame displaying (and logging) Acela command messages
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaMonFrame extends jmri.jmrix.AbstractMonFrame implements AcelaListener {

	public AcelaMonFrame() {
		super();
	}

	protected String title() { return "Acela Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		AcelaTrafficController.instance().addAcelaListener(this);
	}

	public void dispose() {
		AcelaTrafficController.instance().removeAcelaListener(this);
        super.dispose();
	}

	public synchronized void message(AcelaMessage l) {  // receive a message and log it
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(AcelaReply l) {  // receive a reply message and log it
		l.setBinary(true);
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static Logger log = Logger.getLogger(AcelaMonFrame.class.getName());
}

/* @(#)AcelaMonFrame.java */

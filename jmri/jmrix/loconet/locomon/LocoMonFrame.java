/**
 * LocoMonFrame.java
 *
 * Description:		Frame displaying (and logging) LocoNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.*;

public class LocoMonFrame extends jmri.jmrix.AbstractMonFrame implements LocoNetListener {

	public LocoMonFrame() {
		super();
	}

	protected String title() { return "LocoNet Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		LnTrafficController.instance().removeLocoNetListener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the LnTrafficController
		LnTrafficController.instance().addLocoNetListener(~0, this);
	}

	public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
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
		nextLine(llnmon.displayMessage(l), raw);

	}

	jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoMonFrame.class.getName());

}

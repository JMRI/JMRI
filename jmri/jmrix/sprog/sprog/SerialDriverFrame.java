// SerialDriverFrame.java

package jmri.jmrix.sprog.sprog;


/**
 * Frame to control and connect Sprog command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */
@Deprecated
public class SerialDriverFrame extends jmri.jmrix.sprog.serialdriver.SerialDriverFrame {


	public SerialDriverFrame() {
		super("Open Sprog connection");
		adapter = new jmri.jmrix.sprog.serialdriver.SerialDriverAdapter();
	}

}

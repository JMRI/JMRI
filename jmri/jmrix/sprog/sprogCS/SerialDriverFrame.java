// SerialDriverFrame.java

package jmri.jmrix.sprog.sprogCS;

/**
 * Frame to control and connect Sprog command station via SerialDriver interface and comm port
 * @author			Andrew Crosland   Copyright (C) 2006
 * @version			$Revision: 1.3 $
 */
public class SerialDriverFrame extends jmri.jmrix.sprog.serialdriver.SerialDriverFrame {

	public SerialDriverFrame() {
		super("Open Sprog Command Station connection");
		adapter = new SprogCSSerialDriverAdapter();
	}

}

/* @(#)SerialdriverFrame.java */


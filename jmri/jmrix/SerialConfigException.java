// SerialConfigException.java


package jmri.jmrix;

/** 
 * Represents a failure during the configuration of a serial 
 * port, typically via a SerialPortAdapter interface.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: SerialConfigException.java,v 1.1 2002-03-01 00:02:44 jacobsen Exp $
 */
public class SerialConfigException extends jmri.JmriException {
	public SerialConfigException(String s) { super(s); }
	public SerialConfigException() {}
	
}


/* @(#)SerialConfigException.java */

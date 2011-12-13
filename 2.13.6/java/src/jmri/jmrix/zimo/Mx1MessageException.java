// Mx1MessageException.java

package jmri.jmrix.zimo;

import jmri.JmriException;

/**
 * Zimo-specfic exceptions.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision$
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 */
public class Mx1MessageException extends JmriException {
	public Mx1MessageException(String s) { super(s); }
	public Mx1MessageException() {}
}


/* @(#)Mx1MessageException.java */

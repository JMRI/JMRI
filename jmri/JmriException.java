// JmriException.java

package jmri;

/**
 * Base for JMRI-specific exceptions. No functionality, 
 * just used to confirm type-safety.
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @version			$Revision: 1.5 $
 */
public class JmriException extends Exception {
	public JmriException(String s, Throwable t) { super(s, t); }
	public JmriException(String s) { super(s); }
	public JmriException() {}

}

/* @(#)JmriException.java */

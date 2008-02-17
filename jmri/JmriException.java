// JmriException.java

package jmri;

/**
 * Base for JMRI-specific exceptions.
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @version			$Revision: 1.4 $
 */
public class JmriException extends Exception {
	public JmriException(String s) { super(s); }
	public JmriException() {}

}

/* @(#)JmriException.java */

// JMRIClientPortController.java

package jmri.jmrix.jmriclient;

/**
 * Abstract base for classes representing a JMRIClient communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008, 2010
 * @author			Paul Bender     Copyright (C) 2010
 * @version			$Revision$
 */
public abstract class JMRIClientPortController extends jmri.jmrix.AbstractNetworkPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to JMRIClientTrafficController classes, who in turn will deal in messages.
    protected JMRIClientSystemConnectionMemo adaptermemo = null;
}


/* @(#)JMRIClientPortController.java */

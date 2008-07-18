// PortAdapter.java

package jmri.jmrix;

/**
 * Enables basic setup of a interface
 * for a jmrix implementation.
 *<P>
 * This has no e.g. serial-specific information.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @version	$Revision: 1.1 $
 * @see         jmri.jmrix.SerialConfigException
 * @since 2.3.1
 */
public interface PortAdapter  {

	/** Configure all of the other jmrix widgets needed to work with this adapter
	 */
	public void configure();

	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
	public boolean status();

}

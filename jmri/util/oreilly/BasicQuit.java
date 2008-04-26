/*   BasicQuit.java
 *
 *		This class handles operations needed when a JMRI application quits
 *
 *      Adopted from Apple Java template
 *
 * @author      Dave Duchamp Copyright (C) 2008
 * @version     $Revision: 1.2 $
 */

package jmri.util.oreilly;

public class BasicQuit {
	
	public BasicQuit() {}

	/**
	 * Performs tasks prior to exit, then exits.
	 * Here add call to methods to perform exit tasks.
	 */
	public static void handleQuit() {
		// Save block values prior to exit, if necessary
		try {
			new jmri.jmrit.display.BlockValueFile().writeBlockValues();
		} 
		catch (org.jdom.JDOMException jde) {}				
		catch (java.io.IOException ioe) {}	
		System.exit(0);
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BasicWindowMonitor.class.getName());
}

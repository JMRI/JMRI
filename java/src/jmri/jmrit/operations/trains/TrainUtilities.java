// TrainPrintUtilities

package jmri.jmrit.operations.trains;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Train print utilities
 * @author Daniel Boudreau (C) 2010
 * @version $Revision: 20668 $
 *
 */
public class TrainUtilities {
		
	/**
	 * This method uses Desktop which is supported in Java 1.6.
	 */
	public static void openDesktop (File file){
		if (!java.awt.Desktop.isDesktopSupported()) {
			log.warn("desktop not supported");
			return;
		}
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		if (!desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
			log.warn("desktop open not supported");
			return;
		}
		try {
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static Logger log = org.apache.log4j.Logger
	.getLogger(TrainUtilities.class.getName());
}

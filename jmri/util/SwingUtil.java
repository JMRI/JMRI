package jmri.util;

import javax.swing.JComponent;

/**
 * Common utility methods for working with Swing.
 * <P>
 * We needed a place to put code to Java 2 Swing functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 *
 * @author Alex Shepherd  Copyright 2004
 * @version $Revision: 1.1 $
 */

public class SwingUtil {

	static public boolean setFocusable( JComponent component, boolean focusable ) {
	try {
		component.setFocusable( focusable) ;
		return true ;
	} catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
		// just carry on with original fonts
		return false;
	}
}


}
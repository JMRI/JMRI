// PreferencesHandler.java
package jmri.plaf.macosx;

import java.util.EventObject;

/**
 * Handle a trigger to launch the application about dialog from Mac OS X.
 *
 * @author rhwood
 */
public interface AboutHandler {
    
    abstract public void handleAbout(EventObject eo);

}

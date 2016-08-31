package jmri.plaf.macosx;

import java.util.EventObject;

/**
 * Handle a trigger to launch the application preferences from Mac OS X.
 *
 * @author Randall Wood (c) 2011
 */
public interface PreferencesHandler {

    abstract public void handlePreferences(EventObject eo);

}

package jmri.plaf.macosx;

import java.util.EventObject;

/**
 * A QuitHandler responds to externally initiated attempts to quit JMRI apps on
 * Mac OS X.
 *
 * @author Randall Wood (c) 2011
 */
public interface QuitHandler {

    /**
     * Handle the quit request using whatever means are most appropriate. This
     * method returns a boolean state indicating that the application should
     * quit (true) or that some condition (like the user cancels the quit when
     * prompted in a dialog) requires that the application not quit (false).
     *
     * @param eo The quit event object.
     * @return allow quit to continue.
     */
    abstract public boolean handleQuitRequest(EventObject eo);

}

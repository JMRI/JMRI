package jmri.plaf.macosx;

import java.util.EventObject;

/**
 * Handle a trigger to launch the application about dialog from Mac OS X.
 *
 * @author Randall Wood (c) 2011
 * @deprecated since 4.21.1; use {@link apps.plaf.macosx.AboutHandler} instead
 */
@Deprecated
public interface AboutHandler {

    abstract public void handleAbout(EventObject eo);

}

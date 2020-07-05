package jmri.plaf.macosx;

import java.util.EventObject;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Handle a trigger to launch the application about dialog from Mac OS X.
 *
 * @author Randall Wood (c) 2011
 * @deprecated since 4.21.1; use {@link apps.plaf.macosx.AboutHandler} instead
 */
@Deprecated
@API(status = EXPERIMENTAL)
public interface AboutHandler {

    abstract public void handleAbout(EventObject eo);

}

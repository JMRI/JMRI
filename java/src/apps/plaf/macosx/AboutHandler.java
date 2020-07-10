package apps.plaf.macosx;

import java.util.EventObject;

import org.apiguardian.api.API;

/**
 * Handle a trigger to launch the application about dialog from Mac OS X.
 *
 * @author Randall Wood (c) 2011
 */
@API(status=API.Status.MAINTAINED)
public interface AboutHandler {

    abstract public void handleAbout(EventObject eo);

}

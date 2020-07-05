package jmri.plaf.macosx;

import java.util.EventObject;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Handle a trigger to launch the application preferences from Mac OS X.
 *
 * @author Randall Wood (c) 2011
 * @deprecated since 4.21.1; use {@link apps.plaf.macosx.PreferencesHandler} instead
 */
@Deprecated
@API(status = EXPERIMENTAL)
public interface PreferencesHandler {

    abstract public void handlePreferences(EventObject eo);

}

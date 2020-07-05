package jmri.util.prefs;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * This exception represents an exception thrown while attempting to initialize
 * a PreferencesProvider.
 *
 * @author Randall Wood (C) 2015
 */
@API(status = EXPERIMENTAL)
public class InitializationException extends Exception {

    private String localizedMessage = null;

    public InitializationException(String message, String localized) {
        this(message, localized, null);
    }

    public InitializationException(String message, String localized, Throwable cause) {
        super(message, cause);
        this.localizedMessage = localized;
    }

    public InitializationException(Throwable cause) {
        super(cause);
        this.localizedMessage = cause.getLocalizedMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return (this.localizedMessage == null) ? this.getMessage() : this.localizedMessage;
    }
}

package jmri.util.prefs;

/**
 * This exception represents an exception thrown while attempting to initialize
 * a PreferencesProvider.
 *
 * @author Randall Wood (C) 2015
 */
public class InitializationException extends Exception {

    private static final long serialVersionUID = 1L;
    private String localizedMessage = null;

    public InitializationException(String message, String localized) {
        this(message, localized, null);
    }

    public InitializationException(String message, String localized, Throwable cause) {
        super(message, cause);
        this.localizedMessage = localized;
    }

    @Override
    public String getLocalizedMessage() {
        return (this.localizedMessage == null) ? this.getMessage() : this.localizedMessage;
    }
}

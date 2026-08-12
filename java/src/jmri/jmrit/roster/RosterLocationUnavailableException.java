package jmri.jmrit.roster;

import javax.annotation.Nonnull;

import jmri.util.prefs.InitializationException;

/**
 * An exception thrown when the configured roster location is not available
 * at startup, for example because the user removed a USB drive that holds
 * the roster directory. Recognized by
 * {@link jmri.implementation.JmriConfigurationManager} so that, in a
 * non-headless application, the user can be offered Continue or Quit before
 * the broader initialization-error dialog is shown.
 *
 * @author Chad Francis (C) 2026
 */
public class RosterLocationUnavailableException extends InitializationException {

    private final String unavailablePath;

    public RosterLocationUnavailableException(String message, String localized,
            @Nonnull String unavailablePath, Throwable cause) {
        super(message, localized, cause);
        this.unavailablePath = unavailablePath;
    }

    @Nonnull
    public String getUnavailablePath() {
        return unavailablePath;
    }
}

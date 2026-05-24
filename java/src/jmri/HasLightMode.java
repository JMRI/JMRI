package jmri;

/**
 * Interface for Lights that support selectable output modes.
 * <p>
 * Implementing classes expose a fixed set of named modes and allow the active
 * mode to be read and changed at runtime.  The mode determines which protocol
 * command is sent to the hardware when the Light state changes.
 * <p>
 * The API mirrors the Turnout feedback-mode API so that table models and
 * persistence code can treat both uniformly.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public interface HasLightMode {

    /** @return the current mode as an implementation-defined integer constant */
    int getMode();

    /** @return the display name of the current mode */
    String getModeName();

    /** @return all valid mode display names for this Light */
    String[] getValidModeNames();

    /**
     * Set the active mode by display name.
     *
     * @param modeName one of the values returned by {@link #getValidModeNames()}
     */
    void setModeByName(String modeName);

}

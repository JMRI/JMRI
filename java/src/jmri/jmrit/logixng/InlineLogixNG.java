package jmri.jmrit.logixng;

/**
 * This interface declares an Inline LogixNG.
 * @author Daniel Bergqvist (C) 2022
 */
public interface InlineLogixNG {

    /**
     * Get the LogixNG of this InlineLogixNG.
     * @return the LogixNG or null if it has no LogixNG
     */
    LogixNG getLogixNG();

    /**
     * Set the LogixNG of this InlineLogixNG.
     * @param logixNG the LogixNG or null if remove the LogixNG from the InlineLogixNG
     */
    void setLogixNG(LogixNG logixNG);

    /**
     * Set the system name for the LogixNG of this InlineLogixNG.
     * @param systemName the system name
     */
    void setLogixNG_SystemName(String systemName);

    /**
     * Setup the LogixNG of this InlineLogixNG.
     */
    void setupLogixNG();

    /**
     * Get the name of this InlineLogixNG.
     * @return the name
     */
    String getNameString();

    /**
     * Get the LogixNG of this InlineLogixNG.
     * @return the LogixNG or null if it has no LogixNG
     */
    String getEditorName();

    /**
     * Get the type of item of this InlineLogixNG.
     * @return the type
     */
    String getTypeName();

    /**
     * Get the X position of this InlineLogixNG.
     * @return the X position
     */
    int getX();

    /**
     * Get the Y position of this InlineLogixNG.
     * @return the Y position
     */
    int getY();

}

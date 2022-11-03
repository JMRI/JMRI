package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * This interface declares an Inline LogixNG.
 * @author Daniel Bergqvist (C) 2022
 */
public interface InlineLogixNG {

    /**
     * Get the LogixNG of this InlineLogixNG.
     * @return the LogixNG or null if it has no LogixNG
     */
    public LogixNG getLogixNG();

    /**
     * Set the LogixNG of this InlineLogixNG.
     * @param logixNG the LogixNG or null if remove the LogixNG from the InlineLogixNG
     */
    public void setLogixNG(LogixNG logixNG);

    /**
     * Get the name of this InlineLogixNG.
     * @return the name
     */
    public String getNameString();

    /**
     * Get the LogixNG of this InlineLogixNG.
     * @return the LogixNG or null if it has no LogixNG
     */
    public String getEditorName();

    /**
     * Get the NamedBean of this InlineLogixNG.
     * @return the NamedBean or null if it has no NamedBean
     */
    public NamedBean getNamedBean();

    /**
     * Get the X position of this InlineLogixNG.
     * @return the X position
     */
    public int getX();

    /**
     * Get the Y position of this InlineLogixNG.
     * @return the Y position
     */
    public int getY();

}

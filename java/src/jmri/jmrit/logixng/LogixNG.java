package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * LogixNG.
 *
 * @author Daniel Bergqvist Copyright 2018
 * @author Dave Sand        Copyright 2021
 */
public interface LogixNG extends Base, NamedBean {

    String PROPERTY_INLINE = "IsInline";

    /**
     * Clear the startup flag.
     */
    void clearStartup();

    /**
     * Determines whether this LogixNG is currently during startup.
     *
     * @return true if the LogixNG is currently during startup, false otherwise
     */
    public boolean isStartup();

    /**
     * Sets whether this LogixNG is inline or not.
     *
     * @param inline true if the LogixNG is inline, false otherwise
     */
    void setInline(boolean inline);

    /**
     * Determines whether this LogixNG is inline or not.
     *
     * @return true if the LogixNG is inline, false otherwise
     */
    boolean isInline();

    /**
     * Set the InlineLogixNG that owns this LogixNG, if the LogixNG is inline.
     *
     * @param inlineLogixNG the InlineLogixNG that owns this LogixNG, if the LogixNG is inline.
     */
    void setInlineLogixNG(InlineLogixNG inlineLogixNG);

    /**
     * Get the InlineLogixNG that owns this LogixNG, if the LogixNG is inline.
     *
     * @return the InlineLogixNG
     */
    InlineLogixNG getInlineLogixNG();

    /**
     * Set whenether this LogixNG is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners() and
     * also call execute() if enable is true.
     *
     * @param enable true if this LogixNG should be enabled, false otherwise
     */
    void setEnabled(boolean enable);

    /**
     * Determines whether this LogixNG is enabled.
     *
     * @return true if the LogixNG is enabled, false otherwise
     */
    @Override
    boolean isEnabled();

    /**
     * Activates this LogixNG.
     * <P>
     * This method is called by the LogixNG manager during
     * initialization of the LogixNGs.
     */
    void activate();

    /**
     * Activates/deactivates this LogixNG.
     * <P>
     * This method is used by the LogixNG action EnableLogixNG to temporary
     * activate or deactivate a LogixNG.
     * @param active true if activate, false if deactivate
     */
    public void setActive(boolean active);

    /**
     * Set the system name for the conditionalNG at the specified position in this list
     * @param index index of the element to set the system name
     * @return the system name
     */
    String getConditionalNG_SystemName(int index);

    /**
     * Set the system name for the conditionalNG at the specified position in this list
     * @param index index of the element to set the system name
     * @param systemName the new system name
     */
    void setConditionalNG_SystemName(int index, String systemName);

    /**
     * Get number of ConditionalNGs for this LogixNG.
     *
     * @return the number of conditionals
     */
    int getNumConditionalNGs();

    /**
     * Move 'row' to 'nextInOrder' and shift all between 'nextInOrder' and 'row'
     * up one position. Requires {@code row > nextInOrder}.
     *
     * @param nextInOrder target order for ConditionalNG at row
     * @param row         position of ConditionalNG to move
     */
    void swapConditionalNG(int nextInOrder, int row);

    /**
     * Returns the conditionalNG that will calculate in the specified order.
     * This is also the order the ConditionalNG is listed in the
     * Add/Edit LogixNG dialog. If 'order' is greater than the number of
     * ConditionalNGs for this LogixNG, null is returned.
     *
     * @param order order in which the ConditionalNG calculates
     * @return the conditionalNG or null
     */
    ConditionalNG getConditionalNG(int order);

    /**
     * Add a child ConditionalNG to the parent LogixNG.
     * <p>
     * The first part handles adding conditionalNGs to the LogixNG list
     * during file loading.
     * <p>
     * The second part handles normal additions using the GUI, Logix imports or tests.
     *
     * @param conditionalNG The ConditionalNG object.
     * @return true if the ConditionalNG was added, false otherwise.
     */
    boolean addConditionalNG(ConditionalNG conditionalNG);

    /**
     * Get a ConditionalNG belonging to this LogixNG.
     *
     * @param systemName The name of the ConditionalNG object.
     * @return the ConditionalNG object or null if not found.
     */
    ConditionalNG getConditionalNG(String systemName);

    /**
     * Get a ConditionalNG belonging to this LogixNG.
     *
     * @param userName The name of the ConditionalNG object.
     * @return the ConditionalNG object or null if not found.
     */
    ConditionalNG getConditionalNGByUserName(String userName);

    /**
     * Delete a ConditionalNG from this LogixNG.
     * <p>
     * Note: Since each LogixNG must have at least one ConditionalNG, the last
     * ConditionalNG will not be deleted.
     * <p>
     * Returns An array of names used in an error message explaining why
     * ConditionalNG should not be deleted.
     *
     * @param conditionalNG The ConditionalNG to delete
     */
    void deleteConditionalNG(ConditionalNG conditionalNG);

    /**
     * Is this item active?
     * This method returns true if the the LogixNG is active but not enabled,
     * while the method {@link #isActive() } only returns true if the LogixNG
     * is both active and enabled.
     * @return true if active, false otherwise.
     */
    boolean isActivated();

    /**
     * Execute all ConditionalNGs if the LogixNG is enabled and activated.
     */
    void execute();

    /**
     * Execute all ConditionalNGs if the LogixNG is enabled and activated.
     * @param allowRunDelayed true if it's ok to run delayed, false otherwise
     */
    void execute(boolean allowRunDelayed);

    /**
     * Execute all ConditionalNGs if the LogixNG is enabled and activated.
     * @param allowRunDelayed true if it's ok to run delayed, false otherwise
     * @param isStartup true if startup, false otherwise
     */
    void execute(boolean allowRunDelayed, boolean isStartup);

}

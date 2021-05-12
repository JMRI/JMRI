package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * LogixNG.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixNG extends Base, NamedBean {

    /**
     * Set whenether this LogixNG is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners() and
     * also call execute() if enable is true.
     * 
     * @param enable true if this LogixNG should be enabled, false otherwise
     */
    public void setEnabled(boolean enable);
    
    /**
     * Determines whether this LogixNG is enabled.
     * 
     * @return true if the LogixNG is enabled, false otherwise
     */
    @Override
    public boolean isEnabled();
    
    /**
     * Set the system name for the conditionalNG at the specified position in this list
     * @param index index of the element to set the system name
     * @return the system name
     */
    public String getConditionalNG_SystemName(int index);
    
    /**
     * Set the system name for the conditionalNG at the specified position in this list
     * @param index index of the element to set the system name
     * @param systemName the new system name
     */
    public void setConditionalNG_SystemName(int index, String systemName);
    
    /**
     * Get number of ConditionalNGs for this LogixNG.
     *
     * @return the number of conditionals
     */
    public int getNumConditionalNGs();

    /**
     * Move 'row' to 'nextInOrder' and shift all between 'nextInOrder' and 'row'
     * up one position. Requires {@code row > nextInOrder}.
     *
     * @param nextInOrder target order for ConditionalNG at row
     * @param row         position of ConditionalNG to move
     */
    public void swapConditionalNG(int nextInOrder, int row);

    /**
     * Returns the conditionalNG that will calculate in the specified order.
     * This is also the order the ConditionalNG is listed in the
     * Add/Edit LogixNG dialog. If 'order' is greater than the number of
     * ConditionalNGs for this LogixNG, null is returned.
     *
     * @param order order in which the ConditionalNG calculates
     * @return the conditionalNG or null
     */
    public ConditionalNG getConditionalNG(int order);

    /**
     * Add a child ConditionalNG to the parent LogixNG.
     *
     * @param conditionalNG The ConditionalNG object.
     * @return true if the ConditionalNG was added, false otherwise.
     */
    public boolean addConditionalNG(ConditionalNG conditionalNG);

    /**
     * Get a ConditionalNG belonging to this LogixNG.
     *
     * @param systemName The name of the ConditionalNG object.
     * @return the ConditionalNG object or null if not found.
     */
    public ConditionalNG getConditionalNG(String systemName);

    /**
     * Get a ConditionalNG belonging to this LogixNG.
     *
     * @param userName The name of the ConditionalNG object.
     * @return the ConditionalNG object or null if not found.
     */
    public ConditionalNG getConditionalNGByUserName(String userName);

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
    public void deleteConditionalNG(ConditionalNG conditionalNG);

    /**
     * Execute all ConditionalNGs if the LogixNG is enabled and activated.
     */
    public void execute();

    /**
     * Execute all ConditionalNGs if the LogixNG is enabled and activated.
     * @param allowRunDelayed true if it's ok to run delayed, false otherwise
     */
    public void execute(boolean allowRunDelayed);

}

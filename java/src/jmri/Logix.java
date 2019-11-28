package jmri;

/**
 * A Logix is a group of Conditionals that monitor one or more conditions
 * (internal or on the layout). It services these Conditionals by installing and
 * deinstalling the proper listeners for their variables.
 * <p>
 * A Logix can be enabled or not. It passes this attribute to its Conditionals.
 * By default it is enabled. When not enabled, a Conditional will still respond
 * to callbacks from its listeners and calculate its state, however it will not
 * execute its actions. Enabled is a bound property of a Logix.
 * <p>
 * A Logix can be deactivated or not. When deactivated, the listeners of the
 * Conditional variables are deinstalled.
 * <p>
 * A Logix does not have a "state", however, each of its Conditionals does.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Additional modifications Pete Cressman 2009
 */
public interface Logix extends NamedBean {

    public static final int LISTENER_TYPE_SENSOR = 1;
    public static final int LISTENER_TYPE_TURNOUT = 2;
    public static final int LISTENER_TYPE_LIGHT = 3;
    public static final int LISTENER_TYPE_CONDITIONAL = 4;
    public static final int LISTENER_TYPE_SIGNALHEAD = 5;
    public static final int LISTENER_TYPE_MEMORY = 6;
    public static final int LISTENER_TYPE_FASTCLOCK = 7;
    public static final int LISTENER_TYPE_WARRANT = 8;
    public static final int LISTENER_TYPE_SIGNALMAST = 9;
    public static final int LISTENER_TYPE_OBLOCK = 10;
    public static final int LISTENER_TYPE_ENTRYEXIT = 11;

    /**
     * Set enabled status. Enabled is a bound property All conditionals are set
     * to UNKNOWN state and recalculated when the Logix is enabled, provided the
     * Logix has been previously activated.
     *
     * @param state true if Logix should be enabled; false otherwise
     */
    public void setEnabled(boolean state);

    /**
     * Get enabled status.
     *
     * @return true if enabled; false otherwise
     */
    public boolean getEnabled();

    /**
     * Get number of Conditionals for this Logix.
     *
     * @return the number of conditionals
     */
    public int getNumConditionals();

    /**
     * Move 'row' to 'nextInOrder' and shift all between 'nextInOrder' and 'row'
     * up one position. Requires {@code row > nextInOrder}.
     *
     * @param nextInOrder target order for Conditional at row
     * @param row         position of Conditional to move
     */
    public void swapConditional(int nextInOrder, int row);

    /**
     * Returns the system name of the conditional that will calculate in the
     * specified order. This is also the order the Conditional is listed in the
     * Add/Edit Logix dialog. If 'order' is greater than the number of
     * Conditionals for this Logix, and empty String is returned.
     *
     * @param order order in which the Conditional calculates
     * @return system name of conditional or an empty String
     */
    public String getConditionalByNumberOrder(int order);

    /**
     * Add a Conditional name and sequence number to this Logix.
     *
     * @param systemName The Conditional system name
     * @param order      the order this conditional should calculate in if
     *                   order is negative, the conditional is added at the end
     *                   of current group of conditionals
     * @return true if the Conditional was added, false otherwise (most likely
     *         false indicates that maximum number of Conditionals was exceeded)
     */
    public boolean addConditional(String systemName, int order);

    /**
     * Add a child Conditional to the parent Logix.
     *
     * @since 4.7.4
     * @param systemName The system name for the Conditional object.
     * @param conditional The Conditional object.
     * @return true if the Conditional was added, false otherwise.
     */
    public boolean addConditional(String systemName, Conditional conditional);

    /**
     * Get a Conditional belonging to this Logix.
     *
     * @since 4.7.4
     * @param systemName The name of the Conditional object.
     * @return the Conditional object or null if not found.
     */
    public Conditional getConditional(String systemName);

    /**
     * Delete a Conditional from this Logix.
     * <p>
     * Note: Since each Logix must have at least one Conditional, the last
     * Conditional will not be deleted.
     * <p>
     * Returns An array of names used in an error message explaining why
     * Conditional should not be deleted.
     *
     * @param systemName The Conditional system name
     * @return names of objects blocking deletion or null; note that null does
     *         not exclusively indicate successful deletion
     */
    public String[] deleteConditional(String systemName);

    /**
     * Calculate all Conditionals, triggering action if the user specified
     * conditions are met, and the Logix is enabled.
     */
    public void calculateConditionals();

    /**
     * Activate the Logix, starts Logix processing by connecting all inputs that
     * are included the Conditionals in this Logix.
     * <p>
     * A Logix must be activated before it will calculate any of its
     * Conditionals.
     */
    public void activateLogix();

    /**
     * Deactivate the Logix. This method disconnects the Logix from all input
     * objects and stops it from being triggered to calculate.
     * <p>
     * A Logix must be deactivated before its Conditionals are changed.
     */
    public void deActivateLogix();

    /**
     * ConditionalVariables only have a single name field.  For user interface purposes
     * a gui name is used for the referenced conditional user name.  This is not used
     * for other object types.
     * <p>
     * In addition to setting the GUI name, any state variable references are changed to
     * conditional system names.  This converts the XML system/user name field to the system name
     * for conditional references.  It does not affect other objects such as sensors, turnouts, etc.
     * @since 4.7.4
     */
    public void setGuiNames();

    /**
     * Assemble a list of state variables that both trigger the Logix, and are
     * changed by it. Returns true if any such variables were found. Returns
     * false otherwise.
     */
    //public boolean checkLoopCondition();
    /**
     * Assembles a string listing state variables that might result in a loop.
     * Returns an empty string if there are none, probably because
     * "checkLoopConditioncheckLoopCondition" was not invoked before the call,
     * or returned false.
     */
    //public ArrayList <String[]> getLoopGremlins();
    /**
     * Assembles and returns a list of state variables that are used by
     * conditionals of this Logix including the number of occurances of each
     * variable that trigger a calculation, and the number of occurances where
     * the triggering has been suppressed. The main use of this method is to
     * return information that can be used to test for inconsistency in
     * suppressing triggering of a calculation among multiple occurances of the
     * same state variable. Caller provides an ArrayList of the variables to
     * check and an empty Array list to return the counts for triggering or
     * suppressing calculation. The first index is a count that the
     * correspondeing variable triggers calculation and second is a count that
     * the correspondeing variable suppresses Calculation. Note this method must
     * not modify the supplied variable list in any way.
     */
    //public void getStateVariableList(ArrayList <ConditionalVariable> varList, ArrayList <int[]> triggerPair);
    
}

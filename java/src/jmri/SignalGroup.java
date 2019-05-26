package jmri;

/**
 * Signal Groups are used to represent European subsidary signals that would
 * be sited with a signal mast. Such subsidary signals would be used to
 * indicated routes, junctions and allowable speeds. Each such
 * route/junction/speed would be represented by a single output signal (head),
 * that is either Off or On. Within the group only one such signal head would be
 * allowed on at any one time.
 * <p>
 * The group is attached to a main signal mast, and can be configured to be
 * activated depending upon one or more aspects when displayed on that signal
 * mast.
 * <p>
 * Each signal head within the group is defined with an On and Off appearance,
 * and a set of criteria in the form of matching turnouts and sensor states,
 * that must be met for the head to be set On.
 * <p>
 * For code clarity, JMRI uses the terms (signal)Mast and (signal)Head instead
 * of Signal. Masts show Aspects, Heads show Appearances, though outside the USA
 * this will vary. Use localization to address this in the user interface.
 *
 * @see jmri.implementation.DefaultSignalGroup DefaultSignalGroup
 *
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2017
 */
public interface SignalGroup extends NamedBean {

    /**
     * Set enabled status of the signal group.
     *
     * @param boo true if signal group should be enabled; false otherwise
     */
    public void setEnabled(boolean boo);

    /**
     * Get enabled status of the signal group.
     *
     * @return true if signal group is enabled; false otherwise
     */
    public boolean getEnabled();

    /**
     * Set the main Signal Mast for the Group by name.
     *
     * @param mastName Name of the (existing) Signal Mast to set as main mast
     *                 for the group
     */
    public void setSignalMast(String mastName);

    /**
     * Set the main Signal Mast for the Group.
     *
     * @param signalMast Mast object to attach
     * @param mastName   Name of the (existing) Signal Mast to set as main mast
     *                   for the group
     */
    public void setSignalMast(SignalMast signalMast, String mastName);

    /**
     * Get the name of the main Signal Mast in a group.
     *
     * @return Name of the mast as string
     */
    public String getSignalMastName();

    /**
     * Get the main Signal Mast in a group.
     *
     * @return The main Signal Mast as bean
     */
    public SignalMast getSignalMast();

    /**
     * Clear the list of SignalMast Aspects that may trigger the group. Causes
     * the Aspect list to be rebuilt next time it is called
     */
    public void clearSignalMastAspect();

    /**
     * Add an Aspect that can trigger the group activation.
     *
     * @param aspect Name of an aspect for the Main Signal Mast in the Group,
     *               must be valid for the Mast type
     */
    public void addSignalMastAspect(String aspect);

    /**
     * Get the total number of Signal Mast Aspects available in this group.
     *
     * @return the number of Aspects on the main Signal Mast, null if no Main
     *         Mast is assigned
     */
    public int getNumSignalMastAspects();

    /**
     * Get a SignalMast Aspect for the Main Signal Mast by its Index.
     *
     * @param x index of the Signal Mast Aspect in the list
     * @return the aspect or null if there are no aspects with that index
     */
    public String getSignalMastAspectByIndex(int x);

    /**
     * Inquire if a Signal Mast Aspect is included in the group.
     *
     * @param aspect name of the Aspect, i.e. "Clear"
     * @return true if aspect is included in group
     */
    public boolean isSignalMastAspectIncluded(String aspect);

    /**
     * Remove a SignalMast Aspect from the set of triggers.
     *
     * @param aspect Name of the Aspect, i.e. "Clear"
     */
    public void deleteSignalMastAspect(String aspect);

    /**
     * Add a Signal Head item for this Signal Head to the list in the Group.
     *
     * @param headBean The Signal Head as a Named Bean
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> headBean);

    /**
     * Add a Signal Head item for this Signal Head to the list in the Group.
     *
     * @param signalHead The Signal Head object
     */
    public void addSignalHead(SignalHead signalHead);

    /**
     * Get a Signal Head by Index.
     *
     * @param x Index of the SignalHead in the Group
     * @return null if there are no Signal Heads with that index in the group
     */
    public SignalHead getHeadItemBeanByIndex(int x);

    /**
     * Get the name of a Signal Head by Index.
     *
     * @param x Index of the SignalHead in the Group
     * @return null if there are no Signal Heads with that index in the group
     */
    public String getHeadItemNameByIndex(int x);

    /**
     * Get the On State for the Signal Head item at Index x in the group.
     *
     * @param x Index of the SignalHead in Group
     * @return -1 if there are less than 'x' Signal Heads defined
     */
    public int getHeadOnStateByIndex(int x);

    /**
     * Get the Off State for the Signal Head item at Index x in the group.
     *
     * @param x Index of the SignalHead in Group
     * @return -1 if there are less than 'x' Signal Heads defined
     */
    public int getHeadOffStateByIndex(int x);

    /**
     * Remove the Signal Head item for this Signal Head from the group by Name.
     *
     * @param sh The Signal Head to be deleted from the group.
     */
    public void deleteSignalHead(SignalHead sh);

    /**
     * Remove the Signal Head item for this Signal Head from the group by
     * NamedBean
     *
     * @param headBean The Named Bean to be removed from the group.
     */
    public void deleteSignalHead(NamedBeanHandle<SignalHead> headBean);

    /**
     * Get the number of Signal Heads configured as items in this group.
     *
     * @return the number of Signal Heads
     */
    public int getNumHeadItems();

    /**
     * Inquire if a Signal Head item for this head is included in this Group.
     *
     * @param signalHead The Signal Head object we are querying
     * @return true if the signal head is included in the group; false otherwise
     */
    public boolean isHeadIncluded(SignalHead signalHead);

    /**
     * Get the On (conditions met) State of a Signal Head item in the group.
     *
     * @param signalHead The Signal Head object we are querying
     * @return state value for the On state (appearance)
     */
    public int getHeadOnState(SignalHead signalHead);

    /**
     * Get the Off (conditions NOT met) State of a Signal Head item in the
     * group.
     *
     * @param signalHead The Signal Head Bean object we are querying
     * @return state value for the Off state (appearance)
     */
    public int getHeadOffState(SignalHead signalHead);

    /**
     * Set the On (conditions met) State of a Signal Head item in the Group.
     *
     * @param signalHead The SignalHead Bean
     * @param state      The Appearance that the SignalHead will change to when
     *                   the conditions are met.
     */
    public void setHeadOnState(SignalHead signalHead, int state);

    /**
     * Set the Off (conditions NOT met) State of a Signal Head item in the
     * Group.
     *
     * @param signalHead The SignalHead Bean
     * @param state      The Apperance that the SignalHead will change to when
     *                   the conditions are NOT met.
     */
    public void setHeadOffState(SignalHead signalHead, int state);

    /**
     * Set whether the sensors and turnouts should be treated as separate
     * calculations (OR) or as one (AND) when determining if the Signal Head
     * should be On or Off.
     *
     * @param signalHead The SignalHead Bean
     * @param boo        Provide true for AND, false for OR
     */
    public void setSensorTurnoutOper(SignalHead signalHead, boolean boo);

    /**
     * Get the state of the AND/OR conditional operand for Signal Head at Index.
     *
     * @param x Index of the SignalHead in Group
     * @return true when set to AND, false for OR
     */
    public boolean getSensorTurnoutOperByIndex(int x);

    /**
     * Get the number of turnouts configured for the Signal Head at index x.
     *
     * @param x Index of the SignalHead in Group
     * @return -1 if there are less than 'x' Signal Heads defined
     */
    public int getNumHeadTurnoutsByIndex(int x);

    /**
     * Add a Turnout and its On state to a Signal Head.
     *
     * @param signalHead SignalHead we are adding the turnout to
     * @param turnout    Turnout Bean
     * @param state      Value for the turnout On state (Turnout.THROWN or
     *                   Turnout.CLOSED).
     */
    public void setHeadAlignTurnout(SignalHead signalHead, Turnout turnout, int state);

    /**
     * Inquire if a Turnout is included in the Signal Head Calculation.
     *
     * @param signalHead signalHead that may consider turnout
     * @param turnout    turnout to consider
     * @return true if turnout state is considered; false otherwise
     */
    public boolean isTurnoutIncluded(SignalHead signalHead, Turnout turnout);

    /**
     * Get the On state of the Turnout for the given Signal Head in the group.
     *
     * @param signalHead Signal Head Bean
     * @param turnout    The Turnout within the Group
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutState(SignalHead signalHead, Turnout turnout);

    /**
     * Get the On state of a given Turnout for the Signal Head at index x.
     *
     * @param x       Index for the Signal Head in the group
     * @param turnout Name of the Turnout configured for the head
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutStateByIndex(int x, Turnout turnout);

    /**
     * Get the On state of the Turnout at index pTurnout, for the Signal Head at
     * index x in the group.
     *
     * @param x        Index for the Signal Head in the group
     * @param pTurnout Index of the Turnout configured for the head
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutStateByIndex(int x, int pTurnout);

    /**
     * Get the Name of the Turnout at index pTurnout, for the Signal Head at
     * index x in the group.
     *
     * @param x        Index for the Signal Head in the group
     * @param pTurnout Index for the turnout in the signal head item
     * @return null if the Turnout or Signal Head is invalid
     */
    public String getTurnoutNameByIndex(int x, int pTurnout);

    /**
     * Get the Turnout at index x, for the Signal Head at index x in the group.
     *
     * @param x        Index for the Signal Head in the group
     * @param pTurnout Index for the turnout in the signal head item
     * @return null if the Turnout or Signal Head is invalid
     */
    public Turnout getTurnoutByIndex(int x, int pTurnout);

    /**
     * Add a Sensor and its On state to a Signal Head.
     *
     * @param signalHead Signal Head we are adding the sensor to
     * @param sensor     Sensor Bean
     * @param state      Value for the Sensor On state (Sensor.ACTIVE or
     *                   Sensor.INACTIVE).
     */
    public void setHeadAlignSensor(SignalHead signalHead, Sensor sensor, int state);

    /**
     * Inquire if a Sensor is included in the Signal Head Calculation.
     *
     * @param signalHead Signal Head Bean
     * @param sensor     Sensor Bean
     * @return true if sensor is considered for signalHead state; false
     *         otherwise
     */
    public boolean isSensorIncluded(SignalHead signalHead, Sensor sensor);

    /**
     * Get the On state of the Sensor for the Signal Head in the group.
     *
     * @param signalHead The Signal Head Bean
     * @param sensor     Name of the Sensor in the head item
     * @return -1 if the Sensor or Signal Head is invalid
     */
    public int getSensorState(SignalHead signalHead, Sensor sensor);

    /**
     * Get the On state of the Sensor at index pSensor for the Signal Head at
     * index x.
     *
     * @param x       Index for the Signal Head in the group
     * @param pSensor Index of the Sensor in the head item
     * @return -1 if the Sensor or Signal Head is invalid
     */
    public int getSensorStateByIndex(int x, int pSensor);

    /**
     * Get the name of the Sensor at index pSensor for the Signal Head at index
     * x.
     *
     * @param x       Index for the Signal Head in the group
     * @param pSensor Index of the Sensor in the head item
     * @return null if the Sensor or Signal Head is invalid
     */
    public String getSensorNameByIndex(int x, int pSensor);

    /**
     * Get the Sensor at index pSensor, for the Signal Head at index x.
     *
     * @param x       Index for the Signal Head in the group
     * @param pSensor Index of the Sensor in the head item
     * @return null if the Sensor or Signal Head is invalid
     */
    public Sensor getSensorByIndex(int x, int pSensor);

    /**
     * Get the AND/OR conditional operand set for a Signal Head in the group.
     *
     * @param signalHead The Signal Head Bean
     * @return true when set to AND, false for OR
     */
    public boolean getSensorTurnoutOper(SignalHead signalHead);

    /**
     * Get the number of Sensors configured for the Signal Head at index x.
     *
     * @param x Index for the Signal Head in the group
     * @return -1 if there are less than 'x' Signal Heads defined
     */
    public int getNumHeadSensorsByIndex(int x);

    /**
     * Delete all Turnouts for a given Signal Head in the group.
     *
     * @param signalHead The Signal Head Bean from which the Turnouts will be
     *                   removed
     */
    public void clearHeadTurnout(SignalHead signalHead);

    /**
     * Delete all Sensors for a given Signal Head in the group.
     *
     * @param signalHead The Signal Head Bean from which the Turnouts will be
     *                   removed
     */
    public void clearHeadSensor(SignalHead signalHead);

    @Override
    public int getState();

    @Override
    public void setState(int state);

    static final int ONACTIVE = 0;    // group head conditional fires if sensor goes active
    static final int ONINACTIVE = 1;  // group head conditional fires if sensor goes inactive

    static final int ONCLOSED = 2;    // group head conditional fires if turnout goes closed
    static final int ONTHROWN = 4;  // group head conditional fires if turnout goes thrown
}

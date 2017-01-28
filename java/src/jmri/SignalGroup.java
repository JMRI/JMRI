package jmri;

/**
 * SignalGroup.java
 *<P>
 * The Signal Group is used to represent European subsidary signals that would
 * be sited with a signal mast. Such subsidary signals would be used to
 * indicated routes, junctions and allowable speeds. Each such
 * route/junction/speed would be represented by a single output signal (head), that is
 * either Off or On. Within the group only one such signal head would be allowed on at
 * any one time.
 * <P>
 * The group is attached to a main signal mast, and can be configured to be activated
 * depending upon one or more aspects when displayed on that signal mast.
 * <P>
 * Each signal head within the group is defined with an On and Off appearance, and a set
 * of criteria in the form of matching turnouts and sensor states, that must be met for
 * the head to be set On.
 * <P>
 * For code clarity, JMRI uses the terms (signal)Mast and (signal)Head instead of Signal.
 * Masts show Aspects, Heads show Appearances, though outside the USA this will vary.
 * Use localization to address this in the user interface.
 *
 * @author	Pete Cressman Copyright (C) 2009
 */
public interface SignalGroup extends NamedBean {

    /**
     * Set enabled status of the signal group.
     */
    public void setEnabled(boolean boo);

    /**
     * Get enabled status of the signal group.
     */
    public boolean getEnabled();

    /**
     * Sets the main Signal Mast to which the Group belongs.
     */
    public void setSignalMast(String mastName);

    /**
     * Sets the main Signal Mast to which the Group belongs.
     */
    public void setSignalMast(SignalMast signalMast, String mastName);

    /**
     * Get the name of the main Signal Mast in a group.
     * @return Name of the main Signal Mast as string
     */
    public String getSignalMastName();

    /**
     * Get the SignalMast in a group.
     * @return The main Signal Mast as bean
     */
    public SignalMast getSignalMast();

    /**
     * Clear the list of SignalMast Aspects that trigger the group.
     */
    public void clearSignalMastAspect();

    /**
     * Add an Aspect that can trigger the group activation.
     * @param aspect Name of an aspect to be included on the Main Signal Mast in the Group, must be valid
     */
    public void addSignalMastAspect(String aspect);

    public int getNumSignalMastAspects();

    /**
     * Method to get a SignalMast Aspect by Index.
     * Returns null if there are no Appearances with that index.
     */
    public String getSignalMastAspectByIndex(int x);

    /**
     * Inquire if a SignalMast Aspect is included.
     */
    public boolean isSignalMastAspectIncluded(String aspect);

    /**
     * Remove a SignalMast Aspect from the set of triggers.
     */
    public void deleteSignalMastAspect(String aspect);

    /**
     * Add a Signal Head to the Group.
     * @param headBean The Signal Head as a Named Bean
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> headBean);

    /**
     * Add a Signal Head to the Group.
     * @param mHead The Signal Head object
     */
    public void addSignalHead(SignalHead mHead);

    public SignalHead getHeadItemBeanByIndex(int n);

    /**
     * Method to get a Signal Head by Index.
     * @return null if there are no Signal Heads with that index
     */
    public String getHeadItemNameByIndex(int n);

    /**
     * Method to get the On State of a Signal Head at Index n.
     * @return -1 if there are less than 'n' Signal Heads defined
     */
    public int getHeadOnStateByIndex(int n);

    /**
     * Method to get the Off State of a Signal Head at Index n.
     * @return -1 if there are less than 'n' Signal Heads defined
     */
    public int getHeadOffStateByIndex(int n);

    /**
     * Method to delete a Signal Head by Name.
     * @param sh The Signal Head to be deleted from the group.
     */
    public void deleteSignalHead(SignalHead sh);

    /**
     * Method to delete a Signal Head by NamedBean
     * @param headBean The Named Bean to be removed from the group.
     */
    public void deleteSignalHead(NamedBeanHandle<SignalHead> headBean);

    /*
     * Method to get the number of Signal Heads in this group.
     * @return the number of Signal Heads
     */
    public int getNumHeadItems();

    /**
     * Method to inquire if a Signal Head is included in this Group.
     * @param signalHead The Signal Head object we are querying
     */
    public boolean isHeadIncluded(SignalHead signalHead);

    /**
     * Method to get the On State of Signal Head.
     * @param signalHead The Signal Head object we are querying
     * @return state value for the On state (appearance)
     */
    public int getHeadOnState(SignalHead signalHead);

    /**
     * Method to get the Off State of Signal Head.
     * @param signalHead The Signal Head Bean object we are querying
     * @return state value for the Off state (appearance)
     */
    public int getHeadOffState(SignalHead signalHead);

    /**
     * Sets the On State of the Signal Head in the Group.
     * @param signalHead  The SignalHead Bean
     * @param state The Appearance that the SignalHead will change to when the
     *              conditions are met.
     */
    public void setHeadOnState(SignalHead signalHead, int state);

    /**
     * Sets the Off State of the Signal Head in the Group.
     * @param signalHead  The SignalHead Bean
     * @param state The Apperance that the SignalHead will change to when the
     *              conditions are NOT met.
     */
    public void setHeadOffState(SignalHead signalHead, int state);

    /**
     * Sets whether the sensors and turnouts should be treated as separate
     * calculations (OR) or as one (AND) when determining if the Signal Head should be On
     * or Off.
     * @param signalHead The SignalHead Bean
     * @param boo Provide true for AND, false for OR
     */
    public void setSensorTurnoutOper(SignalHead signalHead, boolean boo);

    /**
     * Gets the state of the AND/OR conditional operand for Signal Head at Index.
     * @param x Index of the SignalHead in Group
     * @return true when set to AND, false for OR
     */
    public boolean getSensorTurnoutOperByIndex(int x);

    /**
     * Method to get the number of turnouts used to determine the On state for
     * the Signal Head at index x.
     * @return -1 if there are less than 'n' Signal Heads defined
     */
    public int getNumHeadTurnoutsByIndex(int x);

    /**
     * Method to add a Turnout and its state to a Signal Head.
     * @param mHead SignalHead we are adding the turnout to
     * @param mTurn Turnout Bean
     * @param state The State that the turnout must be set to.
     */
    public void setHeadAlignTurnout(SignalHead signalHead, Turnout turnout, int state);

    /**
     * Inquire if a Turnout is included in the Signal Head Calculation.
     * @param signalHead  Signal Head Bean
     * @param pTurnout Turnout Bean
     */
    public boolean isTurnoutIncluded(SignalHead signalHead, Turnout turnout);

    /**
     * Gets the state of the Turnout for the given Signal Head in the group.
     * @param signalHead  Signal Head Bean
     * @param pTurnout Name of the Turnout within the Group
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutState(SignalHead signalHead, Turnout turnout);

    /**
     * Gets the state of a given Turnout for the Signal Head at index x.
     * @param x        Signal Head at index x
     * @param pTurnout Name of the Turnout within the Group
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutStateByIndex(int x, Turnout turnout);

    /**
     * Gets the state of the Turnout at index pTurnout, for the Signal Head at
     * index x.
     * @param x        Signal Head at index x
     * @param pTurnout Turnout at index pTurnout
     * @return -1 if the Turnout or Signal Head is invalid
     */
    public int getTurnoutStateByIndex(int x, int pTurnout);

    /**
     * Gets the Name of the Turnout at index pTurnout, for the Signal Head at
     * index x in the group.
     * @param x Index for the signal head in the group
     * @param pTurnout Index for the turnout in the signal head item
     * @return null if the Turnout or Signal Head is invalid
     */
    public String getTurnoutNameByIndex(int x, int pTurnout);

    /**
     * Gets the Turnout at index x, for the Signal Head at
     * index x in the group.
     * @param x Index for the signal head in the group
     * @param pTurnout Index for the turnout in the signal head item
     * @return null if the Turnout or Signal Head is invalid
     */
    public Turnout getTurnoutByIndex(int x, int pTurnout);

    /**
     * Method to add a Sensor and its state to a Signal Head.
     * @param mHead   SignalHead we are adding the sensor to
     * @param mSensor Sensor Bean
     * @param state   The State that the sensor must be set to.
     */
    public void setHeadAlignSensor(SignalHead signalHead, Sensor sensor, int state);

    /**
     * Inquire if a Sensor is included in the Signal Head Calculation.
     * @param signalHead Signal Head Bean
     * @param pSensor Sensor Bean
     */
    public boolean isSensorIncluded(SignalHead signalHead, Sensor sensor);

    /**
     * Gets the state of the Sensor for the Signal Head in the group.
     * @param signalHead Signal Head Bean
     * @param pSensor Name of the Sensor within the Group
     * @return -1 if the Sensor or Signal Head is invalid
     */
    public int getSensorState(SignalHead signalHead, Sensor sensor);

    /**
     * Gets the state of the Sensor at index pSensor for the Signal Head at
     * index x.
     * @param x       Index of the Signal Head
     * @param pSensor Index of the Sensor for the head
     * @return -1 if the Sensor or Signal Head is invalid
     */
    public int getSensorStateByIndex(int x, int pSensor);

    /**
     * Gets the name of the Sensor at index pSensor for the Signal Head at
     * index x.
     * @param x       Index of the Signal Head
     * @param pSensor Sensor at index pTurnout
     * @return null if the Sensor or Signal Head is invalid
     */
    public String getSensorNameByIndex(int x, int pSensor);

    /**
     * Gets the Sensor at index pSensor, for the Signal Head at
     * index x.
     * @param x       Signal Head at index x
     * @param pSensor Index of the sensor in the head item
     * @return null if the Sensor or Signal Head is invalid
     */
    public Sensor getSensorByIndex(int x, int pSensor);

    /**
     * Gets the state of the AND/OR conditional operand for a Signal Head.
     * @param signalHead The name of the SignalHead
     * @return true when set to AND, false for OR
     */
    public boolean getSensorTurnoutOper(SignalHead signalHead);

    /**
     * Method to get the number of Sensors used to determine the On state for
     * the Signal Head at index x.
     * @return -1 if there are less than 'n' Signal Heads defined
     */
    public int getNumHeadSensorsByIndex(int x);

    /**
     * Delete all Turnouts for a given Signal Head in the group.
     * @param signalHead The Named Bean for Signal Head to be cleared
     */
    public void clearHeadTurnout(SignalHead signalHead);

    /**
     * Delete all Sensors for a given Signal Head in the group.
     * @param signalHead The Named Bean for Signal Head to be cleared
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

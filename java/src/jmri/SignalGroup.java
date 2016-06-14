package jmri;

/**
 * SignalGroup.java
 *
 * The Signal Group is used to represent European subsidary signals that would
 * be sited with a signal mast. Such subsidary signals would be used to
 * indicated routes, junctions and allowable speeds. Each such
 * route/junction/speed would be represented by a single output signal, that is
 * either off or on. Within the group on one such signal would be allowed on at
 * any one time.
 *
 * The group is attached to a signal mast, and can be configured to be activated
 * depending upon that masts appearance. The Each signal head within the group
 * is defined with a On and Off appearance, and a set of criteria in the form of
 * matching turnouts and sensor states, that must be met for the head to be set
 * On.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Pete Cressman Copyright (C) 2009
 */
public interface SignalGroup extends NamedBean {

    /**
     * Set enabled status.
     */
    public void setEnabled(boolean boo);

    /**
     * Get enabled status
     */
    public boolean getEnabled();

    /**
     * Sets the main Signal Mast to which the Group belongs
     */
    public void setSignalMast(String pName);

    /**
     * Sets the main Signal Mast to which the Group belongs
     */
    public void setSignalMast(SignalMast mMast, String pName);

    /**
     * Get the name of the main Signal Mast
     */
    public String getSignalMastName();

    /**
     * Get the SignalMast
     */
    public SignalMast getSignalMast();

    /**
     * Clear the list of SignalMast Appearances that trigger the group
     */
    public void clearSignalMastAppearance();

    /**
     * Add an appearance that can trigger the group activation
     */
    public void addSignalMastAppearance(String sppearance);

    public int getNumSignalMastAppearances();

    /**
     * Method to get a SignalMast Appearance by Index Returns null if there are
     * no Appearances with that index
     */
    public String getSignalMastAppearanceByIndex(int x);

    /**
     * Inquire if a SignalMast Appearance is included.
     */
    public boolean isSignalMastAppearanceIncluded(String appearance);

    /**
     * Remove a SignalMast Appearance from the set of triggers.
     */
    public void deleteSignalMastAppearance(String appearance);

    /**
     * Add a Signal Head to the Group
     *
     * @param sh The SignalHead as a Named Bean
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> sh);

    /**
     * Add a Signal Head to the Group
     *
     * @param mHead The SignalHead as a Named Bean
     */
    public void addSignalHead(SignalHead mHead);

    public SignalHead getSignalHeadItemBeanByIndex(int n);

    /**
     * Method to get a SignalHead by Index Returns null if there are no Signal
     * Heads with that index
     */
    public String getSignalHeadItemNameByIndex(int n);

    /**
     * Method to get the On State of a SignalState at Index n
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getSignalHeadOnStateByIndex(int n);

    /**
     * Method to get the Off State of a SignalState at Index n
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getSignalHeadOffStateByIndex(int n);

    /**
     * Delete Signal Head by Name
     */
    public void deleteSignalHead(SignalHead pSignal);

    /**
     * Delete Signal Head by NamedBean
     */
    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh);

    public int getNumSignalHeadItems();

    /**
     * Method to inquire if a Signal Head is included in this Group
     */
    public boolean isSignalIncluded(SignalHead signalHead);

    /**
     * Method to get the On State of Signal Head
     *
     * @param signalHead The signal head object we are querying
     */
    public int getSignalHeadOnState(SignalHead signalHead);

    /**
     * Method to get the Off State of Signal Head
     *
     * @param signalHead The signal head bean object we are querying
     */
    public int getSignalHeadOffState(SignalHead signalHead);

    /**
     * Sets the On State of the Signal in the Group
     *
     * @param head  The SignalHead Bean
     * @param state The Apperance that the SignalHead will change to when the
     *              conditions are met.
     */
    public void setSignalHeadOnState(SignalHead head, int state);

    /**
     * Sets the Off State of the Signal in the Group
     *
     * @param head  The SignalHead Bean
     * @param state The Apperance that the SignalHead will change to when the
     *              conditions are NOT met.
     */
    public void setSignalHeadOffState(SignalHead head, int state);

    /**
     * Sets whether the sensors and turnouts should be treated as seperate
     * calculations or as one, when determining if the signal head should be on
     * or off.
     */
    public void setSensorTurnoutOper(SignalHead pSignal, boolean boo);

    public boolean getSensorTurnoutOperByIndex(int x);

    /**
     * Method to get the number of turnouts used to determine the On state for
     * the signalhead at index x
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getNumSignalHeadTurnoutsByIndex(int x);

    /**
     * Method to add a Turnout and its state to a signal head.
     * <p>
     * @param mHead SignalHead we are adding the turnout to
     * @param mTurn Turnout Bean
     * @param state The State that the turnout must be set to.
     */
    public void setSignalHeadAlignTurnout(SignalHead mHead, Turnout mTurn, int state);

    /**
     * Inquire if a Turnout is included in the Signal Head Calculation.
     *
     * @param pSignal  Signal Head Bean
     * @param pTurnout Turnout Bean
     */
    public boolean isTurnoutIncluded(SignalHead pSignal, Turnout pTurnout);

    /**
     * Gets the state of the Turnout for the given Signal Head in the group
     *
     * @param pSignal  Signal Head Bean
     * @param pTurnout Name of the Turnout within the Group
     * @return -1 if the turnout or signal head is invalid
     */
    public int getTurnoutState(SignalHead pSignal, Turnout pTurnout);

    /**
     * Gets the state of the Turnout for the given Signal Head at index x
     *
     * @param x        Signal Head at index x
     * @param pTurnout Name of the Turnout within the Group
     * @return -1 if the turnout or signal head is invalid
     */
    public int getTurnoutStateByIndex(int x, Turnout pTurnout);

    /**
     * Gets the state of the Turnout at index x, for the given Signal Head at
     * index x
     *
     * @param x        Signal Head at index x
     * @param pTurnout Turnout at index pTurnout
     * @return -1 if the turnout or signal head is invalid
     */
    public int getTurnoutStateByIndex(int x, int pTurnout);

    /**
     * Gets the Name of the Turnout at index x, for the given Signal Head at
     * index x
     *
     * @param x        Signal Head at index x
     * @param pTurnout Turnout at index pTurnout
     * @return null if the turnout or signal head is invalid
     */
    public String getTurnoutNameByIndex(int x, int pTurnout);

    /**
     * Gets the Name of the Turnout at index x, for the given Signal Head at
     * index x
     *
     * @param x        Signal Head at index x
     * @param pTurnout Turnout at index pTurnout
     * @return null if the turnout or signal head is invalid
     */
    public Turnout getTurnoutByIndex(int x, int pTurnout);

    /**
     * Method to add a Sensor and its state to a signal head.
     * <p>
     * @param mHead   SignalHead we are adding the sensor to
     * @param mSensor Sensor Bean
     * @param state   The State that the sensor must be set to.
     */
    public void setSignalHeadAlignSensor(SignalHead mHead, Sensor mSensor, int state);

    /**
     * Inquire if a Sensor is included in the Signal Head Calculation.
     *
     * @param pSignal Signal Head Bean
     * @param pSensor Sensor Bean
     */
    public boolean isSensorIncluded(SignalHead pSignal, Sensor pSensor);

    /**
     * Gets the state of the Sensor for the given Signal Head in the group
     *
     * @param pSignal Signal Head Bean
     * @param pSensor Name of the Sensor within the Group
     * @return -1 if the sensor or signal head is invalid
     */
    public int getSensorState(SignalHead pSignal, Sensor pSensor);

    /**
     * Gets the state of the Sensor for the given Signal Head at index x
     *
     * @param x       Signal Head at index x
     * @param pSensor Name of the Sensor within the Group
     * @return -1 if the sensor or signal head is invalid
     */
    public int getSensorStateByIndex(int x, int pSensor);

    /**
     * Gets the state of the Sensor at index x, for the given Signal Head at
     * index x
     *
     * @param x       Signal Head at index x
     * @param pSensor Sensor at index pTurnout
     * @return null if the sensor or signal head is invalid
     */
    public String getSensorNameByIndex(int x, int pSensor);

    /**
     * Gets the state of the Sensor at index x, for the given Signal Head at
     * index x
     *
     * @param x       Signal Head at index x
     * @param pSensor Sensor at index pTurnout
     * @return null if the sensor or signal head is invalid
     */
    public Sensor getSensorByIndex(int x, int pSensor);

    public boolean getSensorTurnoutOper(SignalHead pSignal);

    /**
     * Method to get the number of Sensors used to determine the On state for
     * the signalhead at index x
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getNumSignalHeadSensorsByIndex(int x);

    /**
     * Delete all Turnouts for a given SignalHead in the group
     *
     * @param pSignal SignalHead Name
     */
    public void clearSignalTurnout(SignalHead pSignal);

    /**
     * Delete all Sensors for a given SignalHead in the group
     *
     * @param pSignal SignalHead Name
     */
    public void clearSignalSensor(SignalHead pSignal);

    public int getState();

    public void setState(int state);

    static final int ONACTIVE = 0;    // route fires if sensor goes active
    static final int ONINACTIVE = 1;  // route fires if sensor goes inactive

    static final int ONCLOSED = 2;    // route fires if turnout goes closed
    static final int ONTHROWN = 4;  // route fires if turnout goes thrown
}

// Conditional.java

package jmri;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import jmri.ConditionalAction;
/**
 * A Conditional is layout control logic, consisting of a logical 
 * expression and an action.
 * <P>
 * A Conditional does not exist on its own, but is part of a Logix.
 * The system name of each Conditional is set automatically when
 * the conditional is created.  It begins with the system name of
 * its parent Logix.  There is no Conditional Table.  Conditionals
 * are created, editted, and deleted via the Logix Table. 
 * <P>
 * A Conditional has a "state", which changes depending on whether its
 * logical expression calculates to TRUE or FALSE. The "state" may not
 * be changed by the user. It only changes in response to changes in
 * the "state variables" used in its logical expression.
 * <P>
 * Listeners may be set to monitor a change in the state of a 
 * conditional.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Dave Duchamp Copyright (C) 2007, 2008
 * @author                      Matthew Harris copyright (c) 2009
 * @version			$Revision 1.0 $
 */
 
public interface Conditional extends NamedBean {


    // states 
    public static final int TRUE      = 0x01;
    public static final int FALSE     = 0x02;
	public static final int UNKNOWN   = 0x04;

    // logic operators used in antecedent
    public static final int ALL_AND     = 0x01;
    public static final int ALL_OR      = 0x02;
	public static final int MIXED       = 0x03;
	
	// state variables definitions
	public static final int MAX_STATE_VARIABLES = 21;
	// note: when state variables are evaluate for calculating
	//       the state of a Conditional, only NOT is important.
	//		 AND is assumed.
	public static final int OPERATOR_AND = 1;
	public static final int OPERATOR_NOT = 2;
	public static final int OPERATOR_AND_NOT = 3;	
	public static final int OPERATOR_NONE = 4;
    public static final int OPERATOR_OR = 5;
    public static final int OPERATOR_OR_NOT = 6;
	// state variable types
	public static final int NUM_STATE_VARIABLE_TYPES = 20;	
	public static final int TYPE_NONE = 0;
	public static final int TYPE_SENSOR_ACTIVE = 1;
	public static final int TYPE_SENSOR_INACTIVE = 2;
	public static final int TYPE_TURNOUT_THROWN = 3;
	public static final int TYPE_TURNOUT_CLOSED = 4;
	public static final int TYPE_CONDITIONAL_TRUE = 5;
	public static final int TYPE_CONDITIONAL_FALSE = 6;	
	public static final int TYPE_LIGHT_ON = 7;
	public static final int TYPE_LIGHT_OFF = 8;
	public static final int TYPE_MEMORY_EQUALS = 9;
	public static final int TYPE_FAST_CLOCK_RANGE = 10;
// Note - within the TYPE_SIGNAL_HEAD definitions, all must be together,
//		RED must be first, and HELD must be last
	public static final int TYPE_SIGNAL_HEAD_RED = 11;
	public static final int TYPE_SIGNAL_HEAD_YELLOW = 12;
	public static final int TYPE_SIGNAL_HEAD_GREEN = 13;
	public static final int TYPE_SIGNAL_HEAD_DARK = 14;
	public static final int TYPE_SIGNAL_HEAD_FLASHRED = 15;
	public static final int TYPE_SIGNAL_HEAD_FLASHYELLOW = 16;
	public static final int TYPE_SIGNAL_HEAD_FLASHGREEN = 17;
	public static final int TYPE_SIGNAL_HEAD_LIT = 18;
	public static final int TYPE_SIGNAL_HEAD_HELD = 19;
    public static final int TYPE_MEMORY_COMPARE = 20;
	public static final int TYPE_SIGNAL_HEAD_LUNAR = 21;
	public static final int TYPE_SIGNAL_HEAD_FLASHLUNAR = 22;
	
	// action definitions
	public static final int ACTION_OPTION_ON_CHANGE_TO_TRUE = 1;
	public static final int ACTION_OPTION_ON_CHANGE_TO_FALSE = 2;
	public static final int ACTION_OPTION_ON_CHANGE = 3;
	public static final int NUM_ACTION_OPTIONS = 3;
	// action types
	public static final int NUM_ACTION_TYPES = 30;
	public static final int ACTION_NONE = 1;
	public static final int ACTION_SET_TURNOUT = 2;
	// allowed settings for turnout are Thrown and Closed (in data)
	public static final int ACTION_SET_SIGNAL_APPEARANCE = 3;
	// allowed settings for signal head are the seven Appearances (in data)
	public static final int ACTION_SET_SIGNAL_HELD = 4;
	public static final int ACTION_CLEAR_SIGNAL_HELD = 5;
	public static final int ACTION_SET_SIGNAL_DARK = 6;
	public static final int ACTION_SET_SIGNAL_LIT = 7;
	public static final int ACTION_TRIGGER_ROUTE = 8;
	public static final int ACTION_SET_SENSOR = 9;
	// allowed settings for sensor are active and inactive (in data)
	public static final int ACTION_DELAYED_SENSOR = 10;
	// allowed settings for timed sensor are active and inactive (in data)
	//   time in seconds before setting sensor should be in delay
	public static final int ACTION_SET_LIGHT = 11;
	// allowed settings for light are ON and OFF (in data)
	public static final int ACTION_SET_MEMORY = 12;
	// text to set into the memory variable should be in string
	public static final int ACTION_ENABLE_LOGIX = 13;
	public static final int ACTION_DISABLE_LOGIX = 14;
	public static final int ACTION_PLAY_SOUND = 15;
	// reference to sound should be in string
	public static final int ACTION_RUN_SCRIPT = 16;
	// reference to script should be in string
	public static final int ACTION_DELAYED_TURNOUT = 17;
	// allowed settings for timed turnout are Thrown and Closed (in data)
	//   time in seconds before setting turnout should be in delay
	public static final int ACTION_LOCK_TURNOUT = 18;
	public static final int ACTION_RESET_DELAYED_SENSOR = 19;
	// allowed settings for timed sensor are active and inactive (in data)
	//   time in seconds before setting sensor should be in delay
	public static final int ACTION_CANCEL_SENSOR_TIMERS = 20;
	// cancels all timers delaying setting of specified sensor
	public static final int ACTION_RESET_DELAYED_TURNOUT = 21;
	// allowed settings for timed sensor are active and inactive (in data)
	//   time in seconds before setting sensor should be in delay
	public static final int ACTION_CANCEL_TURNOUT_TIMERS = 22;
	// cancels all timers delaying setting of specified sensor
	public static final int ACTION_SET_FAST_CLOCK_TIME = 23;
	// sets the fast clock time to the time specified
	public static final int ACTION_START_FAST_CLOCK = 24;
	// starts the fast clock
	public static final int ACTION_STOP_FAST_CLOCK = 25;
	// stops the fast clock
	public static final int ACTION_COPY_MEMORY = 26;
	// copies value from memory variable (in name) to memory variable (in string)
	public static final int ACTION_SET_LIGHT_INTENSITY = 27;
	// copies value from memory variable (in name) to memory variable (in string)
	public static final int ACTION_SET_LIGHT_TRANSITION_TIME = 28;
	// copies value from memory variable (in name) to memory variable (in string)
    public static final int ACTION_CONTROL_AUDIO = 29;
    // control the specified audio object
    public static final int ACTION_JYTHON_COMMAND = 30;
    // execute a jython command
				
    /**
    * set the logic type (all AND's all OR's or mixed AND's and OR's
    * set the antecedent expression - should be a well formed boolean
    * statement with parenthesis indicating the order of evaluation
    */
    public void setLogicType(int type, String antecedent);

	/**
	 * Get antecedent (boolean expression) of Conditional
	 */
	public String getAntecedentExpression();

	/**
	 * Get type of operators in the antecedent statement
	 */
    public int getLogicType();

	/**
	 * Set list of actions
	 */
	public void setAction (ArrayList <ConditionalAction> arrayList);

	/**
	 * Make deep clone of actions
	 */
	public ArrayList <ConditionalAction> getCopyOfActions ();

   /**
     * Set State Variables for this Conditional. Each state variable will 
	 * evaluate either True or False when this Conditional is calculated.
	 *<P>
	 * This method assumes that all information has been validated.
     */
    public void setStateVariables(ArrayList <ConditionalVariable> arrayList);
		
	/**
	 * Make deep clone of variables
	 */
	public ArrayList <ConditionalVariable> getCopyOfStateVariables ();
		
    /**
	 * Calculate this Conditional, triggering either or both actions if the user 
	 *   specified conditions are met, and the Logix is enabled.
	 *  Sets the state of the conditional.
	 *  Returns the calculated state of this Conditional.
	 */
	public int calculate (boolean enabled, PropertyChangeEvent evt);

   /**
    *  Check that an antecedent is well formed.  If not,
    * returns an error message.  Otherwise returns null.
    */
    public String validateAntecedent(String ant, ArrayList <ConditionalVariable> variableList);


	/**
	 * Stop a sensor timer if one is actively delaying setting of the specified sensor
	 */
	public void cancelSensorTimer (String sname);

	/**
	 * Stop a turnout timer if one is actively delaying setting of the specified turnout
	 */
	public void cancelTurnoutTimer (String sname);

    /**
     * State of the Conditional is returned.  
     * @return state value
     */
    public int getState();

    /**
     * Request a call-back when the bound KnownState property changes.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!
  
}

/* @(#)Conditional.java */

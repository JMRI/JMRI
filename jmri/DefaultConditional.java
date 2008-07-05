// DefaultConditional.java

package jmri;
import jmri.Timebase;
import java.util.Date;
import javax.swing.Timer;

 /**
 * Class providing the basic logic of the Conditional interface.
 *
 * @author	Dave Duchamp Copyright (C) 2007
 * @version     $Revision: 1.13 $
 */
public class DefaultConditional extends AbstractNamedBean
    implements Conditional, java.io.Serializable {

    public DefaultConditional(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultConditional(String systemName) {
        super(systemName);
    }

    /**
     *  Persistant instance variables (saved between runs)
     */
	 // state variables in logical expression
	 protected int[] varOperator = new int[MAX_STATE_VARIABLES];
	 protected int[] varType = new int[MAX_STATE_VARIABLES];
	 protected String[] varName = new String[MAX_STATE_VARIABLES];
	 protected String[] varDataString = new String[MAX_STATE_VARIABLES];
	 protected int[] varNum1 = new int[MAX_STATE_VARIABLES];
	 protected int[] varNum2 = new int[MAX_STATE_VARIABLES];
	 protected boolean[] varTriggersCalculation = new boolean[MAX_STATE_VARIABLES];
	 // action parameters - 2 actions allowed
	 protected int[] actionOption = {ACTION_OPTION_ON_CHANGE_TO_TRUE,
									ACTION_OPTION_ON_CHANGE_TO_TRUE};
	 protected int[] actionDelay = {0,0}; // delay before action (seconds)
	 protected int[] actionType = {ACTION_NONE,ACTION_NONE};
	 protected String[] actionName = {" "," "};
	 protected int[] actionData = {0,0};
	 protected String[] actionString = {" "," "};

    /**
     *  Operational instance variables (not saved between runs)
     */
	 protected int numStateVariables = 0;	 
	 protected int currentState = Conditional.UNKNOWN;
	 protected Timer[] mDelayTimer = {null,null};
	 protected java.awt.event.ActionListener[] mDelayListener = {null,null};
	 protected boolean [] mDelayTimerActive = {false,false}; 
	 protected jmri.jmrit.Sound[] snd = {null,null};
	
	/**
	 * Get number of State Variables for this Conditional
	 */
	public int getNumStateVariables() {
		return (numStateVariables);
	}
	
	/**
     * Set State Variables for this Conditional. Each state variable will 
	 * evaluate either True or False when this Conditional is calculated.
	 *<P>
	 * Returns true if state variables were successfully set, otherwise false.
	 *<P>
	 * This method should only be called by LogixTableAction.  It assumes that all
	 * information has been validated.
     */
    public boolean setStateVariables(int[] opern,int[] type,String[] name,String[] data,
					int[] num1,int[] num2,boolean[] triggersCalc,int numVariables) {
		if (numVariables<=0) {
			// return without doing anything - user will have been warned elsewhere
			return (true);
		}
		if (numVariables>MAX_STATE_VARIABLES) {
			// too many state variables - ignore the excess
			log.error("attempt to set too many State Variables for Conditional");
			numVariables = MAX_STATE_VARIABLES;
		}
		// copy information to this Conditional's arrays
		for (int i = 0;i<numVariables;i++) {
			varOperator[i] = opern[i];
			varType[i] = type[i];
			varName[i] = name[i];
			varDataString[i] = data[i];
			varNum1[i] = num1[i];
			varNum2[i] = num2[i];
			varTriggersCalculation[i] = triggersCalc[i];
		}
		numStateVariables = numVariables;
		return (true);
	}
	
	/**
     * Get State Variables for this Conditional. 
	 *<P>
	 * Returns state variables for this Conditional in supplied arrays.
	 *<P>
	 * This method should only be called by LogixTableAction and methods to save
	 * this conditional to disk in a panel file.  
     */
    public void getStateVariables(int[] opern,int[] type,String[] name,
					String[] data,int[] num1,int[] num2,boolean[] triggersCalc) {
		if (numStateVariables == 0) {
			return;
		}
		// copy information from this Conditional's arrays
		for (int i = 0;i<numStateVariables;i++) {
			opern[i] = varOperator[i];
			type[i] = varType[i];
			name[i] = varName[i];
			data[i] = varDataString[i];
			num1[i] = varNum1[i];
			num2[i] = varNum2[i];
			triggersCalc[i] = varTriggersCalculation[i];
		}
	}

	/**
	 * Provide access to operator of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public int getStateVariableOperator(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varOperator[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableOperator");
		return (-1);
	}

	/**
	 * Provide access to type of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public int getStateVariableType(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varType[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableType");
		return (-1);
	}
	
	/**
	 * Provide access to Name (user or system, whichever was specified) of 
	 *    state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public String getStateVariableName(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varName[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableName");
		return ("");
	}
	
	/**
	 * Provide access to data string of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public String getStateVariableDataString(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varDataString[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableDataString");
		return ("");
	}

	/**
	 * Provide access to number 1 data of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public int getStateVariableNum1(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varNum1[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableNum1");
		return (-1);
	}

	/**
	 * Provide access to number 2 data of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public int getStateVariableNum2(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varNum2[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableNum2");
		return (-1);
	}

	/**
	 * Provide access to triggers option of state variable by index
	 *  Note: returns true if Logix should listen for changes in this
	 *		state variable to trigger calculation (default) and
	 *		returns false if the listener should be suppressed.
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public boolean getStateVariableTriggersCalculation(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varTriggersCalculation[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableTriggersCalculation");
		return (true);
	}

	/**
     * Delete all State Variables from this Conditional
     */
    public void deleteAllStateVariables() {
		numStateVariables = 0;
	}
	
	/**
	 * Set action parameters for action 1 and action 2
	 */
	public void setAction (int[] opt, int[] delay, int[] type,
				String[] name,int[] data,String[] s) {
		for (int i = 0;i<2;i++) {
			actionOption[i] = opt[i];
			actionDelay[i] = delay[i];
			actionType[i] = type[i];
			actionName[i] = name[i];
			actionData[i] = data[i];
			actionString[i] = s[i];
			snd[i] = null;
		}
	}		
	
	/**
	 * Get action parameters for action 1 and action 2
	 */
	public void getAction (int[] opt, int[] delay, int[] type,
				String[] name,int[] data,String[] s) {
		for (int i = 0;i<2;i++) {
			opt[i] = actionOption[i];
			delay[i] = actionDelay[i];
			type[i] = actionType[i];
			name[i] = actionName[i];
			data[i] = actionData[i];
			s[i] = actionString[i];
		}
	}		
	
    /**
	 * Calculate this Conditional, triggering either or both actions if the user 
	 *   specified conditions are met, and the Logix is enabled.
	 * Note: if any state variable evaluates false, the Conditional calculates
	 *   to false.  If all state variables evaluate true, the Conditional 
	 *   calculates to true.  So, the first false state variable results in 
	 *   a false state for the conditional.
	 * Sets the state of the conditional.
	 * Returns the calculated state of this Conditional.
	 */
	public int calculate (boolean logixEnabled) {
		// check if  there are no state variables
		if (numStateVariables==0) {
			// if there are no state variables, no state can be calculated
			currentState = Conditional.UNKNOWN;
		}
		else {
			// calculate the state
			int oldState = currentState;
			boolean result = true;
			for (int index = 0;(index<numStateVariables)&&result;index++) {
				result = evaluateStateVariable(index);
			}
			int newState = FALSE;
			if (result) newState = TRUE;
			if (newState!=oldState) {
				// a change has occurred, set state and take action if needed
				setState(newState);
				if (logixEnabled) {
					takeActionIfNeeded();
				}
			}
		}
		return currentState;
	}

	/**
	*  Evaluates one State Variable
	*  <P>
	*  Returns true if variable evaluates true, otherwise false. 
	*/
	private boolean evaluateStateVariable(int index) {
		// check vNOT and translate to the proper Conditional operator designation
		boolean result = true;
		String vName = varName[index];
		Sensor sn = null;
		Turnout t = null;
		SignalHead h = null;
		Conditional c = null;
		Light lgt = null;
		Memory m = null;
		Logix x = null;
		// evaluate according to state variable type		
		switch (varType[index]) {
			case TYPE_SENSOR_ACTIVE:
				sn = InstanceManager.sensorManagerInstance().provideSensor(vName);
				if (sn == null) {
					log.error("invalid sensor name in state variable - "+vName);
					return (false);
				}
				if (sn.getState() == Sensor.ACTIVE) result = true;
				else result = false;
				break;
			case TYPE_SENSOR_INACTIVE:
				sn = InstanceManager.sensorManagerInstance().provideSensor(vName);
				if (sn == null) {
					log.error("invalid sensor name in state variable - "+vName);
					return (false);
				}
				if (sn.getState() == Sensor.INACTIVE) result = true;
				else result = false;
				break;
			case TYPE_TURNOUT_THROWN:
				t = InstanceManager.turnoutManagerInstance().provideTurnout(vName);
				if (t == null) {
					log.error("invalid turnout name in state variable - "+vName);
					return (false);
				}
				if (t.getKnownState() == Turnout.THROWN) result = true;
				else result = false;
				break;
			case TYPE_TURNOUT_CLOSED:
				t = InstanceManager.turnoutManagerInstance().provideTurnout(vName);
				if (t == null) {
					log.error("invalid turnout name in state variable - "+vName);
					return (false);
				}
				if (t.getKnownState() == Turnout.CLOSED) result = true;
				else result = false;
				break;
			case TYPE_CONDITIONAL_TRUE:
				x = InstanceManager.conditionalManagerInstance().getParentLogix(getSystemName());
				if (x==null) {
					log.error("cannot find parent logix for "+getSystemName());
				}
				c = InstanceManager.conditionalManagerInstance().getConditional(x,vName);
				if (c == null) {
					log.error("invalid conditional name in state variable - "+vName);
					return (false);
				}
				if (c.getState() == TRUE) result = true;
				else result = false;
				break;
			case TYPE_CONDITIONAL_FALSE:
				x = InstanceManager.conditionalManagerInstance().getParentLogix(getSystemName());
				if (x==null) {
					log.error("cannot find parent logix for "+getSystemName());
				}
				c = InstanceManager.conditionalManagerInstance().getConditional(x,vName);
				if (c == null) {
					log.error("invalid conditional system name in state variable - "+vName);
					return (false);
				}
				if (c.getState() == FALSE) result = true;
				else result = false;
				break;
			case TYPE_LIGHT_ON:
				lgt = InstanceManager.lightManagerInstance().getLight(vName);
				if (lgt == null) {
					log.error("invalid light name in state variable - "+vName);
					return (false);
				}
				if (lgt.getState() == Light.ON) result = true;
				else result = false;
				break;
			case TYPE_LIGHT_OFF:
				lgt = InstanceManager.lightManagerInstance().getLight(vName);
				if (lgt == null) {
					log.error("invalid light name in state variable - "+vName);
					return (false);
				}
				if (lgt.getState() == Light.OFF) result = true;
				else result = false;
				break;
			case TYPE_MEMORY_EQUALS:
				m = InstanceManager.memoryManagerInstance().provideMemory(vName);
				if (m == null) {
					log.error("invalid memory name in state variable - "+vName);
					return (false);
				}
				String str = (String)m.getValue();
				if (str!=null && str.equals(varDataString[index])) result = true; 
				else result = false;
				break;
			case TYPE_FAST_CLOCK_RANGE:
				// get current fast clock time
				Timebase fastClock = InstanceManager.timebaseInstance();
				Date currentTime = fastClock.getTime();
				int currentMinutes = (currentTime.getHours()*60) + currentTime.getMinutes();
				// check if current time is within range specified
				int beginTime = varNum1[index];
				int endTime = varNum2[index];
				if (endTime>beginTime) {
					// range is entirely within one day
					if ( (currentMinutes<endTime) && (currentMinutes>=beginTime) ) result = true;
					else result = false;
				}
				else {
					// range includes midnight
					if (currentMinutes>=beginTime) result = true;
					else if (currentMinutes<endTime) result = true;
					else result = false;
				}
				break;
			case TYPE_SIGNAL_HEAD_RED:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.RED) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_YELLOW:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.YELLOW) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_GREEN:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.GREEN) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_DARK:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.DARK) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHRED:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHRED) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHYELLOW:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHYELLOW) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHGREEN:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHGREEN) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_LIT:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				result = h.getLit(); 
				break;
			case TYPE_SIGNAL_HEAD_HELD:
				h = InstanceManager.signalHeadManagerInstance().getSignalHead(vName);
				if (h == null) {
					log.error("invalid signal head name in state variable - "+vName);
					return (false);
				}
				result = h.getHeld();
				break;
		}
		// apply NOT if specified
		if ( (varOperator[index]==OPERATOR_AND_NOT) || (varOperator[index]==OPERATOR_NOT) ) {
			result = !result;
		}
		return (result);
	}
	
	/**
	 * Compares action options, and takes action if appropriate
	 * <P>
	 * Only get here if a change in state has occurred when calculating this Conditional
	 */
	private void takeActionIfNeeded() {
		// cycle over both actions
		for (int i = 0;i<2;i++) {
			if ( ((currentState==TRUE) && (actionOption[i]==ACTION_OPTION_ON_CHANGE_TO_TRUE)) ||
				((currentState==FALSE) && (actionOption[i]==ACTION_OPTION_ON_CHANGE_TO_FALSE)) ||
					(actionOption[i]==ACTION_OPTION_ON_CHANGE) ) {
				// need to take this action
				SignalHead h = null;
				Logix x = null;		
				switch (actionType[i]) {
					case Conditional.ACTION_NONE:
						break;
					case Conditional.ACTION_SET_TURNOUT:
						Turnout t = InstanceManager.turnoutManagerInstance().
									getTurnout(actionName[i]);
						if (t == null) {
							log.error("invalid turnout name in action - "+actionName[i]);
						}
						else {
							t.setCommandedState(actionData[i]);
						}
						break;
					case Conditional.ACTION_DELAYED_TURNOUT:
						if (!mDelayTimerActive[i]) {
							// Create a timer if one does not exist
							if (mDelayTimer[i]==null) {
								mDelayListener[i] = new TimeTurnout(i);
								mDelayTimer[i] = new Timer(2000,
															mDelayListener[i]);
								mDelayTimer[i].setRepeats(false);
							}
							// Start the Timer to set the turnout
							mDelayTimer[i].setInitialDelay(actionDelay[i]*1000);
							mDelayTimerActive[i] = true;
							mDelayTimer[i].start();
						}
						else {
							log.warn("timer already active on request to start delayed turnout action - "+
																				actionName[i]);
						}
						break;
					case Conditional.ACTION_RESET_DELAYED_TURNOUT:
						if (mDelayTimerActive[i]) {
							// Stop the timer if it is active
							mDelayTimer[i].stop();
							mDelayTimerActive[i] = false;
						}
						else if (mDelayTimer[i]==null) {
							// Create a timer if one does not exist							
							mDelayListener[i] = new TimeTurnout(i);
							mDelayTimer[i] = new Timer(2000,mDelayListener[i]);
							mDelayTimer[i].setRepeats(false);
						}
						// Start the Timer to set the turnout
						mDelayTimer[i].setInitialDelay(actionDelay[i]*1000);
						mDelayTimerActive[i] = true;
						mDelayTimer[i].start();
						break;
					case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
						ConditionalManager cmg = jmri.InstanceManager.conditionalManagerInstance();
						java.util.Iterator iter = cmg.getSystemNameList().iterator();
						while (iter.hasNext()) {
							String sname = (String)iter.next();
							if (sname==null) 
								log.error("Conditional system name null during cancel turnput timers for "
														+ actionName[i]);
							Conditional c = cmg.getBySystemName(sname);
							if (c==null)
								log.error("Conditional null during cancel turnout timers for "
														+ actionName[i]);
							else {
								c.cancelTurnoutTimer(actionName[i]);
							}
						}						
						break;
					case Conditional.ACTION_LOCK_TURNOUT:
						Turnout tl = InstanceManager.turnoutManagerInstance().
									getTurnout(actionName[i]);
						if (tl == null) {
							log.error("invalid turnout name in action - "+actionName[i]);
						}
						else {
							if (actionData[i] == Turnout.LOCKED){
								tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
							}
							else if (actionData[i] == Turnout.UNLOCKED){
								tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
							}
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(actionName[i]);
						if (h == null) {
							log.error("invalid signal head name in action - "+actionName[i]);
						}
						else {
							h.setAppearance(actionData[i]);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(actionName[i]);
						if (h == null) {
							log.error("invalid signal head name in action - "+actionName[i]);
						}
						else {
							h.setHeld(true);
						}
						break;
					case Conditional.ACTION_CLEAR_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(actionName[i]);
						if (h == null) {
							log.error("invalid signal head name in action - "+actionName[i]);
						}
						else {
							h.setHeld(false);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_DARK:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(actionName[i]);
						if (h == null) {
							log.error("invalid signal head name in action - "+actionName[i]);
						}
						else {
							h.setLit(false);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_LIT:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(actionName[i]);
						if (h == null) {
							log.error("invalid signal head name in action - "+actionName[i]);
						}
						else {
							h.setLit(true);
						}
						break;
					case Conditional.ACTION_TRIGGER_ROUTE:
						Route r = InstanceManager.routeManagerInstance().
									getRoute(actionName[i]);
						if (r == null) {
							log.error("invalid route name in action - "+actionName[i]);
						}
						else {
							r.setRoute();
						}
						break;
					case Conditional.ACTION_SET_SENSOR:
						Sensor sn = InstanceManager.sensorManagerInstance().
									getSensor(actionName[i]);
						if (sn == null) {
							log.error("invalid sensor name in action - "+actionName[i]);
						}
						else {
							try {
								sn.setKnownState(actionData[i]);
							} 
							catch (JmriException e) {
								log.warn("Exception setting sensor "+actionName[i]+" in action");
							}
						}
						break;
					case Conditional.ACTION_DELAYED_SENSOR:
						if (!mDelayTimerActive[i]) {
							// Create a timer if one does not exist
							if (mDelayTimer[i]==null) {
								mDelayListener[i] = new TimeSensor(i);
								mDelayTimer[i] = new Timer(2000,
															mDelayListener[i]);
								mDelayTimer[i].setRepeats(false);
							}
							// Start the Timer to set the sensor
							mDelayTimer[i].setInitialDelay(actionDelay[i]*1000);
							mDelayTimerActive[i] = true;
							mDelayTimer[i].start();
						}
						else {
							log.warn("timer already active on request to start delayed sensor action - "+
																				actionName[i]);
						}
						break;
					case Conditional.ACTION_RESET_DELAYED_SENSOR:
						if (mDelayTimerActive[i]) {
							// Stop the timer if it is active
							mDelayTimer[i].stop();
							mDelayTimerActive[i] = false;
						}
						else if (mDelayTimer[i]==null) {
							// Create a timer if one does not exist							
							mDelayListener[i] = new TimeSensor(i);
							mDelayTimer[i] = new Timer(2000,mDelayListener[i]);
							mDelayTimer[i].setRepeats(false);
						}
						// Start the Timer to set the sensor
						mDelayTimer[i].setInitialDelay(actionDelay[i]*1000);
						mDelayTimerActive[i] = true;
						mDelayTimer[i].start();
						break;
					case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
						ConditionalManager cm = jmri.InstanceManager.conditionalManagerInstance();
						java.util.Iterator itr = cm.getSystemNameList().iterator();
						while (itr.hasNext()) {
							String sname = (String)itr.next();
							if (sname==null) 
								log.error("Conditional system name null during cancel sensor timers for "
														+ actionName[i]);
							Conditional c = cm.getBySystemName(sname);
							if (c==null)
								log.error("Conditional null during cancel sensor timers for "
														+ actionName[i]);
							else {
								c.cancelSensorTimer(actionName[i]);
							}
						}						
						break;
					case Conditional.ACTION_SET_LIGHT:
						Light lgt = InstanceManager.lightManagerInstance().
										getLight(actionName[i]);
						if (lgt == null) {
							log.error("invalid light name in action - "+actionName[i]);
						}
						else {
							lgt.setState(actionData[i]);
						}
						break;
					case Conditional.ACTION_SET_MEMORY:
						Memory m = InstanceManager.memoryManagerInstance().
										provideMemory(actionName[i]);
						if (m == null) {
							log.error("invalid memory name in action - "+actionName[i]);
						}
						else {
							m.setValue(actionString[i]);
						}
						break;
					case Conditional.ACTION_COPY_MEMORY:
						Memory mFrom = InstanceManager.memoryManagerInstance().
										provideMemory(actionName[i]);
						if (mFrom == null) {
							log.error("invalid memory name in action - "+actionName[i]);
						}
						else {
							Memory mTo = InstanceManager.memoryManagerInstance().
										provideMemory(actionString[i]);
							if (mTo == null) {
								log.error("invalid memory name in action - "+actionString[i]);
							}
							else {
								mTo.setValue(mFrom.getValue());
							}
						}
						break;
					case Conditional.ACTION_ENABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getLogix(actionName[i]);
						if (x == null) {
							log.error("invalid logix name in action - "+actionName[i]);
						}
						else {
							x.setEnabled(true);
						}
						break;
					case Conditional.ACTION_DISABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getLogix(actionName[i]);
						if (x == null) {
							log.error("invalid logix name in action - "+actionName[i]);
						}
						else {
							x.setEnabled(false);
						}
						break;
					case Conditional.ACTION_PLAY_SOUND:
						if (!(actionString[i].equals(""))) {
							if (snd[i] == null) {
								snd[i] = new jmri.jmrit.Sound(actionString[i]);
							}
							snd[i].play();
						}
						break;
					case Conditional.ACTION_RUN_SCRIPT:
						if (!(actionString[i].equals(""))) {
							jmri.util.PythonInterp.runScript(actionString[i]);
						}
						break;
					case Conditional.ACTION_SET_FAST_CLOCK_TIME:
						Date date = InstanceManager.timebaseInstance().getTime();
						date.setHours(actionData[i]/60);
						date.setMinutes(actionData[i] - ((actionData[i]/60)*60));
						date.setSeconds(0);
						InstanceManager.timebaseInstance().userSetTime(date);
						break;
					case Conditional.ACTION_START_FAST_CLOCK:
						InstanceManager.timebaseInstance().setRun(true);
						break;
					case Conditional.ACTION_STOP_FAST_CLOCK:
						InstanceManager.timebaseInstance().setRun(false);
						break;
				}
			}
		}
	}
	
	/**
	 * Stop a sensor timer if one is actively delaying setting of the specified sensor
	 */
	public void cancelSensorTimer (String sname) {
		// cycle over both actions
		for (int i = 0;i<2;i++) {
			if ( (actionType[i] == Conditional.ACTION_DELAYED_SENSOR) || 
						(actionType[i] == Conditional.ACTION_RESET_DELAYED_SENSOR) ) {
				if ( mDelayTimerActive[i] ) {
					// have active set sensor timer - is it for our sensor?
					if ( actionName[i].equals(sname) ) {
						// yes, names match, cancel timer
						mDelayTimer[i].stop();
						mDelayTimerActive[i] = false;
					}
					else {
						// check if same sensor by a different name
						Sensor sn = InstanceManager.sensorManagerInstance().
												getSensor(actionName[i]);
						if (sn == null) {
							log.error("Unknown sensor *"+actionName[i]+" in cancelSensorTimer.");
						}
						else if (sname.equals(sn.getSystemName()) || 
												sname.equals(sn.getUserName())) {
							// same sensor, cancel timer
							mDelayTimer[i].stop();
							mDelayTimerActive[i] = false;
						}
					}
				}
			}
		}
	}

	/**
	 * Stop a turnout timer if one is actively delaying setting of the specified turnout
	 */
	public void cancelTurnoutTimer (String sname) {
		// cycle over both actions
		for (int i = 0;i<2;i++) {
			if ( (actionType[i] == Conditional.ACTION_DELAYED_TURNOUT) || 
						(actionType[i] == Conditional.ACTION_RESET_DELAYED_TURNOUT) ) {
				if ( mDelayTimerActive[i] ) {
					// have active set turnout timer - is it for our turnout?
					if ( actionName[i].equals(sname) ) {
						// yes, names match, cancel timer
						mDelayTimer[i].stop();
						mDelayTimerActive[i] = false;
					}
					else {
						// check if same turnout by a different name
						Turnout tn = InstanceManager.turnoutManagerInstance().
												getTurnout(actionName[i]);
						if (tn == null) {
							log.error("Unknown turnout *"+actionName[i]+" in cancelTurnoutTimer.");
						}
						else if (sname.equals(tn.getSystemName()) || 
												sname.equals(tn.getUserName())) {
							// same turnout, cancel timer
							mDelayTimer[i].stop();
							mDelayTimerActive[i] = false;
						}
					}
				}
			}
		}
	}
	
    /**
     * State of the Conditional is returned.  
     * @return state value
     */
    public int getState() {
        return currentState;
    }
    
    /**
     * State of Conditional is set. Not really public for Conditionals.
	 * The state of a Conditional is only changed by its calculate method, so the state is
	 *    really a read-only bound property.
     */
    public void setState(int state) {
        if (currentState != state) {
            int oldState = currentState;
            currentState = state;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(currentState));
        }
    }
	
	/**
	 *	Class for defining ActionListener for ACTION_DELAYED_SENSOR
	 */
	class TimeSensor implements java.awt.event.ActionListener 
	{
		public TimeSensor(int index) {
			mIndex = index;
		}
		
		private int mIndex = 0;
		
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			// set sensor state
			Sensor sn = InstanceManager.sensorManagerInstance().
										getSensor(actionName[mIndex]);
			if (sn==null) {
				log.error("Invalid delayed sensor name - "+actionName[mIndex]);
			}
			else {
				// set the sensor
				try {
					sn.setKnownState(actionData[mIndex]);
				} 
				catch (JmriException e) {
					log.warn("Exception setting delayed sensor "+actionName[mIndex]+" in action");
				}
			}
			// Turn Timer OFF
			mDelayTimer[mIndex].stop();
			mDelayTimerActive[mIndex] = false;
		}
	}
	
	/**
	 *	Class for defining ActionListener for ACTION_DELAYED_TURNOUT
	 */
	class TimeTurnout implements java.awt.event.ActionListener 
	{
		public TimeTurnout(int index) {
			mIndex = index;
		}
		
		private int mIndex = 0;
		
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			// set turnout state
			Turnout t = InstanceManager.turnoutManagerInstance().
										getTurnout(actionName[mIndex]);
			if (t==null) {
				log.error("Invalid delayed turnout name - "+actionName[mIndex]);
			}
			else {
				// set the turnout
				t.setCommandedState(actionData[mIndex]);
			}
			// Turn Timer OFF
			mDelayTimer[mIndex].stop();
			mDelayTimerActive[mIndex] = false;
		}
	}

	
static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultConditional.class.getName());
}

/* @(#)DefaultConditional.java */

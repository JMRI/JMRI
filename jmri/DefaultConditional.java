// DefaultConditional.java

package jmri;
import jmri.Timebase;
import java.util.Date;
import javax.swing.Timer;

 /**
 * Class providing the basic logic of the Conditional interface.
 *
 * @author	Dave Duchamp Copyright (C) 2007
 * @version     $Revision: 1.1 $
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
	 protected String[] varSystemName = new String[MAX_STATE_VARIABLES];
	 protected String[] varDataString = new String[MAX_STATE_VARIABLES];
	 protected int[] varNum1 = new int[MAX_STATE_VARIABLES];
	 protected int[] varNum2 = new int[MAX_STATE_VARIABLES];
	 // action parameters - 2 actions allowed
	 protected int[] actionOption = {ACTION_OPTION_ON_CHANGE_TO_TRUE,
									ACTION_OPTION_ON_CHANGE_TO_TRUE};
	 protected int[] actionDelay = {0,0}; // delay before action (seconds)
	 protected int[] actionType = {ACTION_NONE,ACTION_NONE};
	 protected String[] actionSystemName = {" "," "};
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
    public boolean setStateVariables(int[] opern,int[] type,String[] systemName,
		String[] data,int[] num1,int[] num2,int numVariables) {
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
			varSystemName[i] = systemName[i];
			varDataString[i] = data[i];
			varNum1[i] = num1[i];
			varNum2[i] = num2[i];
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
    public void getStateVariables(int[] opern,int[] type,String[] systemName,
		String[] data,int[] num1,int[] num2) {
		if (numStateVariables == 0) {
			return;
		}
		// copy information from this Conditional's arrays
		for (int i = 0;i<numStateVariables;i++) {
			opern[i] = varOperator[i];
			type[i] = varType[i];
			systemName[i] = varSystemName[i];
			data[i] = varDataString[i];
			num1[i] = varNum1[i];
			num2[i] = varNum2[i];
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
	 * Provide access to System Name  of state variable by index
	 *  Note: index ranges from 0 to numStateVariables-1
	 */
	public String getStateVariableSystemName(int index) {
		if ( (index>=0) && (index<numStateVariables) ) {
			return (varSystemName[index]);
		}
		// illegal index
		log.warn("bad index in call to getStateVariableSystemName");
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
		log.warn("bad index in call to getStateVariableSystemName");
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
		log.warn("bad index in call to getStateVariableSystemName");
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
		log.warn("bad index in call to getStateVariableSystemName");
		return (-1);
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
				String[] systemName,int[] data,String[] s) {
		for (int i = 0;i<2;i++) {
			actionOption[i] = opt[i];
			actionDelay[i] = delay[i];
			actionType[i] = type[i];
			actionSystemName[i] = systemName[i];
			actionData[i] = data[i];
			actionString[i] = s[i];
		}
	}		
	
	/**
	 * Get action parameters for action 1 and action 2
	 */
	public void getAction (int[] opt, int[] delay, int[] type,
				String[] systemName,int[] data,String[] s) {
		for (int i = 0;i<2;i++) {
			opt[i] = actionOption[i];
			delay[i] = actionDelay[i];
			type[i] = actionType[i];
			systemName[i] = actionSystemName[i];
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
		String vSName = varSystemName[index];
		Sensor sn = null;
		Turnout t = null;
		SignalHead h = null;
		Conditional c = null;
		Light lgt = null;
		Memory m = null;
		// evaluate according to state variable type		
		switch (varType[index]) {
			case TYPE_SENSOR_ACTIVE:
				sn = InstanceManager.sensorManagerInstance().getBySystemName(vSName);
				if (sn == null) {
					log.error("invalid sensor system name in state variable - "+vSName);
					return (false);
				}
				if (sn.getState() == Sensor.ACTIVE) result = true;
				else result = false;
				break;
			case TYPE_SENSOR_INACTIVE:
				sn = InstanceManager.sensorManagerInstance().getBySystemName(vSName);
				if (sn == null) {
					log.error("invalid sensor system name in state variable - "+vSName);
					return (false);
				}
				if (sn.getState() == Sensor.INACTIVE) result = true;
				else result = false;
				break;
			case TYPE_TURNOUT_THROWN:
				t = InstanceManager.turnoutManagerInstance().getBySystemName(vSName);
				if (t == null) {
					log.error("invalid turnout system name in state variable - "+vSName);
					return (false);
				}
				if (t.getState() == Turnout.THROWN) result = true;
				else result = false;
				break;
			case TYPE_TURNOUT_CLOSED:
				t = InstanceManager.turnoutManagerInstance().getBySystemName(vSName);
				if (t == null) {
					log.error("invalid turnout system name in state variable - "+vSName);
					return (false);
				}
				if (t.getState() == Turnout.CLOSED) result = true;
				else result = false;
				break;
			case TYPE_CONDITIONAL_TRUE:
				c = InstanceManager.conditionalManagerInstance().getBySystemName(vSName);
				if (c == null) {
					log.error("invalid conditional system name in state variable - "+vSName);
					return (false);
				}
				if (c.getState() == TRUE) result = true;
				else result = false;
				break;
			case TYPE_CONDITIONAL_FALSE:
				c = InstanceManager.conditionalManagerInstance().getBySystemName(vSName);
				if (c == null) {
					log.error("invalid conditional system name in state variable - "+vSName);
					return (false);
				}
				if (c.getState() == FALSE) result = true;
				else result = false;
				break;
			case TYPE_LIGHT_ON:
				lgt = InstanceManager.lightManagerInstance().getBySystemName(vSName);
				if (lgt == null) {
					log.error("invalid light system name in state variable - "+vSName);
					return (false);
				}
				if (lgt.getState() == Light.ON) result = true;
				else result = false;
				break;
			case TYPE_LIGHT_OFF:
				lgt = InstanceManager.lightManagerInstance().getBySystemName(vSName);
				if (lgt == null) {
					log.error("invalid light system name in state variable - "+vSName);
					return (false);
				}
				if (lgt.getState() == Light.OFF) result = true;
				else result = false;
				break;
			case TYPE_MEMORY_EQUALS:
				m = InstanceManager.memoryManagerInstance().getBySystemName(vSName);
				if (m == null) {
					log.error("invalid memory system name in state variable - "+vSName);
					return (false);
				}
				String str = (String)m.getValue();
				if (str.equals(varDataString[index])) result = true; 
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
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.RED) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_YELLOW:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.YELLOW) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_GREEN:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.GREEN) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_DARK:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.DARK) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHRED:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHRED) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHYELLOW:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHYELLOW) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_FLASHGREEN:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				if (h.getAppearance() == SignalHead.FLASHGREEN) result = true;
				else result = false; 
				break;
			case TYPE_SIGNAL_HEAD_LIT:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
					return (false);
				}
				result = h.getLit(); 
				break;
			case TYPE_SIGNAL_HEAD_HELD:
				h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
				if (h == null) {
					log.error("invalid signal head system name in state variable - "+vSName);
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
									getBySystemName(actionSystemName[i]);
						if (t == null) {
							log.error("invalid turnout system name in action - "+actionSystemName[i]);
						}
						else {
							t.setCommandedState(actionData[i]);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
						h = InstanceManager.signalHeadManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (h == null) {
							log.error("invalid signal head system name in action - "+actionSystemName[i]);
						}
						else {
							h.setAppearance(actionData[i]);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (h == null) {
							log.error("invalid signal head system name in action - "+actionSystemName[i]);
						}
						else {
							h.setHeld(true);
						}
						break;
					case Conditional.ACTION_CLEAR_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (h == null) {
							log.error("invalid signal head system name in action - "+actionSystemName[i]);
						}
						else {
							h.setHeld(false);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_DARK:
						h = InstanceManager.signalHeadManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (h == null) {
							log.error("invalid signal head system name in action - "+actionSystemName[i]);
						}
						else {
							h.setLit(false);
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_LIT:
						h = InstanceManager.signalHeadManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (h == null) {
							log.error("invalid signal head system name in action - "+actionSystemName[i]);
						}
						else {
							h.setLit(true);
						}
						break;
					case Conditional.ACTION_TRIGGER_ROUTE:
						Route r = InstanceManager.routeManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (r == null) {
							log.error("invalid route system name in action - "+actionSystemName[i]);
						}
						else {
							r.setRoute();
						}
						break;
					case Conditional.ACTION_SET_SENSOR:
						Sensor sn = InstanceManager.sensorManagerInstance().
									getBySystemName(actionSystemName[i]);
						if (sn == null) {
							log.error("invalid sensor system name in action - "+actionSystemName[i]);
						}
						else {
							try {
								sn.setKnownState(actionData[i]);
							} 
							catch (JmriException e) {
								log.warn("Exception setting sensor "+varSystemName[i]+" in action");
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
							log.warn("timer already active on request to start - delayed sensor action - "+
																				actionSystemName[i]);
						}
						break;
					case Conditional.ACTION_SET_LIGHT:
						Light lgt = InstanceManager.lightManagerInstance().
										getBySystemName(actionSystemName[i]);
						if (lgt == null) {
							log.error("invalid light system name in action - "+actionSystemName[i]);
						}
						else {
							lgt.setState(actionData[i]);
						}
						break;
					case Conditional.ACTION_SET_MEMORY:
						Memory m = InstanceManager.memoryManagerInstance().
										getBySystemName(actionSystemName[i]);
						if (m == null) {
							log.error("invalid memory system name in action - "+actionSystemName[i]);
						}
						else {
							m.setValue(actionString[i]);
						}
						break;
					case Conditional.ACTION_ENABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getBySystemName(actionSystemName[i]);
						if (x == null) {
							log.error("invalid logix system name in action - "+actionSystemName[i]);
						}
						else {
							x.setEnabled(true);
						}
						break;
					case Conditional.ACTION_DISABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getBySystemName(actionSystemName[i]);
						if (x == null) {
							log.error("invalid logix system name in action - "+actionSystemName[i]);
						}
						else {
							x.setEnabled(false);
						}
						break;
					case Conditional.ACTION_PLAY_SOUND:
						if (!(actionString[i].equals(""))) {
							jmri.jmrit.Sound snd = new jmri.jmrit.Sound(actionString[i]);
							snd.play();
						}
						break;
					case Conditional.ACTION_RUN_SCRIPT:
						if (!(actionString[i].equals(""))) {
							jmri.util.PythonInterp.runScript(actionString[i]);
						}
						break;
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
										getBySystemName(actionSystemName[mIndex]);
			if (sn==null) {
				log.error("Invalid delayed sensor system name - "+actionSystemName[mIndex]);
			}
			else {
				// set the sensor
				try {
					sn.setKnownState(actionData[mIndex]);
				} 
				catch (JmriException e) {
					log.warn("Exception setting delayed sensor "+actionSystemName[mIndex]+" in action");
				}
			}
			// Turn Timer OFF
			mDelayTimer[mIndex].stop();
			mDelayTimerActive[mIndex] = false;
		}
	}

	
static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultLogix.class.getName());
}

/* @(#)DefaultConditional.java */

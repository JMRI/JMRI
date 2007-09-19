// DefaultLogix.java

package jmri;
import jmri.Light;
import jmri.Conditional;
import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.Memory;
import jmri.Timebase;
import java.util.Date;

 /**
 * Class providing the basic logic of the Logix interface.
 *
 * @author	Dave Duchamp Copyright (C) 2007
 * @version     $Revision: 1.9 $
 */
public class DefaultLogix extends AbstractNamedBean
    implements Logix, java.io.Serializable {

    public DefaultLogix(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultLogix(String systemName) {
        super(systemName);
    }

    /**
     *  Persistant instance variables (saved between runs)
     */
	protected String[] mConditionalSystemNames = new String[MAX_CONDITIONALS];
	protected int[] mConditionalOrder = new int[MAX_CONDITIONALS];
	// Note: valid order values are from 0 to mNumConditionals-1

    /**
     *  Operational instance variables (not saved between runs)
     */
	protected int mNextConditionalNumber = 0;
    protected int mNumConditionals = 0;	
	private boolean mEnabled = true;
	private int mNextInOrder = 0;  // Used in reordering Conditionals
	
	private boolean mBusy = false;  // True if Logix is activated 
	private int mNumListeners = 0;  // number of listeners described below
	protected java.beans.PropertyChangeListener[] mListeners = 
							new java.beans.PropertyChangeListener[MAX_LISTENERS];
	protected String[] mListenerName = new String[MAX_LISTENERS];
	protected int[] mListenerVarType = new int[MAX_LISTENERS];
	protected int[] mListenerType = new int[MAX_LISTENERS];
	protected String[] mListenerProperty = new String[MAX_LISTENERS];
	protected int[] mListenerState = new int[MAX_LISTENERS];
	protected String[] mListenerData = new String[MAX_LISTENERS];
	
	protected int mNumMinuteListenerTimes = 0;  // number of fast clock ranges 
	protected int[] mMinuteBeginTime = new int[MAX_LISTENERS];
	protected int[] mMinuteEndTime = new int[MAX_LISTENERS];
	protected java.beans.PropertyChangeListener mFastClockListener = null;
	protected Timebase mFastClock = null;
	protected int mCurrentMinutes = 0;
		
	/**
	 * Get number of Conditionals for this Logix
	 */
	public int getNumConditionals() {
		return (mNumConditionals);
	}
	
	/**
	 * Initialize for reordering Conditionals
	 */
	public void initializeReorder() {
		mNextInOrder = 0;
	}
	
	/**
	 * Get never used number for next Conditional system name
	 */
	public int getNextConditionalNumber() {
		mNextConditionalNumber ++;
		return (mNextConditionalNumber);
	}
	
	/**
	 * Make the conditional with given current order, the next in order
	 * Returns 'true' if reordering is done, returns 'false' if continue
	 */
	public boolean nextConditionalInOrder(int oldOrder) {
		// check that reordering will work OK
		if ( (mNextInOrder < mNumConditionals-1) && (oldOrder < mNumConditionals) ) {
			for (int i=0;i<mNumConditionals;i++) {
				if (mConditionalOrder[i]==oldOrder) {
					mConditionalOrder[i] = mNextInOrder;
				}
				else if ( (mConditionalOrder[i]>=mNextInOrder) &&
						(mConditionalOrder[i]<oldOrder) ) {
					mConditionalOrder[i] ++; 
				}
			}
			mNextInOrder ++;
			if (mNextInOrder==mNumConditionals-1) return true;
			return false;
		}
		else {
			// nonsense call
			log.error("Nonsense call to Logix.nextConditionalInOrder.");
			return true;
		}
	}
	
	String emptyString = "";
	
	/**
	 * Returns the system name of the conditional that will calculate in the
	 * specified order. This is also the order the Conditional is listed in
	 * the Add/Edit Logix dialog.
	 * If 'order' is greater than the number of Conditionals for this Logix,
	 * and empty String is returned.
	 * @param order - order in which the Conditional calculates.
	 */	
	public String getConditionalByNumberOrder(int order) {
		for (int i=0;i<mNumConditionals;i++) {
			if (mConditionalOrder[i]==order) return mConditionalSystemNames[i];
		}
		return (emptyString);
	}
	
	/**
     * Add a Conditional to this Logix
	 * Returns true if Conditional was successfully added, returns false
	 * if the maximum number of conditionals has been exceeded.
     * @param systemName The Conditional system name
	 * @param order - the order this conditional should calculate in
	 *                 if order is negative, the conditional is added
	 *				   at the end of current group of conditionals
     */
    public boolean addConditional(String systemName,int order) {
		if (mNumConditionals<MAX_CONDITIONALS) {
			mConditionalSystemNames[mNumConditionals] = systemName;
			if (order>=0) {
				// specific order entered
    // add check that order is unique and legal
				mConditionalOrder[mNumConditionals] = order;
			}
			else {
				// add to end of current order
				mConditionalOrder[mNumConditionals] = mNumConditionals;
			}
			mNumConditionals ++;
			// Get conditional number
			int cNum=0;
			// find the last 'C' in the System Name
			for (int i = systemName.length()-1; i>=2; i--) {
				if (systemName.charAt(i) == 'C') {
					cNum = Integer.valueOf(systemName.substring(i+1)).intValue();
					break;
				}
            }
			if (cNum==0) {
				// Error 'C' not found in Conditional system name
				log.error("Invalid conditional system name, no C - "+systemName);
			}
			// Insure that next Conditional created will have a larger number
			if (cNum > mNextConditionalNumber) {
				mNextConditionalNumber = cNum;
			}
			return (true);
		}
		// Too many Conditionals
		return (false);
	}

    /**
     * Set enabled status.  Enabled is a bound property
	 *   All conditionals are set to UNKNOWN state and recalculated
	 *		when the Logix is enabled, provided the Logix has been 
	 *		previously activated.
     */
    public void setEnabled(boolean state) { 
        boolean old = mEnabled;
        mEnabled = state;
        if (old != state) {
			firePropertyChange("Enabled", new Boolean(old), new Boolean(state));
			// set the state of all Conditionals to UNKNOWN
			if ( mBusy && (mNumConditionals> 0) ) {
				Conditional c = null;
				for (int i = 0;i<mNumConditionals;i++) {
					c = InstanceManager.conditionalManagerInstance().
										getBySystemName(mConditionalSystemNames[i]);
					if (c!=null) {
						c.setState(Conditional.UNKNOWN);
					}
				}
				calculateConditionals();
			}
		}
    }

    /**
     * Get enabled status
    */
    public boolean getEnabled() { 
		return mEnabled; 
	}

	/**
     * Delete a Conditional and remove it from this Logix
     * <P>
	 * Note: Since each Logix must have at least one Conditional to
	 *	do anything, the user is warned in Logix Table Action when the 
	 *  last Conditional is deleted.
     * <P>
	 * Returns true if Conditional was successfully deleted, otherwise 
	 *  returns false. 
     * @param systemName The Conditional system name
     */
    public boolean deleteConditional(String systemName) {
		if (mNumConditionals<=0) {
			log.error("attempt to delete Conditional, none in Logix: "+systemName);
			return (false);
		}
		// find the Conditional to delete
		int found = -1;
		for (int i = 0;(i<mNumConditionals)&&(found<0);i++) {
			if (systemName.equals(mConditionalSystemNames[i])) {
				found = i;
			}
		}
		if (found<0) {
			// Not found
			log.error("attempt to delete Conditional not in Logix: "+systemName);
			return (false);
		}
		// have located Conditional to delete - remove from this Logix
		int oldOrder = mConditionalOrder[found];
		if ( (mNumConditionals>1) && (oldOrder<mNumConditionals-1) ) {
			// update the order of any remaining Conditionals if needed
			for (int i = 0;i<mNumConditionals;i++) {
				if (mConditionalOrder[i]>oldOrder) {
					mConditionalOrder[i] --;
				}
			}
		}
		if (found < mNumConditionals-1) {
			// remove from array and move up remaining Conditionals as needed
			for (int i = found;i<mNumConditionals-1;i++) {
				mConditionalSystemNames[i] = mConditionalSystemNames[i+1];
				mConditionalOrder[i] = mConditionalOrder[i+1];
			}
		}
		mNumConditionals --;
		// delete the Conditional object
		Conditional c = InstanceManager.conditionalManagerInstance().
											getBySystemName(systemName);
		if (c==null) {
			log.error("attempt to delete non-existant Conditional - "+systemName);
			return (false);
		}
		InstanceManager.conditionalManagerInstance().deleteConditional(c);
		return (true);
	}	
	
    /**
	 * Calculate all Conditionals, triggering action if the user specified
	 *   conditions are met, and the Logix is enabled.
	 */
	public void calculateConditionals() {
		// are there Conditionals to calculate?
		if (mNumConditionals>0) {
			// There are conditionals to calculate
			String cName = "";
			Conditional c = null;
			for (int i = 0;i<mNumConditionals;i++) {
				cName = getConditionalByNumberOrder(i);
				c = InstanceManager.conditionalManagerInstance().getBySystemName(cName);
				if (c==null) {
					log.error("Invalid conditional system name when calculating Logix - "+cName);
				}
				else {
					// calculate without taking any action unless Logix is enabled
					c.calculate(mEnabled);
				}
			}
		}
	}
	
    /**
     * Activate the Logix, starts Logix processing by connecting all
	 *    inputs that are included the Conditionals in this Logix.
     * <P>
     * A Logix must be activated before it will calculate any of its
	 *    Conditionals.
     */
    public void activateLogix() {
		// if the Logix is already busy, simply return
		if (mBusy) return;
		// set the state of all Conditionals to UNKNOWN
		if (mNumConditionals> 0) {
			Conditional c = null;
			for (int i = 0;i<mNumConditionals;i++) {
				c = InstanceManager.conditionalManagerInstance().
											getBySystemName(mConditionalSystemNames[i]);
				if (c!=null) {
					c.setState(Conditional.UNKNOWN);
				}
			}
		}		
		// assemble a list of needed listeners
		assembleListenerList();
		// create and attach the needed property change listeners
		if (mNumListeners>0) {
			for (int i = 0;i<mNumListeners;i++) {
				createStartListener(i);
			}
		}
		// start a minute Listener if needed
		if (mNumMinuteListenerTimes>0) {
			// start a minute timer if needed
			mFastClock = InstanceManager.timebaseInstance();
			Date currentTime = mFastClock.getTime();
			mCurrentMinutes = (currentTime.getHours()*60) + currentTime.getMinutes();
			mFastClockListener =  new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if (mBusy) 
						checkFastClock();
				}
			};
			mFastClock.addMinuteChangeListener(mFastClockListener);
		}
		// mark this Logix as busy
		mBusy = true;
		// calculate this Logix to set initial state of Conditionals
		calculateConditionals();
	}
	
	/**
	 * Check if have entered/exited one of the Fast Clock Ranges
	 * <P>
	 * This method is invoked when the minute listener fires.
	 */
	void checkFastClock() {
		// update current minutes since midnight
		Date currentTime = mFastClock.getTime();
		int oldMinutes = mCurrentMinutes;
		mCurrentMinutes = (currentTime.getHours()*60) + currentTime.getMinutes();
		// check if we have entered or left one of the ranges
		boolean inRange = false;
		for (int index = 0;(index<mNumMinuteListenerTimes)&&!inRange;index++) {
			// check if entered or left desired time range
			if (oldMinutes<mCurrentMinutes) {
				// minutes not crossing midnight, test ends of range
				if ( ((oldMinutes<mMinuteBeginTime[index]) && 
					(mCurrentMinutes>=mMinuteBeginTime[index])) ||
						((oldMinutes<mMinuteEndTime[index]) && 
							(mCurrentMinutes>=mMinuteEndTime[index])) ) {
					inRange = true;
				}
			}
			else {
				// minutes crossing midnight, is range crossing midnight
				if (mMinuteBeginTime[index]<=mMinuteEndTime[index]) {
					// range not crossing midnight
					if ( ( (oldMinutes<mMinuteBeginTime[index]) && 
						((mCurrentMinutes+1440)>=mMinuteBeginTime[index]) ) ||
							( (oldMinutes<=mMinuteEndTime[index]) && 
								((mCurrentMinutes+1440)>mMinuteEndTime[index]) ) ) {
						inRange = true;
					}
				}
				else {
					// both cross midnight 
					if ( ( (oldMinutes<mMinuteBeginTime[index]) && 
						((mCurrentMinutes+1440)>=mMinuteBeginTime[index]) ) ||
							( ((oldMinutes-1440)<mMinuteEndTime[index]) && 
								(mCurrentMinutes>=mMinuteEndTime[index])) ) {
						inRange = true;
					}
				}
			}
		}
		if (inRange) {
			// went over one end of some range, calculate
			calculateConditionals();
		}
	}
		
	/**
	 * Assembles a list of Listeners needed to activate this Logix
	 * Also identifies the need(s) for monitoring the fast clock
	 */
	private void assembleListenerList() {
		// initialize
		mNumListeners = 0;
		mNumMinuteListenerTimes = 0;
		Conditional c = null;
		int numVars = 0;
		int varType = 0;
		String varName = "";
		boolean newSV = true;
		// cycle thru Conditionals to find objects to listen to
		for (int i = 0;i<mNumConditionals;i++) {
			c = InstanceManager.conditionalManagerInstance().
											getBySystemName(mConditionalSystemNames[i]);
			if (c!=null) {
				numVars = c.getNumStateVariables();
				if (numVars>0) {
					// cycle thru state variables for this Conditional
					for (int k = 0;k<numVars;k++) {
						// check if listening for a change has been suppressed
						if (c.getStateVariableTriggersCalculation(k)) {
							// not suppressed
							varName = c.getStateVariableName(k);
							varType = c.getStateVariableType(k);
							newSV = true;
							if (mNumListeners>0) {
								// check if already in list
								for (int j = 0;(j<mNumListeners)&&newSV;j++) {
									if (varType==mListenerVarType[j]) {
										if (varName.equals(mListenerName[j])) 
											newSV = false;
									}
								}
							}
							// make sure this is not one of this logix's conditionals
							if ( (varType==Conditional.TYPE_CONDITIONAL_TRUE) ||
									(varType==Conditional.TYPE_CONDITIONAL_FALSE) ) {
								// check this logix's conditionals
								for (int n = 0;n<mNumConditionals;n++) {
									// check for system name within this logix
									if (varName.equals(mConditionalSystemNames[n])) {
										newSV = false;
									}
									else {
										// check for user name within this logix
										Conditional cxx = InstanceManager.conditionalManagerInstance().
												getBySystemName(mConditionalSystemNames[n]);
										if (cxx!=null) {
											if (varName.equals(cxx.getUserName())) {
												newSV = false;
											}
										}
									}
								}
							}
						
							// add to list if new
							if (newSV) {
								mListenerName[mNumListeners] = varName;
								mListenerVarType[mNumListeners] = varType;
								mListenerData[mNumListeners] = "";
								switch (varType) {
									case Conditional.TYPE_SENSOR_ACTIVE:
										mListenerType[mNumListeners] = LISTENER_TYPE_SENSOR;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Sensor.ACTIVE;
										break;
									case Conditional.TYPE_SENSOR_INACTIVE:
										mListenerType[mNumListeners] = LISTENER_TYPE_SENSOR;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Sensor.INACTIVE;
										break;
									case Conditional.TYPE_TURNOUT_THROWN:
										mListenerType[mNumListeners] = LISTENER_TYPE_TURNOUT;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Turnout.THROWN;
										break;
									case Conditional.TYPE_TURNOUT_CLOSED:
										mListenerType[mNumListeners] = LISTENER_TYPE_TURNOUT;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Turnout.CLOSED;
										break;
									case Conditional.TYPE_CONDITIONAL_TRUE:
										mListenerType[mNumListeners] = LISTENER_TYPE_CONDITIONAL;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Conditional.TRUE;
										break;
									case Conditional.TYPE_CONDITIONAL_FALSE:
										mListenerType[mNumListeners] = LISTENER_TYPE_CONDITIONAL;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Conditional.FALSE;
										break;
									case Conditional.TYPE_LIGHT_ON:
										mListenerType[mNumListeners] = LISTENER_TYPE_LIGHT;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Light.ON;
										break;
									case Conditional.TYPE_LIGHT_OFF:
										mListenerType[mNumListeners] = LISTENER_TYPE_LIGHT;
										mListenerProperty[mNumListeners] = "KnownState";
										mListenerState[mNumListeners] = Light.OFF;
										break;
									case Conditional.TYPE_MEMORY_EQUALS:
										mListenerType[mNumListeners] = LISTENER_TYPE_MEMORY;
										mListenerProperty[mNumListeners] = "value";
										mListenerData[mNumListeners] = c.getStateVariableDataString(k);
										mListenerState[mNumListeners] = 0;
										break;
									case Conditional.TYPE_FAST_CLOCK_RANGE:
										int begin = c.getStateVariableNum1(k);
										int end = c.getStateVariableNum2(k);
										boolean need = true;
										if (mNumMinuteListenerTimes>0) {
											for (int n = 0;(n<mNumMinuteListenerTimes)&&need;n++) {
												if ( (begin==mMinuteBeginTime[mNumMinuteListenerTimes]) &&
														(end==mMinuteEndTime[mNumMinuteListenerTimes]) ) {
													need = false;
												}
											}
										}
										if (need) {
											// add listening times
											mMinuteBeginTime[mNumMinuteListenerTimes] = begin;
											mMinuteEndTime[mNumMinuteListenerTimes] = end;
											mNumMinuteListenerTimes ++;
										}
										break;
									case Conditional.TYPE_SIGNAL_HEAD_RED:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.RED;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] =  SignalHead.YELLOW;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_GREEN:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.GREEN;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_DARK:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.DARK;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.FLASHRED;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.FLASHYELLOW;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Appearance";
										mListenerState[mNumListeners] = SignalHead.FLASHGREEN;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_LIT:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Lit";
										mListenerState[mNumListeners] = 0;
										break;
									case Conditional.TYPE_SIGNAL_HEAD_HELD:
										mListenerType[mNumListeners] = LISTENER_TYPE_SIGNAL;
										mListenerProperty[mNumListeners] = "Held";
										mListenerState[mNumListeners] = 0;
										break;
								}
								if (varType!=Conditional.TYPE_FAST_CLOCK_RANGE)
									mNumListeners ++;
							}
						}
					}
				}
			}
			else {
				log.error("invalid conditional system name in Logix assembleListenerList - "+
															mConditionalSystemNames[i]);
			}
		}
	}
	
	/**
	 * Assembles and returns a list of state variables that are used by conditionals 
	 *   of this Logix including the number of occurances of each variable that 
	 *   trigger a calculation, and the number of occurances where the triggering 
	 *   has been suppressed.
	 * The main use of this method is to return information that can be used to test 
	 *   for inconsistency in suppressing triggering of a calculation among multiple 
	 *   occurances of the same state variable.
	 * Note that FastClockRange state varible type is not returned, since each 
	 *   occurance is considered a unique state variable - there is no duplication 
	 *   possible.
	 * Returns the number of state variables returned.
	 * Note that 'arrayMax' is the dimension of the arrays passed in the call.  If 
	 *   more state variables are found than 'arrayMax', the overflow is skipped.
	 */
	public int getStateVariableList(String[] varName, int[] varListenerType, 
			String[] varListenerProperty, int[] varAppearance, int[] numTriggersCalc, 
								int[] numTriggerSuppressed, int arrayMax) {  
		// initialize
		int numVariables = 0;   // counts variables placed in the returned arrays
		Conditional c = null;
		int numVars = 0;
		String testSystemName = "";
		String testUserName = "";
		String testVarName = "";
		int testListenerType = 0;
		int currentVar = 0;
		String testListenerProperty = "";
		// cycle thru Conditionals to find state variables
		for (int i = 0;i<mNumConditionals;i++) {
			c = InstanceManager.conditionalManagerInstance().
										getBySystemName(mConditionalSystemNames[i]);
			if (c!=null) {
				numVars = c.getNumStateVariables();
				if (numVars>0) {
					// cycle thru state variables for this Conditional
					for (int k = 0;k<numVars;k++) {
						testVarName = c.getStateVariableName(k);
						testSystemName = "";
						testUserName = "";
						// initialize this state variable
						switch (c.getStateVariableType(k)) {
							case Conditional.TYPE_SENSOR_ACTIVE:
							case Conditional.TYPE_SENSOR_INACTIVE:
								testListenerType = LISTENER_TYPE_SENSOR;
								testListenerProperty = "KnownState";
								Sensor s = InstanceManager.sensorManagerInstance().
													getSensor(testVarName);
								if (s!=null) {
									testSystemName = s.getSystemName();
									testUserName = s.getUserName();
								}
								break;
							case Conditional.TYPE_TURNOUT_THROWN:
							case Conditional.TYPE_TURNOUT_CLOSED:
								testListenerType = LISTENER_TYPE_TURNOUT;
								testListenerProperty = "KnownState";
								Turnout t = InstanceManager.turnoutManagerInstance().
													getTurnout(testVarName);
								if (t!=null) {
									testSystemName = t.getSystemName();
									testUserName = t.getUserName();
								}
								break;
							case Conditional.TYPE_CONDITIONAL_TRUE:
							case Conditional.TYPE_CONDITIONAL_FALSE:
								testListenerType = LISTENER_TYPE_CONDITIONAL;
								testListenerProperty = "KnownState";
								Conditional cx = InstanceManager.conditionalManagerInstance().
													getConditional(this,testVarName);
								if (cx==null) {
									cx = InstanceManager.conditionalManagerInstance().
													getBySystemName(testVarName);
								}
								if (cx!=null) {
									testSystemName = cx.getSystemName();
									testUserName = cx.getUserName();
								}
								break;
							case Conditional.TYPE_LIGHT_ON:
							case Conditional.TYPE_LIGHT_OFF:
								testListenerType = LISTENER_TYPE_LIGHT;
								testListenerProperty = "KnownState";
								Light lgt = InstanceManager.lightManagerInstance().
													getLight(testVarName);
								if (lgt!=null) {
									testSystemName = lgt.getSystemName();
									testUserName = lgt.getUserName();
								}
								break;
							case Conditional.TYPE_MEMORY_EQUALS:
								testListenerType = LISTENER_TYPE_MEMORY;
								testListenerProperty = "Value";
								Memory m = InstanceManager.memoryManagerInstance().
													getMemory(testVarName);
								if (m!=null) {
									testSystemName = m.getSystemName();
									testUserName = m.getUserName();
								}
								break;
							case Conditional.TYPE_SIGNAL_HEAD_RED:
							case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
							case Conditional.TYPE_SIGNAL_HEAD_GREEN:
							case Conditional.TYPE_SIGNAL_HEAD_DARK:
							case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
							case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
							case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
								testListenerType = LISTENER_TYPE_SIGNAL;
								testListenerProperty = "Appearance";
								SignalHead h = InstanceManager.signalHeadManagerInstance().
													getSignalHead(testVarName);
								if (h!=null) {
									testSystemName = h.getSystemName();
									testUserName = h.getUserName();
								}
								break;
							case Conditional.TYPE_SIGNAL_HEAD_LIT:
								testListenerType = LISTENER_TYPE_SIGNAL;
								testListenerProperty = "Lit";
								SignalHead hx = InstanceManager.signalHeadManagerInstance().
													getSignalHead(testVarName);
								if (hx!=null) {
									testSystemName = hx.getSystemName();
									testUserName = hx.getUserName();
								}
								break;
							case Conditional.TYPE_SIGNAL_HEAD_HELD:
								testListenerType = LISTENER_TYPE_SIGNAL;
								testListenerProperty = "Held";
								SignalHead hy = InstanceManager.signalHeadManagerInstance().
													getSignalHead(testVarName);
								if (hy!=null) {
									testSystemName = hy.getSystemName();
									testUserName = hy.getUserName();
								}
								break;
							default:
								testSystemName = "";
						}
						// complete initialization for signal head appearance
						int testAppearance = 0;
						if (testListenerProperty.equals("Appearance")) {
							switch (c.getStateVariableType(k)) {
							case Conditional.TYPE_SIGNAL_HEAD_RED:
								testAppearance = SignalHead.RED;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
								testAppearance = SignalHead.YELLOW;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_GREEN:
								testAppearance = SignalHead.GREEN;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_DARK:
								testAppearance = SignalHead.DARK;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
								testAppearance = SignalHead.FLASHRED;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
								testAppearance = SignalHead.FLASHYELLOW;
								break;
							case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
								testAppearance = SignalHead.FLASHGREEN;
								break;
							}
						}
						// check if this state variable is already in the list to be returned
						if (testSystemName!="") {
							// getXXXXXX succeeded, process this state variable
							boolean svNew = true;
							if (numVariables>0) {
								for (int index=0;(index<numVariables)&&svNew;index++) {
									if ( ((varName[index].equals(testSystemName)) ||
											(varName[index].equals(testUserName)) ) &&
											(varAppearance[index] == testAppearance) &&
											(varListenerType[index] == testListenerType) &&
											(varListenerProperty[index] == testListenerProperty) ) {
										svNew = false;
										currentVar = index;
									}
								}
							}
							// add to list if new and if there is room
							if ( (svNew) && (numVariables < arrayMax) ) {
								varName[numVariables] = testVarName;
								varListenerType[numVariables] = testListenerType;
								varListenerProperty[numVariables] = testListenerProperty;
								varAppearance[numVariables] = testAppearance;
								numTriggersCalc[numVariables] = 0;
								numTriggerSuppressed[numVariables] = 0;
								currentVar = numVariables;
								numVariables ++;
							}
							// increment the triggers/suppressed counts
							if (c.getStateVariableTriggersCalculation(k))
								numTriggersCalc[currentVar] ++;
							else
								numTriggerSuppressed[currentVar] ++;
						}
					}
				}
			}
			else {
				log.error("invalid conditional system name in Logix getStateVariableList - "+
															mConditionalSystemNames[i]);
			}
		}
		// Have cycled thru all state variables
		return (numVariables);
	}
	
	/**
	 * Creates a listener of the required type and starts it
	 */
	private void createStartListener(int index) {
		switch (mListenerType[index]) {
			case LISTENER_TYPE_SENSOR:
				Sensor s = InstanceManager.sensorManagerInstance().
										provideSensor(mListenerName[index]);
				if (s==null) {
					log.error("Bad name for sensor \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Sensor within this Logix
				mListeners[index] = new StateListener(index);
				s.addPropertyChangeListener (mListeners[index]);
				break;
			case LISTENER_TYPE_TURNOUT:
				Turnout t = InstanceManager.turnoutManagerInstance().
										provideTurnout(mListenerName[index]);
				if (t==null) {
					log.error("Bad name for turnout \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Turnout within this Logix
				mListeners[index] = new StateListener(index);
				t.addPropertyChangeListener (mListeners[index]);
				break;
			case LISTENER_TYPE_LIGHT:
				Light lgt = InstanceManager.lightManagerInstance().
										getLight(mListenerName[index]);
				if (lgt==null) {
					log.error("Bad name for light \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Light within this Logix
				mListeners[index] = new StateListener(index);
				lgt.addPropertyChangeListener (mListeners[index]);
				break;
			case LISTENER_TYPE_CONDITIONAL:
				Conditional c = InstanceManager.conditionalManagerInstance().
										getBySystemName(mListenerName[index]);
				if (c==null) {
					log.error("Bad system name for conditional \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Conditional in other Logix referenced within this Logix
				mListeners[index] = new StateListener(index);
				c.addPropertyChangeListener (mListeners[index]);
				break;
			case LISTENER_TYPE_SIGNAL:
				SignalHead h = InstanceManager.signalHeadManagerInstance().
										getSignalHead(mListenerName[index]);
				if (h==null) {
					log.error("Bad name for signal head \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Signal Heads within this Logix
				mListeners[index] = new SignalHeadListener(index);
				h.addPropertyChangeListener (mListeners[index]);
				break;
			case LISTENER_TYPE_MEMORY:
				Memory m = InstanceManager.memoryManagerInstance().
										provideMemory(mListenerName[index]);
				if (m==null) {
					log.error("Bad name for memory \""+mListenerName[index]+
									"\" when setting up Logix listener");
					return;
				}
				// Add listener for Memory within this Logix
				mListeners[index] = new MemoryListener(index);
				m.addPropertyChangeListener (mListeners[index]);
				break;
		}
	}

    /**
     * Deactivate the Logix. This method disconnects the Logix from
     *    all input objects and stops it from being triggered to calculate.
     * <P>
     * A Logix must be deactivated before it's Conditionals are
	 *   changed.
     */
    public void deActivateLogix() {
		if (mBusy) {
			// Logix is active, deactivate it and all listeners
			mBusy = false;
			// remove listeners if there are any
			if (mNumListeners>0) {
				for (int index = 0;index<mNumListeners;index++) {
					removeListener(index);
				}
			}
			// remove minute listener if there is one
			if (mFastClock!=null) {
				mFastClock.removeMinuteChangeListener(mFastClockListener);
				mFastClock = null;
			}
		}
	}
	
	/**
	 * Removes a listener of the required type
	 */
	private void removeListener(int index) {
		switch (mListenerType[index]) {
			case LISTENER_TYPE_SENSOR:
				Sensor s = InstanceManager.sensorManagerInstance().
										provideSensor(mListenerName[index]);
				if (s==null) {
					log.error("Bad name for sensor \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Sensor
				s.removePropertyChangeListener(mListeners[index]);
				break;
			case LISTENER_TYPE_TURNOUT:
				Turnout t = InstanceManager.turnoutManagerInstance().
										provideTurnout(mListenerName[index]);
				if (t==null) {
					log.error("Bad name for turnout \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Turnout
				t.removePropertyChangeListener(mListeners[index]);
				break;
			case LISTENER_TYPE_LIGHT:
				Light lgt = InstanceManager.lightManagerInstance().
										getLight(mListenerName[index]);
				if (lgt==null) {
					log.error("Bad name for light \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Light
				lgt.removePropertyChangeListener(mListeners[index]);
				break;
			case LISTENER_TYPE_CONDITIONAL:
				Conditional c = InstanceManager.conditionalManagerInstance().
										getBySystemName(mListenerName[index]);
				if (c==null) {
					log.error("Bad system name for conditional \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Conditional
				c.removePropertyChangeListener(mListeners[index]);
				break;
			case LISTENER_TYPE_SIGNAL:
				SignalHead h = InstanceManager.signalHeadManagerInstance().
										getSignalHead(mListenerName[index]);
				if (h==null) {
					log.error("Bad name for signal head \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Signal Head
				h.removePropertyChangeListener(mListeners[index]);
				break;
			case LISTENER_TYPE_MEMORY:
				Memory m = InstanceManager.memoryManagerInstance().
										provideMemory(mListenerName[index]);
				if (m==null) {
					log.error("Bad name for memory \""+mListenerName[index]+
									"\"when removing a Logix listener");
					return;
				}
				// remove listener for this Memory
				m.removePropertyChangeListener(mListeners[index]);
				break;
		}
	}
	
	/** 
	 * Assembles a list of state variables that both trigger the Logix, and are
	 *   changed by it.  Returns true if any such variables were found.  Returns false
	 *   otherwise.
	 * Note: This method can only work if the Logix is not activated.  If the Logix
	 *   is activated, no testing is done, and false is returned.
	 */
	public boolean checkLoopCondition() {
		if (!mBusy) {
			// Clear the string of possible offenders
			loopGremlins = "";
			numGremlins = 0;
			// Prepare a list of all listeners that will result on activation
			assembleListenerList();
			if (mNumListeners>0) {
				// listeners were found, initialize
				boolean[] conflictFound = new boolean[MAX_LISTENERS];
				for (int k = 0; k<mNumListeners; k++) {
					conflictFound[k] = false;
				}
				// check conditional action items
				Conditional c = null;
				int[] opt = {0,0};
				int[] delay = {0,0};
				int[] type = {0,0};
				String[] name = {" "," "};
				int[] data = {0,0};
				String[] dataString = {" "," "};
				for (int i = 0;i<mNumConditionals;i++) {
					// get next conditional
					c = InstanceManager.conditionalManagerInstance().
										getBySystemName(mConditionalSystemNames[i]);
					if (c!=null) {
						c.getAction(opt,delay,type,name,data,dataString);
						for (int j = 0; j<2; j++) {
							String sName = "";
							String uName = "";
							switch (type[j]) {
								case Conditional.ACTION_NONE:
									break;
								case Conditional.ACTION_SET_TURNOUT:
								case Conditional.ACTION_DELAYED_TURNOUT:
									Turnout t = InstanceManager.turnoutManagerInstance().
												provideTurnout(name[j]);
									if (t!=null) {
										sName = t.getSystemName();
										uName = t.getUserName();
										// check for listener on the same turnout
										for (int k = 0; k<mNumListeners; k++) {
											if ( (!conflictFound[k]) && 
												(mListenerType[k] == LISTENER_TYPE_TURNOUT) ) {
												if ( (mListenerName[k].equals(sName)) ||
														(mListenerName[k].equals(uName)) ) {
													// possible conflict found
													addGremlin(sName,uName);
													conflictFound[k] = true;
												}
											}
										}
									}
									break;
								case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
								case Conditional.ACTION_SET_SIGNAL_HELD:
								case Conditional.ACTION_CLEAR_SIGNAL_HELD:
								case Conditional.ACTION_SET_SIGNAL_DARK:
								case Conditional.ACTION_SET_SIGNAL_LIT:
									SignalHead h = InstanceManager.signalHeadManagerInstance().
													getSignalHead(name[j]);
									if (h!=null) {
										sName = h.getSystemName();
										uName = h.getUserName();
										// check for listener on the same signal head
										for (int k = 0; k<mNumListeners; k++) {
											if ( (!conflictFound[k]) && 
												(mListenerType[k] == LISTENER_TYPE_SIGNAL) ) {
												if ( (mListenerName[k].equals(sName)) ||
														(mListenerName[k].equals(uName)) ) {
													// possible conflict found
													addGremlin(sName,uName);
													conflictFound[k] = true;
												}
											}
										}
									}
									break;
								case Conditional.ACTION_SET_SENSOR:
								case Conditional.ACTION_DELAYED_SENSOR:
									Sensor s = InstanceManager.sensorManagerInstance().
												provideSensor(name[j]);
									if (s!=null) {
										sName = s.getSystemName();
										uName = s.getUserName();
										// check for listener on the same sensor
										for (int k = 0; k<mNumListeners; k++) {
											if ( (!conflictFound[k]) && 
												(mListenerType[k] == LISTENER_TYPE_SENSOR) ) {
												if ( (mListenerName[k].equals(sName)) ||
														(mListenerName[k].equals(uName)) ) {
													// possible conflict found
													addGremlin(sName,uName);
													conflictFound[k] = true;
												}
											}
										}
									}
									break;
								case Conditional.ACTION_SET_LIGHT:
									Light lgt = InstanceManager.lightManagerInstance().
													getLight(name[j]);
									if (lgt!=null) {
										sName = lgt.getSystemName();
										uName = lgt.getUserName();
										// check for listener on the same light
										for (int k = 0; k<mNumListeners; k++) {
											if ( (!conflictFound[k]) && 
												(mListenerType[k] == LISTENER_TYPE_LIGHT) ) {
												if ( (mListenerName[k].equals(sName)) ||
														(mListenerName[k].equals(uName)) ) {
													// possible conflict found
													addGremlin(sName,uName);
													conflictFound[k] = true;
												}
											}
										}
									}
									break;
								case Conditional.ACTION_SET_MEMORY:
									Memory m = InstanceManager.memoryManagerInstance().
												provideMemory(name[j]);
									if (m!=null) {
										sName = m.getSystemName();
										uName = m.getUserName();
										// check for listener on the same memory
										for (int k = 0; k<mNumListeners; k++) {
											if ( (!conflictFound[k]) && 
												(mListenerType[k] == LISTENER_TYPE_MEMORY) ) {
												if ( (mListenerName[k].equals(sName)) ||
														(mListenerName[k].equals(uName)) ) {
													// possible conflict found
													addGremlin(sName,uName);
													conflictFound[k] = true;
												}
											}
										}
									}
									break;
								default:
									break;
							}							
						}
					}
				}
				if (numGremlins>0) return (true);
			}
		}
		// work in progress
		return (false);
	}
	private void addGremlin(String sName, String uName) {
		numGremlins ++;
		if (numGremlins>1) loopGremlins = loopGremlins+" ,";
		loopGremlins = loopGremlins+" "+sName+"( "+uName+" )";
	}
	
	int numGremlins = 0;
	String loopGremlins = "";
	 
	/** 
	 * Returns a string listing state variables that might result in a loop.
	 *    Returns an empty string if there are none, probably because 
	 *    "checkLoopCondition" was not invoked before the call, or returned false.
	 */
	public String getLoopGremlins() {return(loopGremlins);}

    /**
     * Not needed for Logixs - included to complete implementation of the NamedBean interface.
     */
    public int getState() {
        log.warn("Unexpected call to getState in DefaultLogix.");
        return UNKNOWN;
    }
    
    /**
     * Not needed for Logixs - included to complete implementation of the NamedBean interface.
     */
    public void setState(int state) {
        log.warn("Unexpected call to setState in DefaultLogix.");
        return;
    }
	
	/**
	 *	Class for defining PropertyChangeListener for Sensors, Turnouts,
	 *				Lights, and Conditionals
	 */
	class StateListener implements java.beans.PropertyChangeListener  
	{
		public StateListener (int index) {
			mIndex = index;
		}
		
		private int mIndex = 0;
		
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (mBusy) {
				// check for change in State property if Logix is active
				String pName = event.getPropertyName();
				if (pName.equals(mListenerProperty[mIndex])) {
					int newState = ((Integer) event.getNewValue()).intValue();
					int oldState = ((Integer) event.getOldValue()).intValue();
					if ( (newState == mListenerState[mIndex]) || 
								(oldState == mListenerState[mIndex]) ) {
						// property has changed to/from the watched state, calculate
						calculateConditionals();
					}
				}
			}
		}
	}
	
	/**
	 *	Class for defining PropertyChangeListener for Signal Heads
	 */
	class SignalHeadListener implements java.beans.PropertyChangeListener  
	{
		public SignalHeadListener (int index) {
			mIndex = index;
		}
		
		private int mIndex = 0;
		
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (mBusy) {
				// check for change in Signal Head property if Logix is active
				String pName = event.getPropertyName();
				if (pName.equals(mListenerProperty[mIndex])) {
					if ( (pName.equals("Lit")) || (pName.equals("Held")) ){
						// property has changed, calculate this Logix
						calculateConditionals();
					}
					else if (pName.equals("Appearance")) {
						int newAppearance = ((Integer) event.getNewValue()).intValue();
						int oldAppearance = ((Integer) event.getOldValue()).intValue();
						if ( (newAppearance == mListenerState[mIndex]) ||
										(oldAppearance == mListenerState[mIndex]) ) {
							// property has changed to/from the watched appearance, calculate
							calculateConditionals();
						}
					}
				}
			}
		}
	}
	
	/**
	 *	Class for defining PropertyChangeListener for Memorys
	 */
	class MemoryListener implements java.beans.PropertyChangeListener  
	{
		public MemoryListener (int index) {
			mIndex = index;
		}
		
		private int mIndex = 0;
		
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (mBusy) {
				// check for change in Memory property if Logix is active
				String pName = event.getPropertyName();
				if (log.isDebugEnabled())
				    log.debug("Memory event for property "+pName+
				        ", old value =\""+event.getOldValue()+
				        "\", new value =\""+event.getNewValue()+"\"");
				if (pName.equals(mListenerProperty[mIndex])) {
					String newValue = (String) event.getNewValue();
					String oldValue = (String) event.getOldValue();
				    if (newValue == null) return;
				    if (oldValue == null) return;
					if ( ( newValue!=null && newValue.equals(mListenerData[mIndex]) ) ||
							(oldValue!=null && oldValue.equals(mListenerData[mIndex]) ) ) {
						// property has changed to/from the watched state, calculate
						calculateConditionals();
					}
				}
			}
		}
	}
		    
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultLogix.class.getName());
}

/* @(#)DefaultLogix.java */

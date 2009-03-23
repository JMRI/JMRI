package jmri;

import jmri.Timebase;
import jmri.jmrit.Sound;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.ResourceBundle;
import java.beans.PropertyChangeEvent;
import javax.swing.Timer;

 /**
 * Class providing the basic logic of the Conditional interface.
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
 *
 * @author	Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009
 * @version     $Revision: 1.17 $
 */
public class DefaultConditional extends AbstractNamedBean
    implements Conditional, java.io.Serializable {

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    public DefaultConditional(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultConditional(String systemName) {
        super(systemName);
    }

    // boolean expression of state variables
    private String _antecedent = "";
    private int _logicType = Conditional.ALL_AND;
    // variables (antecedent) parameters
    private ArrayList <ConditionalVariable> _variableList = new ArrayList<ConditionalVariable>();
    // actions (consequent) parameters
    protected ArrayList <ConditionalAction> _actionList = new ArrayList<ConditionalAction>();

	private int _currentState = Conditional.UNKNOWN;

	/**
	 * Get antecedent (boolean expression) of Conditional
	 */
	public String getAntecedentExpression() {
		return _antecedent;
	}

	/**
	 * Get type of operators in the antecedent statement
	 */
    public int getLogicType() {
        return _logicType;
    }

    /**
    * set the logic type (all AND's all OR's or mixed AND's and OR's
    * set the antecedent expression - should be a well formed boolean
    * statement with parenthesis indicating the order of evaluation
    */
    public void setLogicType(int type, String antecedent) {
        _logicType = type;
        _antecedent = antecedent;
        setState(Conditional.UNKNOWN);
    }
	
	/**
     * Set State Variables for this Conditional. Each state variable will 
	 * evaluate either True or False when this Conditional is calculated.
	 *<P>
	 * This method assumes that all
	 * information has been validated.
     */
    public void setStateVariables(ArrayList <ConditionalVariable> arrayList) {
        log.debug("Conditional \""+getUserName()+"\" ("+getSystemName()+
                  ") updated ConditionalVariable list.");
        _variableList = arrayList;
	}
	
	/**
	 * Make deep clone of variables
	 */
    public ArrayList <ConditionalVariable> getCopyOfStateVariables() {
        ArrayList <ConditionalVariable> variableList = new ArrayList <ConditionalVariable> ();
		for (int i = 0; i<_variableList.size(); i++) {
            ConditionalVariable variable = (ConditionalVariable)_variableList.get(i);
            ConditionalVariable clone = new ConditionalVariable();
            clone.setNegation(variable.isNegated());
            clone.setOpern(variable.getOpern());
            clone.setType(variable.getType());
            clone.setName(variable.getName());
            clone.setDataString(variable.getDataString());
            clone.setNum1(variable.getNum1());
            clone.setNum2(variable.getNum2());
            clone.setTriggerActions(variable.doTriggerActions());
            clone.setState(variable.getState());
            variableList.add(clone);
		}
        return variableList;
	}

	/**
	 * Set list of actions
	 */
	public void setAction (ArrayList <ConditionalAction> arrayList) {
		_actionList = arrayList;
	}		
	
	/**
	 * Make deep clone of actions
	 */
	public ArrayList <ConditionalAction> getCopyOfActions () {
        ArrayList <ConditionalAction> actionList = new ArrayList <ConditionalAction> ();
		for (int i = 0; i<_actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            ConditionalAction clone = new ConditionalAction();
            clone.setType(action.getType());
            clone.setOption(action.getOption());
            clone.setDeviceName(action.getDeviceName());
            clone.setActionData(action.getActionData());
            clone.setActionString(action.getActionString());
            actionList.add(clone);
		}
        return actionList;
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
	public int calculate (boolean enabled, PropertyChangeEvent evt) {
		// check if  there are no state variables
		if (_variableList.size()==0) {
			// if there are no state variables, no state can be calculated
            setState(Conditional.UNKNOWN);
			return _currentState;
		}
        boolean result = true;
        switch (_logicType) {
            case Conditional.ALL_AND:
                for (int i=0; (i<_variableList.size())&&result; i++) {
                    result = _variableList.get(i).evaluate();
                }
                break;
            case Conditional.ALL_OR:
                result = false;
                for (int k=0; (k<_variableList.size())&&!result; k++) {
                    result = _variableList.get(k).evaluate();
                }
                break;
            case Conditional.MIXED:
                char[] ch = _antecedent.toCharArray();
                int n = 0;
                for (int j=0; j<ch.length; j++) {
                    if (ch[j] != ' ') {
                        if (ch[j] == '{' || ch[j] == '[')  {
                            ch[j] = '(';
                        } else if (ch[j] == '}' || ch[j] == ']')  {
                            ch[j] = ')';
                        }
                        ch[n++] = ch[j];
                    }
                }
                try {
                    DataPair dp = parseCalculate(new String(ch, 0, n).toUpperCase(), _variableList);
                    result = dp.result;
                } catch ( NumberFormatException nfe) {
                    result = false;
                    log.error("parseCalculation error " + nfe);
                } catch ( IndexOutOfBoundsException ioob) {
                    result = false;
                    log.error("parseCalculation error " + ioob);
                }  catch ( JmriException je) {
                    result = false;
                    log.error("parseCalculation error " + je);
                }
                break;
        }
		int newState = FALSE;
        log.debug("Conditional \""+getUserName()+"\" ("+getSystemName()+") has calculated its state to be "+
                  result+". current state is "+_currentState+".  enabled= "+enabled);
		if (result) newState = TRUE;
        if (newState != _currentState) {
            setState(newState);
            if (enabled) {
                if (evt != null) {
                    // check if the current listener wants to (NOT) trigger actions
                    String listener = "";
                    try {
                        listener = ((NamedBean)evt.getSource()).getSystemName();
                    } catch ( ClassCastException e) {
                        log.error("PropertyChangeEvent source of unexpected type: "+ evt);
                    }
                    enabled = wantsToTrigger(listener);
                }
                if (enabled) {
                    takeActionIfNeeded();
                }
            }
		}
		return _currentState;
	}

    /**
    * Find out if the state variable is willing to cause the actions to execute
    */
    boolean wantsToTrigger(String varName) {
        for (int i=0; i<_variableList.size(); i++) {
            if (varName.equals(_variableList.get(i).getName())) {
                return _variableList.get(i).doTriggerActions();
            }
        }
        return true;
    }

    class DataPair {
        boolean result = false;
        int indexCount = 0;         // index reached when parsing completed
        BitSet argsUsed = null;     // error detection for missing arguments
    }

    /**
    *  Check that an antecedent is well formed
    *  
    */
    public String validateAntecedent(String ant, ArrayList <ConditionalVariable> variableList) {
        char[] ch = ant.toCharArray();
        int n = 0;
        for (int j=0; j<ch.length; j++) {
            if (ch[j] != ' ') {
                if (ch[j] == '{' || ch[j] == '[')  {
                    ch[j] = '(';
                } else if (ch[j] == '}' || ch[j] == ']')  {
                    ch[j] = ')';
                }
                ch[n++] = ch[j];
            }
        }
        int count = 0;
        for (int j=0; j<n; j++)  {
            if (ch[j] == '(') {
                count++;
            }
            if (ch[j] == ')') {
                count--;
            }
        }
        if (count > 0) {
            return java.text.MessageFormat.format(
                            rbx.getString("ParseError7"), new Object[] { ')' });
        }
        if (count < 0) {
            return java.text.MessageFormat.format(
                            rbx.getString("ParseError7"), new Object[] { '(' });
        }
        try {
            DataPair dp = parseCalculate(new String(ch, 0, n).toUpperCase(), variableList);
            if (n != dp.indexCount) {
                return java.text.MessageFormat.format(
                            rbx.getString("ParseError4"), new Object[] { ch[dp.indexCount-1] });                
            }
            int index = dp.argsUsed.nextClearBit(0);
            if ( index >= 0 && index < variableList.size() ) {
                return java.text.MessageFormat.format(
                            rbx.getString("ParseError5"), 
                            new Object[] { new Integer(variableList.size()), 
                                            new Integer(index+1) }); 
            }
        } catch ( NumberFormatException nfe) {
            return rbx.getString("ParseError6") + nfe.getMessage();
        } catch ( IndexOutOfBoundsException ioob) {
            return rbx.getString("ParseError6") + ioob.getMessage();
        }  catch ( JmriException je) {
            return rbx.getString("ParseError6") + je.getMessage();
        }
        return null;
    }

    /**
    * parses and computes one parenthsis level of a boolean statement.
    * returns a data pair consisting of the truth value of the level
    * a count of the indices consumed to parse the level and a
    * bitmap of the variable indices used.
    * Recursively calls inner parentheses levels.
    * Note that all logic operators are dectected by the parsing, therefore the
    * internal negation of a variable is washed.
    */
    DataPair parseCalculate(String s, ArrayList <ConditionalVariable> variableList) 
            throws NumberFormatException, IndexOutOfBoundsException, JmriException
    {
        BitSet argsUsed = new BitSet(_variableList.size());
        DataPair dp = null;
        boolean leftArg = false;
        boolean rightArg = false;
        int oper = OPERATOR_NONE;
        int k = -1;
        int i = 0;      // index of String s
        int numArgs = 0;
        if (s.charAt(i) == '(')  {
            dp = parseCalculate(s.substring(++i), variableList);
            leftArg = dp.result;
            i += dp.indexCount;
            argsUsed.or( dp.argsUsed);
        } else {
            // cannot be '('.  must be either leftArg or notleftArg
            if (s.charAt(i) == 'R') {
                try {
                    k = Integer.parseInt(String.valueOf(s.substring(i+1, i+3)));
                    i += 2;
                } catch (NumberFormatException nfe) {
                    k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                } catch (IndexOutOfBoundsException ioob) {
                    k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                }
                leftArg = variableList.get(k-1).evaluate();
                if (variableList.get(k-1).isNegated())
                {
                    leftArg = !leftArg;
                }
                i++;
                argsUsed.set(k-1);
            } else if (rbx.getString("LogicNOT").equals(s.substring(i, i+3)) ) {
                i += 3;
                //not leftArg
                if (s.charAt(i) == '(')  {
                    dp = parseCalculate(s.substring(++i), variableList);
                    leftArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or( dp.argsUsed);
                } else if (s.charAt(i) == 'R') {
                    try {
                        k = Integer.parseInt(String.valueOf(s.substring(i+1, i+3)));
                        i += 2;
                    } catch (NumberFormatException nfe) {
                        k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                    } catch (IndexOutOfBoundsException ioob) {
                        k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                    }
                    leftArg = variableList.get(k-1).evaluate();
                    if (variableList.get(k-1).isNegated())
                    {
                        leftArg = !leftArg;
                    }
                    i++;
                    argsUsed.set(k-1);
                } else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError1"), new Object[] { s.substring(i) }));
                }
                leftArg = !leftArg;
            } else {
                throw new JmriException(java.text.MessageFormat.format(
                    rbx.getString("ParseError9"), new Object[] { s.substring(i) }));
            }
        }
        // crank away to the right until a matching paren is reached
        while ( i<s.length() ) {
            if ( s.charAt(i) != ')' ) {
                // must be either AND or OR
                if (rbx.getString("LogicAND").equals(s.substring(i, i+3)))  {
                    i += 3;
                    oper = OPERATOR_AND;
                } else if (rbx.getString("LogicOR").equals(s.substring(i, i+2))) {
                    i += 2;
                    oper = OPERATOR_OR;
                }else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError2"), new Object[] { s.substring(i) }));
                } 
                if (s.charAt(i) == '(')  {
                    dp = parseCalculate(s.substring(++i), variableList);
                    rightArg =dp.result;
                    i += dp.indexCount;
                    argsUsed.or( dp.argsUsed);
                } else {
                    // cannot be '('.  must be either rightArg or notRightArg
                    if (s.charAt(i) == 'R') {
                        try {
                            k = Integer.parseInt(String.valueOf(s.substring(i+1, i+3)));
                            i += 2;
                        } catch (NumberFormatException nfe) {
                            k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                        } catch (IndexOutOfBoundsException ioob) {
                            k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                        }
                        rightArg = variableList.get(k-1).evaluate();
                        if (variableList.get(k-1).isNegated())
                        {
                            rightArg = !rightArg;
                        }
                        i++;
                        argsUsed.set(k-1);
                    } else if ((i+3)<s.length() && rbx.getString("LogicNOT").equals(s.substring(i, i+3)) )
                    {
                        i += 3;
                        //not rightArg
                        if (s.charAt(i) == '(')  {
                            dp = parseCalculate(s.substring(++i), variableList);
                            rightArg = dp.result;
                            i += dp.indexCount;
                            argsUsed.or( dp.argsUsed);
                        } else if (s.charAt(i) == 'R') {
                            try {
                                k = Integer.parseInt(String.valueOf(s.substring(i+1, i+3)));
                                i += 2;
                            } catch (NumberFormatException nfe) {
                                k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                            } catch (IndexOutOfBoundsException ioob) {
                                k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                            }
                            rightArg = variableList.get(k-1).evaluate();
                            if (variableList.get(k-1).isNegated())
                            {
                                rightArg = !rightArg;
                            }
                            i++;
                            argsUsed.set(k-1);
                        } else {
                            throw new JmriException(java.text.MessageFormat.format(
                                rbx.getString("ParseError3"), new Object[] { s.substring(i) }));
                        }
                        rightArg = !rightArg;
                    } else {
                        throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError9"), new Object[] { s.substring(i) }));
                    }
                }
                if (oper == OPERATOR_AND)   {
                    leftArg = (leftArg && rightArg);
                } else if (oper == OPERATOR_OR) {
                    leftArg = (leftArg || rightArg);
                }
            }
            else {  // This level done, pop recursion
                i++;
                break;
            }
        }
        dp = new DataPair();
        dp.result = leftArg;
        dp.indexCount = i;
        dp.argsUsed = argsUsed;
        return dp;
    }
   
	/**
	 * Compares action options, and takes action if appropriate
	 * <P>
	 * Only get here if a change in state has occurred when calculating this Conditional
	 */
	@SuppressWarnings("deprecation")
	private void takeActionIfNeeded() {
        int actionCount = 0;
        int actionNeeded = 0;
        int act = 0;
        int state = 0;
        // Use a local copy of state to guarantee the entire list of actions will be fired off
        // before a state change occurs that may block their completion.
        int currentState = _currentState;
		for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            //log.debug("Actual! currentState= "+_currentState+" action = "+action.getOptionString()+" "+action.getTypeString()+" "+action.getActionString());
            int option = action.getOption();
			if ( ((currentState==TRUE) && (option==ACTION_OPTION_ON_CHANGE_TO_TRUE)) ||
				((currentState==FALSE) && (option==ACTION_OPTION_ON_CHANGE_TO_FALSE)) ||
					(option==ACTION_OPTION_ON_CHANGE) ) {
				// need to take this action
                actionNeeded++;
				SignalHead h = null;
				Logix x = null;	
				Light lgt = null;
                int value = 0;
                Timer timer = null;
				switch (action.getType()) {
					case Conditional.ACTION_NONE:
						break;
					case Conditional.ACTION_SET_TURNOUT:
						Turnout t = InstanceManager.turnoutManagerInstance().
									getTurnout(action.getDeviceName());
						if (t == null) {
							log.error("invalid turnout name in action - "+action.getDeviceName());
						}
						else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = t.getKnownState();
                                if (state == Turnout.CLOSED)
                                    act = Turnout.THROWN;
                                else
                                    act = Turnout.CLOSED;
                            }
							t.setCommandedState(act);
                            actionCount++;
						}
						break;
                    case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                        action.stopTimer();
                        // fall through
					case Conditional.ACTION_DELAYED_TURNOUT:
						if (!action.isTimerActive()) {
							// Create a timer if one does not exist
                            timer = action.getTimer();
							if (timer==null) {
								action.setListener(new TimeTurnout(i));
								timer = new Timer(2000, action.getListener());
								timer.setRepeats(false);
							}
							// Start the Timer to set the turnout
                            value = getIntegerValue(action.getActionString());
							timer.setInitialDelay(value*1000);
                            action.setTimer(timer);
                            action.startTimer();
                            actionCount++;
						}
						else {
							log.warn("timer already active on request to start delayed turnout action - "+
																				action.getDeviceName());
						}
						break;
					case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
						ConditionalManager cmg = jmri.InstanceManager.conditionalManagerInstance();
						java.util.Iterator iter = cmg.getSystemNameList().iterator();
						while (iter.hasNext()) {
							String sname = (String)iter.next();
							if (sname==null) 
								log.error("Conditional system name null during cancel turnput timers for "
														+ action.getDeviceName());
							Conditional c = cmg.getBySystemName(sname);
							if (c==null)
								log.error("Conditional null during cancel turnout timers for "
														+ action.getDeviceName());
							else {
								c.cancelTurnoutTimer(action.getDeviceName());
                                actionCount++;
							}
						}						
						break;
					case Conditional.ACTION_LOCK_TURNOUT:
						Turnout tl = InstanceManager.turnoutManagerInstance().
									getTurnout(action.getDeviceName());
						if (tl == null) {
							log.error("invalid turnout name in action - "+action.getDeviceName());
						}
						else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                if (tl.getLocked(Turnout.CABLOCKOUT) )
                                    act = Turnout.UNLOCKED;
                                else
                                    act = Turnout.LOCKED;
                            }
							if (act == Turnout.LOCKED){
								tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
							}
							else if (act == Turnout.UNLOCKED){
								tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
							}
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(action.getDeviceName());
						if (h == null) {
							log.error("invalid signal head name in action - "+action.getDeviceName());
						}
						else {
							h.setAppearance(action.getActionData());
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(action.getDeviceName());
						if (h == null) {
							log.error("invalid signal head name in action - "+action.getDeviceName());
						}
						else {
							h.setHeld(true);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_CLEAR_SIGNAL_HELD:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(action.getDeviceName());
						if (h == null) {
							log.error("invalid signal head name in action - "+action.getDeviceName());
						}
						else {
							h.setHeld(false);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_DARK:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(action.getDeviceName());
						if (h == null) {
							log.error("invalid signal head name in action - "+action.getDeviceName());
						}
						else {
							h.setLit(false);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_SIGNAL_LIT:
						h = InstanceManager.signalHeadManagerInstance().
									getSignalHead(action.getDeviceName());
						if (h == null) {
							log.error("invalid signal head name in action - "+action.getDeviceName());
						}
						else {
							h.setLit(true);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_TRIGGER_ROUTE:
						Route r = InstanceManager.routeManagerInstance().
									getRoute(action.getDeviceName());
						if (r == null) {
							log.error("invalid route name in action - "+action.getDeviceName());
						}
						else {
							r.setRoute();
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_SENSOR:
						Sensor sn = InstanceManager.sensorManagerInstance().
									getSensor(action.getDeviceName());
						if (sn == null) {
							log.error("invalid sensor name in action - "+action.getDeviceName());
						}
						else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = sn.getState();
                                if (state == Sensor.ACTIVE)
                                    act = Sensor.INACTIVE;
                                else
                                    act = Sensor.ACTIVE;
                            }
							try {
								sn.setKnownState(act);
                                actionCount++;
							} 
							catch (JmriException e) {
								log.warn("Exception setting sensor "+action.getDeviceName()+" in action");
							}
						}
						break;
					case Conditional.ACTION_RESET_DELAYED_SENSOR:
                        action.stopTimer();
                        // fall through
					case Conditional.ACTION_DELAYED_SENSOR:
						if (!action.isTimerActive()) {
							// Create a timer if one does not exist
                            timer = action.getTimer();
							if (timer==null) {
								action.setListener(new TimeSensor(i));
								timer = new Timer(2000, action.getListener());
								timer.setRepeats(false);
							}
							// Start the Timer to set the turnout
                            value = getIntegerValue(action.getActionString());
							timer.setInitialDelay(value*1000);
                            action.setTimer(timer);
                            action.startTimer();
                            actionCount++;
						}
						else {
							log.warn("timer already active on request to start delayed sensor action - "+
																				action.getDeviceName());
						}
						break;
					case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
						ConditionalManager cm = jmri.InstanceManager.conditionalManagerInstance();
						java.util.Iterator itr = cm.getSystemNameList().iterator();
						while (itr.hasNext()) {
							String sname = (String)itr.next();
							if (sname==null) 
								log.error("Conditional system name null during cancel sensor timers for "
														+ action.getDeviceName());
							Conditional c = cm.getBySystemName(sname);
							if (c==null)
								log.error("Conditional null during cancel sensor timers for "
														+ action.getDeviceName());
							else {
								c.cancelSensorTimer(action.getDeviceName());
                                actionCount++;
							}
						}						
						break;
					case Conditional.ACTION_SET_LIGHT:
						lgt = InstanceManager.lightManagerInstance().
										getLight(action.getDeviceName());
						if (lgt == null) {
							log.error("invalid light name in action - "+action.getDeviceName());
						}
						else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = lgt.getState();
                                if (state == Light.ON)
                                    act = Light.OFF;
                                else
                                    act =Light.ON;
                            }
							lgt.setState(act);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_LIGHT_INTENSITY:
						lgt = InstanceManager.lightManagerInstance().
										getLight(action.getDeviceName());
						if (lgt == null) {
							log.error("invalid light name in action - "+action.getDeviceName());
						}
						else {
							try {
                                value = getIntegerValue(action.getActionString());
								lgt.setTargetIntensity(((double)value)/100.0);
                                actionCount++;
							}
							catch (IllegalArgumentException e) {
								log.error("Exception in set light intensity action - "+e);
							}
						}
						break;
					case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
						lgt = InstanceManager.lightManagerInstance().
										getLight(action.getDeviceName());
						if (lgt == null) {
							log.error("invalid light name in action - "+action.getDeviceName());
						}
						else {
							try {
                                value = getIntegerValue(action.getActionString());
								lgt.setTransitionTime((double)value);
                                actionCount++;
							}
							catch (IllegalArgumentException e) {
								log.error("Exception in set light transition time action - "+e);
							}
						}
						break;
					case Conditional.ACTION_SET_MEMORY:
						Memory m = InstanceManager.memoryManagerInstance().
										provideMemory(action.getDeviceName());
						if (m == null) {
							log.error("invalid memory name in action - "+action.getDeviceName());
						}
						else {
							m.setValue(action.getActionString());
                            actionCount++;
						}
						break;
					case Conditional.ACTION_COPY_MEMORY:
						Memory mFrom = InstanceManager.memoryManagerInstance().
										provideMemory(action.getDeviceName());
						if (mFrom == null) {
							log.error("invalid memory name in action - "+action.getDeviceName());
						}
						else {
							Memory mTo = InstanceManager.memoryManagerInstance().
										provideMemory(action.getActionString());
							if (mTo == null) {
								log.error("invalid memory name in action - "+action.getActionString());
							}
							else {
								mTo.setValue(mFrom.getValue());
                                actionCount++;
							}
						}
						break;
					case Conditional.ACTION_ENABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getLogix(action.getDeviceName());
						if (x == null) {
							log.error("invalid logix name in action - "+action.getDeviceName());
						}
						else {
							x.setEnabled(true);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_DISABLE_LOGIX:
						x = InstanceManager.logixManagerInstance().getLogix(action.getDeviceName());
						if (x == null) {
							log.error("invalid logix name in action - "+action.getDeviceName());
						}
						else {
							x.setEnabled(false);
                            actionCount++;
						}
						break;
					case Conditional.ACTION_PLAY_SOUND:
						if (!(action.getActionString().equals(""))) {
                            Sound sound = action.getSound();
							if (sound == null) {
								sound = new jmri.jmrit.Sound(action.getActionString());
							}
							sound.play();
                            actionCount++;
						}
						break;
					case Conditional.ACTION_RUN_SCRIPT:
						if (!(action.getActionString().equals(""))) {
							jmri.util.PythonInterp.runScript(action.getActionString());
                            actionCount++;
						}
						break;
					case Conditional.ACTION_SET_FAST_CLOCK_TIME:
						Date date = InstanceManager.timebaseInstance().getTime();
						date.setHours(action.getActionData()/60);
						date.setMinutes(action.getActionData() - ((action.getActionData()/60)*60));
						date.setSeconds(0);
						InstanceManager.timebaseInstance().userSetTime(date);
                        actionCount++;
						break;
					case Conditional.ACTION_START_FAST_CLOCK:
						InstanceManager.timebaseInstance().setRun(true);
                        actionCount++;
						break;
					case Conditional.ACTION_STOP_FAST_CLOCK:
						InstanceManager.timebaseInstance().setRun(false);
                        actionCount++;
						break;
				}
			}
		}
        log.debug("Conditional \""+getUserName()+"\" ("+getSystemName()+") has taken "+actionCount
                  +" actions of "+actionNeeded+" actions needed on change to "+currentState);
	}

	/**
	 * Return int from either literal String or Internal memory reference.
	 */
    int getIntegerValue(String sNumber) {
        int time = 0;
        try {
            time = Integer.valueOf(sNumber).intValue();
        } catch (NumberFormatException e) {
            Memory mem = InstanceManager.memoryManagerInstance().provideMemory(sNumber);
            if (mem == null) {
                log.error("invalid memory name for action time variable - "+sNumber);
            }
            else {
                try {
                    time = Integer.valueOf((String)mem.getValue()).intValue();
                } catch (NumberFormatException ex) {
                    log.error("invalid action time variable from memory, "
                              +getUserName()+"("+mem.getSystemName()
                              +"), value = "+(String)mem.getValue());
                }
            }
        }
        return time;
    }
	
	/**
	 * Stop a sensor timer if one is actively delaying setting of the specified sensor
	 */
	public void cancelSensorTimer (String sname) {
		for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
			if ( (action.getType() == Conditional.ACTION_DELAYED_SENSOR) || 
						(action.getType() == Conditional.ACTION_RESET_DELAYED_SENSOR) ) {
				if ( action.isTimerActive() ) {
					// have active set sensor timer - is it for our sensor?
					if ( action.getDeviceName().equals(sname) ) {
						// yes, names match, cancel timer
						action.stopTimer();
					}
					else {
						// check if same sensor by a different name
						Sensor sn = InstanceManager.sensorManagerInstance().
												getSensor(action.getDeviceName());
						if (sn == null) {
							log.error("Unknown sensor *"+action.getDeviceName()+" in cancelSensorTimer.");
						}
						else if (sname.equals(sn.getSystemName()) || 
												sname.equals(sn.getUserName())) {
							// same sensor, cancel timer
                            action.stopTimer();
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
		for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
			if ( (action.getType() == Conditional.ACTION_DELAYED_TURNOUT) || 
						(action.getType() == Conditional.ACTION_RESET_DELAYED_TURNOUT) ) {
				if ( action.isTimerActive() ) {
					// have active set turnout timer - is it for our turnout?
					if ( action.getDeviceName().equals(sname) ) {
						// yes, names match, cancel timer
                        action.stopTimer();
					}
					else {
						// check if same turnout by a different name
						Turnout tn = InstanceManager.turnoutManagerInstance().
												getTurnout(action.getDeviceName());
						if (tn == null) {
							log.error("Unknown turnout *"+action.getDeviceName()+" in cancelTurnoutTimer.");
						}
						else if (sname.equals(tn.getSystemName()) || 
												sname.equals(tn.getUserName())) {
							// same turnout, cancel timer
                            action.stopTimer();
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
        return _currentState;
    }
    
    /**
     * State of Conditional is set. Not really public for Conditionals.
	 * The state of a Conditional is only changed by its calculate method, so the state is
	 *    really a read-only bound property.
     */
    public void setState(int state) {
        if (_currentState != state) {
            int oldState = _currentState;
            _currentState = state;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(_currentState));
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
            ConditionalAction action = _actionList.get(mIndex);
			Sensor sn = InstanceManager.sensorManagerInstance().
										getSensor(action.getDeviceName());
			if (sn==null) {
				log.error("Invalid delayed sensor name - "+action.getDeviceName());
			}
			else {
				// set the sensor
				try {
					sn.setKnownState(action.getActionData());
				} 
				catch (JmriException e) {
					log.warn("Exception setting delayed sensor "+action.getDeviceName()+" in action");
				}
			}
			// Turn Timer OFF
			action.stopTimer();
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
            ConditionalAction action = _actionList.get(mIndex);
			Turnout t = InstanceManager.turnoutManagerInstance().
										getTurnout(action.getDeviceName());
			if (t==null) {
				log.error("Invalid delayed turnout name - "+action.getDeviceName());
			}
			else {
				// set the turnout
				t.setCommandedState(action.getActionData());
			}
			// Turn Timer OFF
			action.stopTimer();
		}
	}

	
static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultConditional.class.getName());
}

/* @(#)DefaultConditional.java */

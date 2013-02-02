package jmri;

import org.apache.log4j.Logger;
import jmri.jmrit.beantable.LogixTableAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * ConditionalVariable.java
 *
 * The variable used in the antecedent (the 'if' part) of the Conditional. 
 * proposition.  The states of ConditionalVariables and logic expression 
 * of the antecedent determine the state of the Conditional.
 * <P>
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
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */


public class ConditionalVariable {

	static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
	static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public static final int NUM_COMPARE_OPERATIONS  = 5;
    public static final int LESS_THAN          = 1;
    public static final int LESS_THAN_OR_EQUAL = 2;
    public static final int EQUAL              = 3;
    public static final int GREATER_THAN_OR_EQUAL = 4;
    public static final int GREATER_THAN       = 5; 

    private boolean _not = false;
    // Not a variable attribute, but retained as an artifact of previous releases.  This will be used
    // as the default operator immediately to the left of this variable in the antecedent statement. 
    // It may be over written by the antecedent statement in the Conditional to which this variable 
    // belongs.
    private int _opern = Conditional.OPERATOR_NONE;
    private int _type   = Conditional.TYPE_NONE;
    private String _name = "";
    private String _dataString = "";
    private int _num1 = 0;
    private int _num2 = 0;
    private NamedBeanHandle<?> _namedBean = null;
    //private NamedBeanHandle<Sensor> _namedSensorBean = null;
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    // Name clarification: Formerly was named '_triggersCalculation' because it controlled whether
    // a listener was installed for this device and thus trigger calcuation of the Conditional.
    // Now named '_triggersActions' because listeners are always installed for activated Logix 
    // Conditionals and this parameter nows controls whether, if its change of state changes the
    // state of the conditional, should that also  trigger the actions.
    private boolean _triggersActions = true;
    private int _state = Conditional.UNKNOWN;        // tri-state

    public ConditionalVariable() {
    }

    public ConditionalVariable(boolean not, int opern, int type, String name, boolean trigger) {
        _not = not;
        _opern = opern;
        _type = type;
        _name = name;
        _triggersActions = trigger;
        try {
            int itemType = Conditional.TEST_TO_ITEM[_type];
            switch (itemType) {
                case Conditional.ITEM_TYPE_SENSOR:
                    Sensor sn = InstanceManager.sensorManagerInstance().provideSensor(_name);
                    if (sn == null) {
                        log.error("invalid sensor name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, sn);
                    break;
                case Conditional.ITEM_TYPE_TURNOUT:
                    Turnout tn = InstanceManager.turnoutManagerInstance().provideTurnout(_name);
                    if (tn == null) {
                        log.error("invalid turnout name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, tn);
                    break;
                case Conditional.ITEM_TYPE_MEMORY:
                    Memory my = InstanceManager.memoryManagerInstance().provideMemory(_name);
                    if (my == null) {
                        log.error("invalid memory name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, my);
                    break;
                case Conditional.ITEM_TYPE_LIGHT:
                    Light l = InstanceManager.lightManagerInstance().getLight(_name);
                    if (l == null) {
                        log.error("invalid light name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, l);
                    break;
                case Conditional.ITEM_TYPE_SIGNALHEAD:
                    SignalHead s = InstanceManager.signalHeadManagerInstance().getSignalHead(_name);
                    if (s == null) {
                        log.error("invalid signalhead name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, s);
                    break;
                case Conditional.ITEM_TYPE_SIGNALMAST:
                    SignalMast sm = InstanceManager.signalMastManagerInstance().provideSignalMast(_name);
                    if (sm == null) {
                        log.error("invalid signalmast name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, sm);
                    break;
                case Conditional.ITEM_TYPE_ENTRYEXIT:
                    NamedBean nb = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class).getBySystemName(_name);
                    if(nb == null){
                        log.error("invalid entry exit name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, nb);
                    break;
                case Conditional.ITEM_TYPE_CONDITIONAL:
                	Conditional c = InstanceManager.conditionalManagerInstance().getConditional(_name);
                    if(c == null){
                        log.error("invalid conditiona; name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, c);
                    break;
                case Conditional.ITEM_TYPE_WARRANT:
                    Warrant w = InstanceManager.warrantManagerInstance().getWarrant(_name);
                    if(w == null){
                        log.error("invalid warrant name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, w);
                    break;
                case Conditional.ITEM_TYPE_OBLOCK:
                    OBlock b = InstanceManager.oBlockManagerInstance().getOBlock(_name);
                    if(b == null){
                        log.error("invalid block name= \""+_name+"\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, b);
                    break;

                default : break;
            }
        } catch (java.lang.NumberFormatException ex) {
            //Can be Considered Normal where the logix is loaded prior to any other beans
        }
    }

    public boolean isNegated() {
        return _not;
    }

    public void setNegation(boolean not) {
        _not = not;
    }

    public int getOpern() {
        return _opern;
    }

    public void setOpern(int opern) {
        if (opern == Conditional.OPERATOR_AND_NOT) {
            _opern = Conditional.OPERATOR_AND;
            _not = true;
        } else if (opern == Conditional.OPERATOR_NOT) {
            _opern = Conditional.OPERATOR_NONE;
            _not = true;
        } else {
            _opern = opern;
        }
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    public String getName() {
        if (_namedBean!=null){
            return _namedBean.getName();
        }
        /* As we have a trigger for something using the variable, then hopefully
        all the managers have been loaded and we can get the bean, which prevented
        the bean from being loaded in the first place */
        setName(_name);
        return _name;
    }

    public void setName(String name) {
        _name = name;
        NamedBean bean = null;
        int itemType = Conditional.TEST_TO_ITEM[_type];
        
        switch (itemType) {
        	case Conditional.TYPE_NONE:
        		break;
        	case Conditional.ITEM_TYPE_CLOCK:
        		break;	/* no beans for these, at least that I know of */
            case Conditional.ITEM_TYPE_SENSOR:
                bean = InstanceManager.sensorManagerInstance().provideSensor(_name);
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                bean = InstanceManager.turnoutManagerInstance().provideTurnout(_name);
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                bean = InstanceManager.lightManagerInstance().getLight(_name);
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                bean = InstanceManager.memoryManagerInstance().provideMemory(_name);
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                bean = InstanceManager.signalMastManagerInstance().provideSignalMast(_name);
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                bean = InstanceManager.signalHeadManagerInstance().getSignalHead(_name);
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                bean = InstanceManager.conditionalManagerInstance().getConditional(_name);
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                bean = InstanceManager.warrantManagerInstance().getWarrant(_name);
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                bean = InstanceManager.oBlockManagerInstance().getOBlock(_name);
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                bean = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class).getBySystemName(_name);
                break;
            default : log.error("Type "+itemType+" not set for " + _name);
        }

        //Once all refactored, we should probably register an error if the bean is returned null.
        if (bean!=null){
            _namedBean = nbhm.getNamedBeanHandle(_name, bean);
        } else {
            _namedBean = null;
        }
    }
    
    public NamedBeanHandle<?> getNamedBean(){
        return _namedBean;
    }
    
    public NamedBean getBean(){
        if (_namedBean!=null){
            return (NamedBean) _namedBean.getBean();
        } 
        setName(_name); //ReApply name as that will create namedBean, save replicating it here
        if(_namedBean!=null)
            return (NamedBean) _namedBean.getBean();
        return null;
    }

    public String getDataString() {
        return _dataString;
    }

    public void setDataString(String data) {
        _dataString = data;
    }

    public int getNum1() {
        return _num1;
    }

    public void setNum1(int num) {
        _num1 = num;
    }

    public int getNum2() {
        return _num2;
    }

    public void setNum2(int num) {
        _num2 = num;
    }

    /**
    * If change of state of this object causes a change of state of the Conditional,
    * should any actions be executed.
    */
    public boolean doTriggerActions() {
        return _triggersActions;
    }

    public void setTriggerActions(boolean trigger) {
        _triggersActions = trigger;
    }

    public int getState() {
        return _state;
    }

    public void setState(int state) {
        _state = state;
    }

    public void setState(boolean state) {
        if (state) {
            _state = Conditional.TRUE;
        }
        else {
            _state = Conditional.FALSE;
        }
    }

    public String getTestTypeString() {
        return getTestTypeString(_type);
    }

    public String getOpernString() {
        switch (_opern) {
            case Conditional.OPERATOR_AND:
                return rbx.getString("LogicAND");
            case Conditional.OPERATOR_NOT:
                return rbx.getString("LogicNOT");
            case Conditional.OPERATOR_AND_NOT:	
                return rbx.getString("LogicAND");
            case Conditional.OPERATOR_NONE:
                return "";
            case Conditional.OPERATOR_OR:
                return rbx.getString("LogicOR");
            default : return "";
        }
    }

	/**
	*  Evaluates this State Variable
	*  <P>
	*  Returns true if variable evaluates true, otherwise false. 
	*/
	@SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
	public boolean evaluate() {
		boolean result = true;
		// evaluate according to state variable type
        int itemType = Conditional.TEST_TO_ITEM[_type];
        if (log.isDebugEnabled()) log.debug("evaluate: \""+getName()+"\" type= "+_type+" itemType= "+itemType);
		switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
				//Sensor sn = InstanceManager.sensorManagerInstance().provideSensor(getName());
                Sensor sn = (Sensor) getBean();
				if (sn == null) {
					log.error("invalid sensor name= \""+getName()+"\" in state variable");
					return (false);
				}
                if (_type == Conditional.TYPE_SENSOR_ACTIVE) {
                    if (sn.getState() == Sensor.ACTIVE) result = true;
                    else result = false;
                } else {
                    if (sn.getState() == Sensor.INACTIVE) result = true;
                    else result = false;
                }
				break;
            case Conditional.ITEM_TYPE_TURNOUT:
				Turnout t = (Turnout) getBean();
				if (t == null) {
					log.error("invalid turnout name= \""+getName()+"\" in state variable");
					return (false);
				}
                if (_type == Conditional.TYPE_TURNOUT_THROWN) {
                    if (t.getKnownState() == Turnout.THROWN) result = true;
                    else result = false;
                } else {
                    if (t.getKnownState() == Turnout.CLOSED) result = true;
                    else result = false;
                }
				break;
            case Conditional.ITEM_TYPE_LIGHT:
				Light lgt = (Light) getBean();
				if (lgt == null) {
					log.error("invalid light name= \""+getName()+"\" in state variable");
					return (false);
				}
                if (_type == Conditional.TYPE_LIGHT_ON) {
                    if (lgt.getState() == Light.ON) result = true;
                    else result = false;
                } else {
                    if (lgt.getState() == Light.OFF) result = true;
                    else result = false;
                }
				break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
				SignalMast f = (SignalMast) getBean();
				if (f == null) {
					log.error("invalid signal mast name= \""+getName()+"\" in state variable");
					return (false);
				}
                switch ((_type)) {
                    case Conditional.TYPE_SIGNAL_MAST_LIT:
                        result = f.getLit(); 
                        break;
                    case Conditional.TYPE_SIGNAL_MAST_HELD:
                        result = f.getHeld();
                        break;
                    case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                        if (f.getAspect() == null) result = false;
                        else if (f.getAspect().equals(_dataString)) result = true;
                        else result = false; 
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
				SignalHead h = (SignalHead) getBean();
				if (h == null) {
					log.error("invalid signal head name= \""+getName()+"\" in state variable");
					return (false);
				}
                switch (_type)
                {
                    case Conditional.TYPE_SIGNAL_HEAD_RED:
                        if (h.getAppearance() == SignalHead.RED) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                        if (h.getAppearance() == SignalHead.YELLOW) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                        if (h.getAppearance() == SignalHead.GREEN) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_DARK:
                        if (h.getAppearance() == SignalHead.DARK) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                        if (h.getAppearance() == SignalHead.FLASHRED) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                        if (h.getAppearance() == SignalHead.FLASHYELLOW) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                        if (h.getAppearance() == SignalHead.FLASHGREEN) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                        if (h.getAppearance() == SignalHead.LUNAR) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                        if (h.getAppearance() == SignalHead.FLASHLUNAR) result = true;
                        else result = false; 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_LIT:
                        result = h.getLit(); 
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_HELD:
                        result = h.getHeld();
                        break;
                    default:
                        result = false;
                }
                break;
            case Conditional.ITEM_TYPE_MEMORY:
				Memory m = (Memory) getBean();
				if (m == null) {
					log.error("invalid memory name= \""+getName()+"\" in state variable");
					return (false);
				}
                String value1 = null;
                String value2 = null;
                if (m.getValue()!=null) {
                    value1 = m.getValue().toString();
                }
                boolean caseInsensitive = ((_type == Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE) ||
                                            (_type == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE));
                if ((_type == Conditional.TYPE_MEMORY_COMPARE) || 
                        (_type == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE)) {
                    Memory m2 = InstanceManager.memoryManagerInstance().provideMemory(_dataString);
                    if (m2 == null) {
                        log.error("invalid data memory name= \""+_dataString+"\" in state variable");
                        return (false);
                    }
                    if (m2.getValue()!=null) {
                        value2 = m2.getValue().toString();
                    }
                } else {
                    value2 = _dataString;
                }
                result = compare(value1,  value2, caseInsensitive);
				break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
				Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(getName());
				if (c ==null) {
                    c = InstanceManager.conditionalManagerInstance().getByUserName(getName());
                    if (c == null) {
                        log.error("invalid conditional name= \""+getName()+"\" in state variable");
                        return (false);
                    }
				}
                if (_type == Conditional.TYPE_CONDITIONAL_TRUE) {
                    if (c.getState() == Conditional.TRUE) result = true;
                    else result = false;
                } else {
                    if (c.getState() == Conditional.FALSE) result = true;
                    else result = false;
                }
				break;
            case Conditional.ITEM_TYPE_WARRANT:
				Warrant w = InstanceManager.warrantManagerInstance().getWarrant(getName());
				if (w == null) {
					log.error("invalid Warrant name= \""+getName()+"\" in state variable");
					return (false);
				}
                switch (_type)
                {
                    case Conditional.TYPE_ROUTE_FREE:
                        result = w.routeIsFree();
                        break;
                    case Conditional.TYPE_ROUTE_OCCUPIED:
                        result = w.routeIsOccupied(); 
                        break;
                    case Conditional.TYPE_ROUTE_ALLOCATED:
                        result = w.isAllocated(); 
                        break;
                    case Conditional.TYPE_ROUTE_SET:
                        result = w.hasRouteSet(); 
                        break;
                    case Conditional.TYPE_TRAIN_RUNNING:
                        // not in either RUN or LEARN state
                        result = !(w.getRunMode() == Warrant.MODE_NONE); 
                        break;
                    default:
                        result = false;
                }
				break;
            case Conditional.ITEM_TYPE_CLOCK:
				Timebase fastClock = InstanceManager.timebaseInstance();
				Date currentTime = fastClock.getTime();
				int currentMinutes = (currentTime.getHours()*60) + currentTime.getMinutes();
                int beginTime = fixMidnight(_num1);
                int endTime = fixMidnight(_num2);
				// check if current time is within range specified
				if (beginTime<=endTime) {
					// range is entirely within one day
					if ( (beginTime<=currentMinutes)&&(currentMinutes<=endTime) ) result = true;
					else result = false;
				}
				else {
					// range includes midnight
					if (beginTime<=currentMinutes || currentMinutes<=endTime) result = true;
					else result = false;
				}
				break;
            case Conditional.ITEM_TYPE_OBLOCK:
                OBlock b = InstanceManager.oBlockManagerInstance().getOBlock(getName());
				if (b == null) {
					log.error("invalid OBlock name= \""+getName()+"\" in state variable");
					return (false);
				}
                result = b.statusIs(_dataString); 
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                NamedBean e = getBean();
                if (_type == Conditional.TYPE_ENTRYEXIT_ACTIVE) {
                    if (e.getState() == 0x02) result = true;
                    else result = false;
                } else {
                    if (e.getState()==0x04) result = true;
                    else result = false;
                }
                break;
            default : break;
		}
		// apply NOT if specified
		if ( _not ) {
			result = !result;
		}
        if (result)
            setState(Conditional.TRUE);
        else
            setState(Conditional.FALSE);
		return (result);
	}

    boolean compare(String value1, String value2, boolean caseInsensitive) {
        if (value1==null) {
            if (value2==null) return true;
            else return false;
        } else {
            if (value2==null) return false;
            value1 = value1.trim();
            value2 = value2.trim();
        }
        try {
            int n1 = Integer.parseInt(value1);
            try {
                int n2 = Integer.parseInt(value2);
                if (_num1 == 0) { // for former code
                    if (n1 == n2) return true;
                    else return false;
                }
                if (log.isDebugEnabled()) log.debug("Compare numbers: n1= "+n1+", to n2= "+n2);
                switch (_num1)  // both are numbers
                {
                    case LESS_THAN:
                        return (n1 < n2);
                    case LESS_THAN_OR_EQUAL:
                        return (n1 <= n2);
                    case EQUAL:
                        return (n1 == n2);
                    case GREATER_THAN_OR_EQUAL:
                        return (n1 >= n2);
                    case GREATER_THAN:
                        return (n1 > n2);
                }
            } catch (NumberFormatException nfe) {
                return false;   // n1 is a number, n2 is not
            }
            log.error("Compare 'numbers': value1= "+value1+", to value2= "+value2);
        } catch (NumberFormatException nfe) { 
            try {
                Integer.parseInt(value2);
                return false;     // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
            }
        }
        if (log.isDebugEnabled()) log.debug("Compare Strings: value1= "+value1+", to value2= "+value2);
        int compare = 0;
        if (caseInsensitive) {
            compare = value1.compareToIgnoreCase(value2);
        } else {
            compare = value1.compareTo(value2);
        }
        if (_num1 == 0) { // for former code
            if (compare == 0) return true;
            else return false;
        }
        switch (_num1)
        {   // fall through
            case LESS_THAN:
                if (compare < 0) return true;
                break;
            case LESS_THAN_OR_EQUAL:
                if (compare <= 0) return true;
                break;
            case EQUAL:
                if (compare == 0) return true;
                break;
            case GREATER_THAN_OR_EQUAL:
                if (compare >= 0) return true;
                break;
            case GREATER_THAN:
                if (compare > 0) return true;
                break;
        }
        return false;
    }

    private int fixMidnight(int time) {
        if (time>24*60)
            time -= 24*60;
        return time;
    }

	/**
	 * Convert Variable Type to Text String
	 */
	public static String getItemTypeString(int t) {
		switch (t) {
            case Conditional.ITEM_TYPE_SENSOR:
                return (rbx.getString("Sensor"));
            case Conditional.ITEM_TYPE_TURNOUT:
                return (rbx.getString("Turnout"));
            case Conditional.ITEM_TYPE_LIGHT:
                return (rbx.getString("Light"));
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                return (rbx.getString("SignalHead"));
            case Conditional.ITEM_TYPE_SIGNALMAST:
                return (rbx.getString("SignalMast"));
            case Conditional.ITEM_TYPE_MEMORY:
                return (rbx.getString("Memory"));
            case Conditional.ITEM_TYPE_CONDITIONAL:
                return (rbx.getString("Conditional"));
            case Conditional.ITEM_TYPE_WARRANT:
                return (rbx.getString("Warrant"));
            case Conditional.ITEM_TYPE_CLOCK:
                return (rbx.getString("FastClock"));
            case Conditional.ITEM_TYPE_OBLOCK:
                return (rbx.getString("OBlock"));
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                return (rbx.getString("EntryExit"));
        }
        return "";
    }

	/**
	 * get state name from Variable Test Type
	 */
	public static String getStateString(int t) {
		switch (t) {
            case Conditional.TYPE_NONE:
                return "";
            case Conditional.TYPE_SENSOR_ACTIVE:
                return (rbx.getString("SensorActive"));
            case Conditional.TYPE_SENSOR_INACTIVE:
                return (rbx.getString("SensorInactive"));
            case Conditional.TYPE_TURNOUT_THROWN:
                return (rbx.getString("TurnoutThrown"));
            case Conditional.TYPE_TURNOUT_CLOSED:
                return (rbx.getString("TurnoutClosed"));
            case Conditional.TYPE_CONDITIONAL_TRUE:
                return (rbx.getString("True"));
            case Conditional.TYPE_CONDITIONAL_FALSE:
                return (rbx.getString("False"));
            case Conditional.TYPE_LIGHT_ON:
                return (rbx.getString("LightOn"));
            case Conditional.TYPE_LIGHT_OFF:
                return (rbx.getString("LightOff"));
            case Conditional.TYPE_MEMORY_EQUALS:
                return (rbx.getString("StateMemoryEquals"));
            case Conditional.TYPE_MEMORY_COMPARE:
                return (rbx.getString("StateMemoryCompare"));
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                return ("");
            case Conditional.TYPE_SIGNAL_HEAD_RED:
                return (rbx.getString("StateSignalHeadRed"));
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                return (rbx.getString("StateSignalHeadYellow"));
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                return (rbx.getString("StateSignalHeadGreen"));
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
                return (rbx.getString("StateSignalHeadDark"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                return (rbx.getString("StateSignalHeadFlashRed"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                return (rbx.getString("StateSignalHeadFlashYellow"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                return (rbx.getString("StateSignalHeadFlashGreen"));
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
                return (rbx.getString("StateSignalHeadLit"));
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                return (rbx.getString("TypeSignalHeadHeld"));
            case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                return (rbx.getString("StateSignalHeadLunar"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                return (rbx.getString("StateSignalHeadFlashLunar"));
            case Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE:
                return (rbx.getString("StateMemoryEqualsInsensitive"));
            case Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE:
                return (rbx.getString("StateMemoryCompareInsensitive"));
            case Conditional.TYPE_ROUTE_FREE:
                return (rbx.getString("StateRouteFree"));
            case Conditional.TYPE_ROUTE_OCCUPIED:
                return (rbx.getString("stateRouteOccupied"));
            case Conditional.TYPE_ROUTE_ALLOCATED:
                return (rbx.getString("StateRouteReserved"));
            case Conditional.TYPE_ROUTE_SET:
                return (rbx.getString("StateRouteIsSet"));
            case Conditional.TYPE_TRAIN_RUNNING:
                return (rbx.getString("StateTrainRunning"));
            case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                return (rbx.getString("TypeSignalMastAspectEquals"));
            case Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS:
                return (rbx.getString("TypeSignalHeadAspectEquals"));
            case Conditional.TYPE_SIGNAL_MAST_LIT:
                return (rbx.getString("StateSignalMastLit"));
            case Conditional.TYPE_SIGNAL_MAST_HELD:
                return (rbx.getString("StateSignalMastHeld"));
            case Conditional.TYPE_ENTRYEXIT_ACTIVE:
                return (rbx.getString("StateEntryExitActive"));
            case Conditional.TYPE_ENTRYEXIT_INACTIVE:
                return (rbx.getString("StateEntryExitInactive"));
        }
        return "";
    }

	/**
	 * Convert Variable Test Type to Text String
	 */
	public static String getTestTypeString(int t) {
		switch (t) {
            case Conditional.TYPE_NONE:
                return "";
            case Conditional.TYPE_SENSOR_ACTIVE:
                return (rbx.getString("TypeSensorActive"));
            case Conditional.TYPE_SENSOR_INACTIVE:
                return (rbx.getString("TypeSensorInactive"));
            case Conditional.TYPE_TURNOUT_THROWN:
                return (rbx.getString("TypeTurnoutThrown"));
            case Conditional.TYPE_TURNOUT_CLOSED:
                return (rbx.getString("TypeTurnoutClosed"));
            case Conditional.TYPE_CONDITIONAL_TRUE:
                return (rbx.getString("TypeConditionalTrue"));
            case Conditional.TYPE_CONDITIONAL_FALSE:
                return (rbx.getString("TypeConditionalFalse"));
            case Conditional.TYPE_LIGHT_ON:
                return (rbx.getString("TypeLightOn"));
            case Conditional.TYPE_LIGHT_OFF:
                return (rbx.getString("TypeLightOff"));
            case Conditional.TYPE_MEMORY_EQUALS:
                return (rbx.getString("TypeMemoryEquals"));
            case Conditional.TYPE_MEMORY_COMPARE:
                return (rbx.getString("TypeMemoryCompare"));
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                return (rbx.getString("TypeFastClockRange"));
            case Conditional.TYPE_SIGNAL_HEAD_RED:
                return (rbx.getString("TypeSignalHeadRed"));
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                return (rbx.getString("TypeSignalHeadYellow"));
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                return (rbx.getString("TypeSignalHeadGreen"));
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
                return (rbx.getString("TypeSignalHeadDark"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                return (rbx.getString("TypeSignalHeadFlashRed"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                return (rbx.getString("TypeSignalHeadFlashYellow"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                return (rbx.getString("TypeSignalHeadFlashGreen"));
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
                return (rbx.getString("TypeSignalHeadLit"));
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                return (rbx.getString("TypeSignalHeadHeld"));
            case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                return (rbx.getString("TypeSignalHeadLunar"));
            case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                return (rbx.getString("TypeSignalHeadFlashLunar"));
            case Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE:
                return (rbx.getString("TypeMemoryEqualsInsensitive"));
            case Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE:
                return (rbx.getString("TypeMemoryCompareInsensitive"));
            case Conditional.TYPE_ROUTE_FREE:
                return (rbx.getString("TypeWarrantRouteFree"));
            case Conditional.TYPE_ROUTE_OCCUPIED:
                return (rbx.getString("TypeWarrantRouteOccupied"));
            case Conditional.TYPE_ROUTE_ALLOCATED:
                return (rbx.getString("TypeWarrantRouteAllocated"));
            case Conditional.TYPE_ROUTE_SET:
                return (rbx.getString("TypeRouteIsSet"));
            case Conditional.TYPE_TRAIN_RUNNING:
                return (rbx.getString("TypeTrainRunning"));
            case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                return (rbx.getString("TypeSignalMastAspectEquals"));
            case Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS:
                return (rbx.getString("TypeSignalHeadAspectEquals"));
            case Conditional.TYPE_SIGNAL_MAST_LIT:
                return (rbx.getString("TypeSignalMastLit"));
            case Conditional.TYPE_SIGNAL_MAST_HELD:
                return (rbx.getString("TypeSignalMastHeld"));
            case Conditional.TYPE_ENTRYEXIT_ACTIVE:
                return (rbx.getString("TypeEntryExitActive"));
            case Conditional.TYPE_ENTRYEXIT_INACTIVE:
                return (rbx.getString("TypeEntryExitInactive"));
        }
        return ("None");
    }

    public static String getCompareOperationString(int index) {
        switch(index) {
            case LESS_THAN:
                return (rbx.getString("LessThan"));
            case LESS_THAN_OR_EQUAL:
                return (rbx.getString("LessOrEqual"));
            case 0:
            case EQUAL:
                return (rbx.getString("Equal"));
            case GREATER_THAN_OR_EQUAL:
                return (rbx.getString("GreaterOrEqual"));
            case GREATER_THAN:
                return (rbx.getString("GreaterThan"));
        }
        return ("");
    }

    public static String getCompareSymbols(int index) {
        switch(index) {
            case LESS_THAN:
                return ("<");
            case LESS_THAN_OR_EQUAL:
                return ("<=");
            case 0:
            case EQUAL:
                return ("=");
            case GREATER_THAN_OR_EQUAL:
                return (">=");
            case GREATER_THAN:
                return (">");
        }
        return ("");
    }

	/**
	 * Identifies action Data from Text String Note: if string does not
	 * correspond to an action Data as defined in
	 * ConditionalAction, returns -1.
	 */
	public static int stringToVariableTest(String str) {
		if (str.equals(rbean.getString("SignalHeadStateRed"))) {
            return Conditional.TYPE_SIGNAL_HEAD_RED;
        }
        else if (str.equals(rbean.getString("SignalHeadStateYellow"))) {
            return Conditional.TYPE_SIGNAL_HEAD_YELLOW;
        }
        else if (str.equals(rbean.getString("SignalHeadStateGreen"))) {
            return Conditional.TYPE_SIGNAL_HEAD_GREEN;
        }
        else if (str.equals(rbean.getString("SignalHeadStateDark"))) {
            return Conditional.TYPE_SIGNAL_HEAD_DARK;
        }
        else if (str.equals(rbean.getString("SignalHeadStateFlashingRed"))) {
            return Conditional.TYPE_SIGNAL_HEAD_FLASHRED;
        }
        else if (str.equals(rbean.getString("SignalHeadStateFlashingYellow"))) {
            return Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW;
        }
        else if (str.equals(rbean.getString("SignalHeadStateFlashingGreen"))) {
            return Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN;
        }
        else if (str.equals(rbean.getString("SignalHeadStateLunar"))) {
            return Conditional.TYPE_SIGNAL_HEAD_LUNAR;
        }
        else if (str.equals(rbean.getString("SignalHeadStateFlashingLunar"))) {
            return Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR;
        }
        // empty strings can occur frequently with types that have no integer data
        if (str.length() > 0)
        {
            log.warn("Unexpected parameter to stringToVariableTest("+str+")");
        }
		return -1;
	}
	
    public String toString() {
        String type = getTestTypeString(_type);
        int itemType = Conditional.TEST_TO_ITEM[_type];
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("Sensor"), getName(), type} );
            case Conditional.ITEM_TYPE_TURNOUT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("Turnout"), getName(), type} );
            case Conditional.ITEM_TYPE_LIGHT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("Light"), getName(), type} );
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                if ((_type==Conditional.TYPE_SIGNAL_HEAD_LIT) ||
                        (_type==Conditional.TYPE_SIGNAL_HEAD_HELD)) {
                    return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                                 new Object[] {rbx.getString("SignalHead"), getName(), type} );
                } else {
                    return java.text.MessageFormat.format(rbx.getString("SignalHeadStateDescrpt"),
                                 new Object[] {rbx.getString("SignalHead"), getName(), type} );
                }
            case Conditional.ITEM_TYPE_SIGNALMAST:
                if ((_type==Conditional.TYPE_SIGNAL_MAST_LIT) ||
                        (_type==Conditional.TYPE_SIGNAL_MAST_HELD)) {
                    return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                                 new Object[] {rbx.getString("SignalMast"), getName(), type} );
                } else {
                    return java.text.MessageFormat.format(rbx.getString("SignalMastStateDescrpt"),
                                 new Object[] {rbx.getString("SignalMast"), getName(), _dataString} );
                }
            case Conditional.ITEM_TYPE_MEMORY:
                if ((_type==Conditional.TYPE_MEMORY_EQUALS) ||
                        (_type==Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE)) {
                    return java.text.MessageFormat.format(rbx.getString("MemoryValueDescrpt"),
                                 new Object[] {rbx.getString("Memory"), getName(), 
                                               getCompareSymbols(_num1), _dataString} );
                } else {
                    return java.text.MessageFormat.format(rbx.getString("MemoryCompareDescrpt"),
                                 new Object[] {rbx.getString("Memory"), getName(), 
                                               getCompareSymbols(_num1), _dataString} );
                }
            case Conditional.ITEM_TYPE_CONDITIONAL:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("Conditional"), getName(), type} );
            case Conditional.ITEM_TYPE_WARRANT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("WarrantRoute"), getName(), type} );
            case Conditional.ITEM_TYPE_CLOCK:
                return java.text.MessageFormat.format(rbx.getString("FastClockDescrpt"),
                             new Object[] {rbx.getString("FastClock"), 
                          LogixTableAction.formatTime(_num1 / 60, _num1 - ((_num1 / 60) * 60)),
                          LogixTableAction.formatTime(_num2 / 60, _num2 - ((_num2 / 60) * 60)) });
            case Conditional.ITEM_TYPE_OBLOCK:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("OBlockStatus"), getName(), _dataString} );
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                    return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                             new Object[] {rbx.getString("EntryExit"), getBean().getUserName(), type} );
            case Conditional.TYPE_NONE:
                return getName()+" type "+type;
        }
        return super.toString();
    }

	static final Logger log = org.apache.log4j.Logger
		.getLogger(ConditionalVariable.class.getName());
}

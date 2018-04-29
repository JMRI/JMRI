package jmri;

import java.util.Date;
import java.util.ResourceBundle;
import jmri.jmrit.beantable.LogixTableAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The variable used in the antecedent (the 'if' part) of the Conditional.
 * proposition. The states of ConditionalVariables and logic expression of the
 * antecedent determine the state of the Conditional.
 * <P>
 * ConditionalVariable objects are fully mutable, so use the default equals()
 * operator that checks for identical objects, not identical contents.
 *
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
 * @author Pete Cressman Copyright (C) 2009
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class ConditionalVariable {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    public static final int NUM_COMPARE_OPERATIONS = 5;
    public static final int LESS_THAN = 1;
    public static final int LESS_THAN_OR_EQUAL = 2;
    public static final int EQUAL = 3;
    public static final int GREATER_THAN_OR_EQUAL = 4;
    public static final int GREATER_THAN = 5;

    private boolean _not = false;
    // Not a variable attribute, but retained as an artifact of previous releases.  This will be used
    // as the default operator immediately to the left of this variable in the antecedent statement.
    // It may be over written by the antecedent statement in the Conditional to which this variable
    // belongs.
    private int _opern = Conditional.OPERATOR_NONE;
    private int _type = Conditional.TYPE_NONE;
    private String _name = "";
    private String _dataString = "";
    private int _num1 = 0;
    private int _num2 = 0;
    private String _guiName = "";       // Contains the user name of the referenced conditional
    private NamedBeanHandle<?> _namedBean = null;
    //private NamedBeanHandle<Sensor> _namedSensorBean = null;
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    // Name clarification: Formerly was named '_triggersCalculation' because it controlled whether
    // a listener was installed for this device and thus trigger calcuation of the Conditional.
    // Now named '_triggersActions' because listeners are always installed for activated Logix
    // Conditionals and this parameter nows controls whether, if its change of state changes the
    // state of the conditional, should that also  trigger the actions.
    private boolean _triggersActions = true;
    private int _state = NamedBean.UNKNOWN;        // tri-state

    public ConditionalVariable() {
    }

    public ConditionalVariable(boolean not, int opern, int type, String name, boolean trigger) {
        _not = not;
        _opern = opern;
        _type = type;
        _name = name;
        _triggersActions = trigger;
        _guiName = "";
        try {
            int itemType = Conditional.TEST_TO_ITEM[_type];
            switch (itemType) {
                case Conditional.ITEM_TYPE_SENSOR:
                    try {
                        Sensor sn = InstanceManager.sensorManagerInstance().provideSensor(_name);
                        _namedBean = nbhm.getNamedBeanHandle(_name, sn);
                    } catch (IllegalArgumentException e) {
                        log.error("invalid sensor name= \"" + _name + "\" in state variable");
                    }
                    break;
                case Conditional.ITEM_TYPE_TURNOUT:
                    try {
                        Turnout tn = InstanceManager.turnoutManagerInstance().provideTurnout(_name);
                        _namedBean = nbhm.getNamedBeanHandle(_name, tn);
                    } catch (IllegalArgumentException e) {
                        log.error("invalid turnout name= \"" + _name + "\" in state variable");
                    }
                    break;
                case Conditional.ITEM_TYPE_MEMORY:
                    try {
                        Memory my = InstanceManager.memoryManagerInstance().provideMemory(_name);
                        _namedBean = nbhm.getNamedBeanHandle(_name, my);
                    } catch (IllegalArgumentException e) {
                        log.error("invalid memory name= \"" + _name + "\" in state variable");
                    }
                    break;
                case Conditional.ITEM_TYPE_LIGHT:
                    try {
                        Light l = InstanceManager.lightManagerInstance().provideLight(_name);
                        _namedBean = nbhm.getNamedBeanHandle(_name, l);
                    } catch (IllegalArgumentException e) {
                        log.error("invalid light name= \"" + _name + "\" in state variable");
                    }
                    break;
                case Conditional.ITEM_TYPE_SIGNALHEAD:
                    SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(_name);
                    if (s == null) {
                        log.error("invalid signalhead name= \"" + _name + "\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, s);
                    break;
                case Conditional.ITEM_TYPE_SIGNALMAST:
                    try {
                        SignalMast sm = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(_name);
                        _namedBean = nbhm.getNamedBeanHandle(_name, sm);
                    } catch (IllegalArgumentException e) {
                        log.error("invalid signalmast name= \"" + _name + "\" in state variable");
                    }
                    break;
                case Conditional.ITEM_TYPE_ENTRYEXIT:
                    NamedBean nb = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getBySystemName(_name);
                    if (nb == null) {
                        log.error("invalid entry exit name= \"" + _name + "\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, nb);
                    break;
                case Conditional.ITEM_TYPE_CONDITIONAL:
                    Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getConditional(_name);
                    if (c == null) {
                        log.error("invalid conditiona; name= \"" + _name + "\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, c);
                    break;
                case Conditional.ITEM_TYPE_WARRANT:
                    Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(_name);
                    if (w == null) {
                        log.error("invalid warrant name= \"" + _name + "\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, w);
                    break;
                case Conditional.ITEM_TYPE_OBLOCK:
                    OBlock b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(_name);
                    if (b == null) {
                        log.error("invalid block name= \"" + _name + "\" in state variable");
                        return;
                    }
                    _namedBean = nbhm.getNamedBeanHandle(_name, b);
                    break;

                default:
                    log.warn("Unexpected type in ConditionalVariable ctor: {} -> {}", _type, itemType);
                    break;
            }
        } catch (java.lang.NumberFormatException ex) {
            //Can be Considered Normal where the logix is loaded prior to any other beans
        } catch (IllegalArgumentException ex) {
            log.warn("could not provide \"{}\" in constructor", _name);
            _namedBean = null;
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
        switch (opern) {
            case Conditional.OPERATOR_AND_NOT:
                _opern = Conditional.OPERATOR_AND;
                _not = true;
                break;
            case Conditional.OPERATOR_NOT:
                _opern = Conditional.OPERATOR_NONE;
                _not = true;
                break;
            default:
                _opern = opern;
                break;
        }
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    public String getName() {
        if (_namedBean != null) {
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

        try {
            switch (itemType) {
                case Conditional.TYPE_NONE:
                    break;
                case Conditional.ITEM_TYPE_CLOCK:
                    break; // no beans for these, at least that I know of
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
                    bean = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(_name);
                    break;
                case Conditional.ITEM_TYPE_SIGNALHEAD:
                    bean = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(_name);
                    break;
                case Conditional.ITEM_TYPE_CONDITIONAL:
                    bean = InstanceManager.getDefault(jmri.ConditionalManager.class).getConditional(_name);
                    break;
                case Conditional.ITEM_TYPE_WARRANT:
                    bean = InstanceManager.getDefault(WarrantManager.class).getWarrant(_name);
                    break;
                case Conditional.ITEM_TYPE_OBLOCK:
                    bean = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(_name);
                    break;
                case Conditional.ITEM_TYPE_ENTRYEXIT:
                    bean = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(_name);
                    break;
                default:
                    log.error("Type " + itemType + " not set for " + _name);
            }

            //Once all refactored, we should probably register an error if the bean is returned null.
            if (bean != null) {
                _namedBean = nbhm.getNamedBeanHandle(_name, bean);
            }

        } catch (IllegalArgumentException ex) {
            log.warn("Did not have or create \"{}\" in setName", _name);
            _namedBean = null;
        }
    }

    public NamedBeanHandle<?> getNamedBean() {
        return _namedBean;
    }

    public NamedBean getBean() {
        if (_namedBean != null) {
            return _namedBean.getBean();
        }
        setName(_name); //ReApply name as that will create namedBean, save replicating it here
        if (_namedBean != null) {
            return _namedBean.getBean();
        }
        return null;
    }

    public String getDataString() {
        if (Conditional.TEST_TO_ITEM[_type] == Conditional.ITEM_TYPE_MEMORY
                && _namedBeanData != null) {
            return _namedBeanData.getName();
        }
        return _dataString;
    }

    public void setDataString(String data) {
        _dataString = data;
        if (data != null && !data.equals("") && Conditional.TEST_TO_ITEM[_type] == Conditional.ITEM_TYPE_MEMORY) {
            NamedBean bean = InstanceManager.memoryManagerInstance().getMemory(data);
            if (bean != null) {
                _namedBeanData = nbhm.getNamedBeanHandle(data, bean);
            }
        }
    }

    private NamedBeanHandle<?> _namedBeanData = null;

    public NamedBean getNamedBeanData() {
        if (_namedBeanData != null) {
            return _namedBeanData.getBean();
        }
        return null;
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
     * @since 4.7.4
     * @return the GUI name for the referenced conditional.
     */
    public String getGuiName() {
        return _guiName;
    }

    /**
     * Set the GUI name for the conditional state variable.
     * @since 4.7.4
     * @param guiName The referenced Conditional user name.
     */
    public void setGuiName(String guiName) {
        _guiName = guiName;
    }


    /**
     * If change of state of this object causes a change of state of the
     * Conditional, should any actions be executed.
     *
     * @return true if actions should be performed if triggered
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
        } else {
            _state = Conditional.FALSE;
        }
    }

    public String getTestTypeString() {
        return getTestTypeString(_type);
    }

    public String getOpernString() {
        switch (_opern) {
            case Conditional.OPERATOR_AND:
                return Bundle.getMessage("LogicAND"); // NOI18N
            case Conditional.OPERATOR_NOT:
                return Bundle.getMessage("LogicNOT"); // NOI18N
            case Conditional.OPERATOR_AND_NOT:
                return Bundle.getMessage("LogicAND"); // NOI18N
            case Conditional.OPERATOR_NONE:
                return "";
            case Conditional.OPERATOR_OR:
                return Bundle.getMessage("LogicOR"); // NOI18N
            default:
                return "";
        }
    }

    /**
     * Evaluates this State Variable.
     *
     * @return true if variable evaluates true, otherwise false.
     */
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public boolean evaluate() {
        boolean result = true;
        // evaluate according to state variable type
        int itemType = Conditional.TEST_TO_ITEM[_type];
        log.debug("evaluate: \"{}\" type= {} itemType= {}", getName(), _type, itemType);
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                //Sensor sn = InstanceManager.sensorManagerInstance().provideSensor(getName());
                Sensor sn = (Sensor) getBean();
                if (sn == null) {
                    log.error("invalid sensor name= \"" + getName() + "\" in state variable");
                    return false;
                }
                if (_type == Conditional.TYPE_SENSOR_ACTIVE) {
                    result = sn.getState() == Sensor.ACTIVE;
                } else {
                    result = sn.getState() == Sensor.INACTIVE;
                }
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                Turnout t = (Turnout) getBean();
                if (t == null) {
                    log.error("invalid turnout name= \"" + getName() + "\" in state variable");
                    return false;
                }
                if (_type == Conditional.TYPE_TURNOUT_THROWN) {
                    result = t.getKnownState() == Turnout.THROWN;
                } else {
                    result = t.getKnownState() == Turnout.CLOSED;
                }
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                Light lgt = (Light) getBean();
                if (lgt == null) {
                    log.error("invalid light name= \"" + getName() + "\" in state variable");
                    return false;
                }
                if (_type == Conditional.TYPE_LIGHT_ON) {
                    result = lgt.getState() == Light.ON;
                } else {
                    result = lgt.getState() == Light.OFF;
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                SignalMast f = (SignalMast) getBean();
                if (f == null) {
                    log.error("invalid signal mast name= \"" + getName() + "\" in state variable");
                    return false;
                }
                switch (_type) {
                    case Conditional.TYPE_SIGNAL_MAST_LIT:
                        result = f.getLit();
                        break;
                    case Conditional.TYPE_SIGNAL_MAST_HELD:
                        result = f.getHeld();
                        break;
                    case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                        if (f.getAspect() == null) {
                            result = false;
                        } else {
                            result = f.getAspect().equals(_dataString);
                        }
                        break;
                    default:
                        log.warn("unexpected type {} in ITEM_TYPE_SIGNALMAST", _type);
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                SignalHead h = (SignalHead) getBean();
                if (h == null) {
                    log.error("invalid signal head name= \"" + getName() + "\" in state variable");
                    return false;
                }
                switch (_type) {
                    case Conditional.TYPE_SIGNAL_HEAD_RED:
                        result = h.getAppearance() == SignalHead.RED;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                        result = h.getAppearance() == SignalHead.YELLOW;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                        result = h.getAppearance() == SignalHead.GREEN;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_DARK:
                        result = h.getAppearance() == SignalHead.DARK;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                        result = h.getAppearance() == SignalHead.FLASHRED;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                        result = h.getAppearance() == SignalHead.FLASHYELLOW;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                        result = h.getAppearance() == SignalHead.FLASHGREEN;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                        result = h.getAppearance() == SignalHead.LUNAR;
                        break;
                    case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                        result = h.getAppearance() == SignalHead.FLASHLUNAR;
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
                    log.error("invalid memory name= \"" + getName() + "\" in state variable");
                    return false;
                }
                String value1 = null;
                String value2 = null;
                if (m.getValue() != null) {
                    value1 = m.getValue().toString();
                }
                boolean caseInsensitive = ((_type == Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE)
                        || (_type == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE));
                if ((_type == Conditional.TYPE_MEMORY_COMPARE)
                        || (_type == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE)) {
                    Memory m2;
                    if (_namedBeanData != null) {
                        m2 = (Memory) _namedBeanData.getBean();
                    } else {
                        try {
                            m2 = InstanceManager.memoryManagerInstance().provideMemory(_dataString);
                        } catch (IllegalArgumentException ex) {
                            log.error("invalid data memory name= \"" + _dataString + "\" in state variable");
                            return false;
                        }
                    }
                    if (m2.getValue() != null) {
                        value2 = m2.getValue().toString();
                    }
                } else {
                    value2 = _dataString;
                }
                result = compare(value1, value2, caseInsensitive);
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(getName());
                if (c == null) {
                    c = InstanceManager.getDefault(jmri.ConditionalManager.class).getByUserName(getName());
                    if (c == null) {
                        log.error("invalid conditional name= \"" + getName() + "\" in state variable");
                        return false;
                    }
                }
                if (_type == Conditional.TYPE_CONDITIONAL_TRUE) {
                    result = c.getState() == Conditional.TRUE;
                } else {
                    result = c.getState() == Conditional.FALSE;
                }
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(getName());
                if (w == null) {
                    log.error("invalid Warrant name= \"" + getName() + "\" in state variable");
                    return false;
                }
                switch (_type) {
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
                Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
                Date currentTime = fastClock.getTime();
                int currentMinutes = (currentTime.getHours() * 60) + currentTime.getMinutes();
                int beginTime = fixMidnight(_num1);
                int endTime = fixMidnight(_num2);
                // check if current time is within range specified
                if (beginTime <= endTime) {
                    // range is entirely within one day
                    result = (beginTime <= currentMinutes) && (currentMinutes <= endTime);
                } else {
                    // range includes midnight
                    result = beginTime <= currentMinutes || currentMinutes <= endTime;
                }
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                OBlock b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(getName());
                if (b == null) {
                    log.error("invalid OBlock name= \"" + getName() + "\" in state variable");
                    return false;
                }
                result = b.statusIs(_dataString);
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                NamedBean e = getBean();
                if (_type == Conditional.TYPE_ENTRYEXIT_ACTIVE) {
                    result = e.getState() == 0x02;
                } else {
                    result = e.getState() == 0x04;
                }
                break;
            default:
                break;
        }
        // apply NOT if specified
        if (_not) {
            result = !result;
        }
        if (result) {
            setState(Conditional.TRUE);
        } else {
            setState(Conditional.FALSE);
        }
        return (result);
    }

    /**
     * Compare two values using the comparator set using the comparison
     * instructions in {@link #setNum1(int)}.
     *
     * <strong>Note:</strong> {@link #getNum1()} must be one of {@link #LESS_THAN},
     * {@link #LESS_THAN_OR_EQUAL}, {@link #EQUAL},
     * {@link #GREATER_THAN_OR_EQUAL}, or {@link #GREATER_THAN}.
     *
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per getNum1(); false otherwise
     */
    boolean compare(String value1, String value2, boolean caseInsensitive) {
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            value1 = value1.trim();
            value2 = value2.trim();
        }
        try {
            int n1 = Integer.parseInt(value1);
            try {
                int n2 = Integer.parseInt(value2);
                if (_num1 == 0) { // for former code
                    return n1 == n2;
                }
                log.debug("Compare numbers: n1= {} to n2= {}", n1, n2);
                switch (_num1) // both are numbers
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
                    default:
                        log.error("Compare numbers: invalid compare case: {}", _num1);
                        return false;
                }
            } catch (NumberFormatException nfe) {
                return false;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return false;     // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
            }
        }
        log.debug("Compare Strings: value1= {} to value2= {}", value1, value2);
        int compare;
        if (caseInsensitive) {
            compare = value1.compareToIgnoreCase(value2);
        } else {
            compare = value1.compareTo(value2);
        }
        if (_num1 == 0) { // for former code
            return compare == 0;
        }
        switch (_num1) {
            case LESS_THAN:
                if (compare < 0) {
                    return true;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (compare <= 0) {
                    return true;
                }
                break;
            case EQUAL:
                if (compare == 0) {
                    return true;
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (compare >= 0) {
                    return true;
                }
                break;
            case GREATER_THAN:
                if (compare > 0) {
                    return true;
                }
                break;
            default:
                // fall through
                break;
        }
        return false;
    }

    private int fixMidnight(int time) {
        if (time > 24 * 60) {
            time -= 24 * 60;
        }
        return time;
    }

    /**
     * Convert Variable Type to Text String
     *
     * @param t the type
     * @return the localized description
     */
    public static String getItemTypeString(int t) {
        switch (t) {
            case Conditional.ITEM_TYPE_SENSOR:
                return Bundle.getMessage("BeanNameSensor"); // NOI18N
            case Conditional.ITEM_TYPE_TURNOUT:
                return Bundle.getMessage("BeanNameTurnout"); // NOI18N
            case Conditional.ITEM_TYPE_LIGHT:
                return Bundle.getMessage("BeanNameLight"); // NOI18N
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                return Bundle.getMessage("BeanNameSignalHead"); // NOI18N
            case Conditional.ITEM_TYPE_SIGNALMAST:
                return Bundle.getMessage("BeanNameSignalMast"); // NOI18N
            case Conditional.ITEM_TYPE_MEMORY:
                return Bundle.getMessage("BeanNameMemory"); // NOI18N
            case Conditional.ITEM_TYPE_CONDITIONAL:
                return Bundle.getMessage("BeanNameConditional"); // NOI18N
            case Conditional.ITEM_TYPE_WARRANT:
                return Bundle.getMessage("BeanNameWarrant"); // NOI18N
            case Conditional.ITEM_TYPE_CLOCK:
                return Bundle.getMessage("FastClock"); // NOI18N
            case Conditional.ITEM_TYPE_OBLOCK:
                return Bundle.getMessage("BeanNameOBlock"); // NOI18N
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                return Bundle.getMessage("EntryExit"); // NOI18N
            default:
                return "";
        }
    }

    /**
     * Get state name from Variable Test Type
     *
     * @param t the state
     * @return the localized description
     */
    public static String describeState(int t) {
        switch (t) {
            case Conditional.TYPE_NONE:
                return ""; // NOI18N
            case Conditional.TYPE_SENSOR_ACTIVE:
                return Bundle.getMessage("SensorStateActive"); // NOI18N
            case Conditional.TYPE_SENSOR_INACTIVE:
                return Bundle.getMessage("SensorStateInactive"); // NOI18N
            case Conditional.TYPE_TURNOUT_THROWN:
                return Bundle.getMessage("TurnoutStateThrown"); // NOI18N
            case Conditional.TYPE_TURNOUT_CLOSED:
                return Bundle.getMessage("TurnoutStateClosed"); // NOI18N
            case Conditional.TYPE_CONDITIONAL_TRUE:
                return rbx.getString("True"); // NOI18N
            case Conditional.TYPE_CONDITIONAL_FALSE:
                return rbx.getString("False"); // NOI18N
            case Conditional.TYPE_LIGHT_ON:
                return rbx.getString("LightOn"); // NOI18N
            case Conditional.TYPE_LIGHT_OFF:
                return rbx.getString("LightOff"); // NOI18N
            case Conditional.TYPE_MEMORY_EQUALS:
                return rbx.getString("StateMemoryEquals"); // NOI18N
            case Conditional.TYPE_MEMORY_COMPARE:
                return rbx.getString("StateMemoryCompare"); // NOI18N
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                return ""; // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_RED:
                return Bundle.getMessage("SignalHeadStateRed"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                return Bundle.getMessage("SignalHeadStateYellow"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                return Bundle.getMessage("SignalHeadStateGreen"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
                return Bundle.getMessage("SignalHeadStateDark"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                return Bundle.getMessage("SignalHeadStateFlashingRed"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                return Bundle.getMessage("SignalHeadStateFlashingYellow"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                return Bundle.getMessage("SignalHeadStateFlashingGreen"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                return Bundle.getMessage("SignalHeadStateHeld"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                return Bundle.getMessage("SignalHeadStateLunar"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                return Bundle.getMessage("SignalHeadStateFlashingLunar"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
                return rbx.getString("TypeSignalHeadLit"); // NOI18N
            case Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE:
                return rbx.getString("StateMemoryEqualsInsensitive"); // NOI18N
            case Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE:
                return rbx.getString("StateMemoryCompareInsensitive"); // NOI18N
            case Conditional.TYPE_ROUTE_FREE:
                return rbx.getString("StateRouteFree"); // NOI18N
            case Conditional.TYPE_ROUTE_OCCUPIED:
                return rbx.getString("stateRouteOccupied"); // NOI18N
            case Conditional.TYPE_ROUTE_ALLOCATED:
                return rbx.getString("StateRouteReserved"); // NOI18N
            case Conditional.TYPE_ROUTE_SET:
                return rbx.getString("StateRouteIsSet"); // NOI18N
            case Conditional.TYPE_TRAIN_RUNNING:
                return rbx.getString("StateTrainRunning"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                return rbx.getString("TypeSignalMastAspectEquals"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS:
                return rbx.getString("TypeSignalHeadAspectEquals"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_LIT:
                return rbx.getString("StateSignalMastLit"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_HELD:
                return rbx.getString("StateSignalMastHeld"); // NOI18N
            case Conditional.TYPE_ENTRYEXIT_ACTIVE:
                return Bundle.getMessage("SensorStateActive"); // NOI18N
            case Conditional.TYPE_ENTRYEXIT_INACTIVE:
                return Bundle.getMessage("SensorStateInactive"); // NOI18N
            default:
                log.warn("Unhandled condition type: {}", t); // NOI18N
                return "<none>";
        }
    }

    /**
     * Convert Variable Test Type to Text String.
     *
     * @param t the type
     * @return the localized description
     */
    public static String getTestTypeString(int t) {
        switch (t) {
            case Conditional.TYPE_NONE:
                return "";
            case Conditional.TYPE_SENSOR_ACTIVE:
                return rbx.getString("TypeSensorActive"); // NOI18N
            case Conditional.TYPE_SENSOR_INACTIVE:
                return rbx.getString("TypeSensorInactive"); // NOI18N
            case Conditional.TYPE_TURNOUT_THROWN:
                return rbx.getString("TypeTurnoutThrown"); // NOI18N
            case Conditional.TYPE_TURNOUT_CLOSED:
                return rbx.getString("TypeTurnoutClosed"); // NOI18N
            case Conditional.TYPE_CONDITIONAL_TRUE:
                return rbx.getString("TypeConditionalTrue"); // NOI18N
            case Conditional.TYPE_CONDITIONAL_FALSE:
                return rbx.getString("TypeConditionalFalse"); // NOI18N
            case Conditional.TYPE_LIGHT_ON:
                return rbx.getString("TypeLightOn"); // NOI18N
            case Conditional.TYPE_LIGHT_OFF:
                return rbx.getString("TypeLightOff"); // NOI18N
            case Conditional.TYPE_MEMORY_EQUALS:
                return rbx.getString("TypeMemoryEquals"); // NOI18N
            case Conditional.TYPE_MEMORY_COMPARE:
                return rbx.getString("TypeMemoryCompare"); // NOI18N
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                return rbx.getString("TypeFastClockRange"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_RED:
                return rbx.getString("TypeSignalHeadRed"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                return rbx.getString("TypeSignalHeadYellow"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                return rbx.getString("TypeSignalHeadGreen"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
                return rbx.getString("TypeSignalHeadDark"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                return rbx.getString("TypeSignalHeadFlashRed"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                return rbx.getString("TypeSignalHeadFlashYellow"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                return rbx.getString("TypeSignalHeadFlashGreen"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
                return rbx.getString("TypeSignalHeadLit"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                return rbx.getString("TypeSignalHeadHeld"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_LUNAR:
                return rbx.getString("TypeSignalHeadLunar"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR:
                return rbx.getString("TypeSignalHeadFlashLunar"); // NOI18N
            case Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE:
                return rbx.getString("TypeMemoryEqualsInsensitive"); // NOI18N
            case Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE:
                return rbx.getString("TypeMemoryCompareInsensitive"); // NOI18N
            case Conditional.TYPE_ROUTE_FREE:
                return rbx.getString("TypeWarrantRouteFree"); // NOI18N
            case Conditional.TYPE_ROUTE_OCCUPIED:
                return rbx.getString("TypeWarrantRouteOccupied"); // NOI18N
            case Conditional.TYPE_ROUTE_ALLOCATED:
                return rbx.getString("TypeWarrantRouteAllocated"); // NOI18N
            case Conditional.TYPE_ROUTE_SET:
                return rbx.getString("TypeRouteIsSet"); // NOI18N
            case Conditional.TYPE_TRAIN_RUNNING:
                return rbx.getString("TypeTrainRunning"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS:
                return rbx.getString("TypeSignalMastAspectEquals"); // NOI18N
            case Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS:
                return rbx.getString("TypeSignalHeadAspectEquals"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_LIT:
                return rbx.getString("TypeSignalMastLit"); // NOI18N
            case Conditional.TYPE_SIGNAL_MAST_HELD:
                return rbx.getString("TypeSignalMastHeld"); // NOI18N
            case Conditional.TYPE_ENTRYEXIT_ACTIVE:
                return rbx.getString("TypeEntryExitActive"); // NOI18N
            case Conditional.TYPE_ENTRYEXIT_INACTIVE:
                return rbx.getString("TypeEntryExitInactive"); // NOI18N
            default:
                // fall though
                break;
        }
        return Bundle.getMessage("NONE");
    }

    public static String getCompareOperationString(int index) {
        switch (index) {
            case LESS_THAN:
                return rbx.getString("LessThan"); // NOI18N
            case LESS_THAN_OR_EQUAL:
                return rbx.getString("LessOrEqual"); // NOI18N
            case 0:
            case EQUAL:
                return rbx.getString("Equal"); // NOI18N
            case GREATER_THAN_OR_EQUAL:
                return rbx.getString("GreaterOrEqual"); // NOI18N
            case GREATER_THAN:
                return rbx.getString("GreaterThan"); // NOI18N
            default:
                // fall through
                break;
        }
        return ""; // NOI18N
    }

    public static String getCompareSymbols(int index) {
        switch (index) {
            case LESS_THAN:
                return "<"; // NOI18N
            case LESS_THAN_OR_EQUAL:
                return "<="; // NOI18N
            case 0:
            case EQUAL:
                return "="; // NOI18N
            case GREATER_THAN_OR_EQUAL:
                return ">="; // NOI18N
            case GREATER_THAN:
                return ">"; // NOI18N
            default:
                break;
        }
        return ""; // NOI18N
    }

    /**
     * Identifies action Data from Text String. Note: if string does not
     * correspond to an action Data as defined in ConditionalAction, returns -1.
     *
     * @param str the text to check
     * @return the conditional action type or -1 if no match
     */
    public static int stringToVariableTest(String str) {
        if (str.equals(Bundle.getMessage("SignalHeadStateRed"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_RED;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateYellow"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_YELLOW;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateGreen"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_GREEN;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateDark"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_DARK;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingRed"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_FLASHRED;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingYellow"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingGreen"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateLunar"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_LUNAR;
        } else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingLunar"))) { // NOI18N
            return Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR;
        }
        // empty strings can occur frequently with types that have no integer data
        if (str.length() > 0) {
            log.warn("Unexpected parameter to stringToVariableTest(" + str + ")");
        }
        return -1;
    }

    @Override
    public String toString() {
        String type = getTestTypeString(_type);
        int itemType = Conditional.TEST_TO_ITEM[_type];
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{Bundle.getMessage("BeanNameSensor"), getName(), type});
            case Conditional.ITEM_TYPE_TURNOUT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{Bundle.getMessage("BeanNameTurnout"), getName(), type});
            case Conditional.ITEM_TYPE_LIGHT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{Bundle.getMessage("BeanNameLight"), getName(), type});
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                if ((_type == Conditional.TYPE_SIGNAL_HEAD_LIT)
                        || (_type == Conditional.TYPE_SIGNAL_HEAD_HELD)) {
                    return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameSignalHead"), getName(), type});
                } else {
                    return java.text.MessageFormat.format(rbx.getString("SignalHeadStateDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameSignalHead"), getName(), type});
                }
            case Conditional.ITEM_TYPE_SIGNALMAST:
                if ((_type == Conditional.TYPE_SIGNAL_MAST_LIT)
                        || (_type == Conditional.TYPE_SIGNAL_MAST_HELD)) {
                    return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameSignalMast"), getName(), type}); // NOI18N
                } else {
                    return java.text.MessageFormat.format(rbx.getString("SignalMastStateDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameSignalMast"), getName(), _dataString}); // NOI18N
                }
            case Conditional.ITEM_TYPE_MEMORY:
                if ((_type == Conditional.TYPE_MEMORY_EQUALS)
                        || (_type == Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE)) {
                    return java.text.MessageFormat.format(rbx.getString("MemoryValueDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameMemory"), getName(), // NOI18N
                                getCompareSymbols(_num1), _dataString});
                } else {
                    return java.text.MessageFormat.format(rbx.getString("MemoryCompareDescrpt"),
                            new Object[]{Bundle.getMessage("BeanNameMemory"), getName(), // NOI18N
                                getCompareSymbols(_num1), _dataString});
                }
            case Conditional.ITEM_TYPE_CONDITIONAL:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{Bundle.getMessage("BeanNameConditional"), getGuiName(), type}); // NOI18N
            case Conditional.ITEM_TYPE_WARRANT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{rbx.getString("WarrantRoute"), getName(), type});
            case Conditional.ITEM_TYPE_CLOCK:
                return java.text.MessageFormat.format(rbx.getString("FastClockDescrpt"),
                        new Object[]{Bundle.getMessage("FastClock"),
                            LogixTableAction.formatTime(_num1 / 60, _num1 - ((_num1 / 60) * 60)),
                            LogixTableAction.formatTime(_num2 / 60, _num2 - ((_num2 / 60) * 60))});
            case Conditional.ITEM_TYPE_OBLOCK:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{rbx.getString("OBlockStatus"), getName(), _dataString});
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                return java.text.MessageFormat.format(rbx.getString("VarStateDescrpt"),
                        new Object[]{Bundle.getMessage("EntryExit"), getBean().getUserName(), type}); // NOI18N
            case Conditional.TYPE_NONE:
                return getName() + " type " + type;
            default:
                // fall through
                break;
        }
        return super.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalVariable.class);
}

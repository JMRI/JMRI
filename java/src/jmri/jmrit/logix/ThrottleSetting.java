package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SpeedStepMode;

import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oct 2020 - change formats to allow I18N of parameters
 * @author  Pete Cressman Copyright (C) 2009, 2020
 */
public class ThrottleSetting {

    static final int CMD_SPEED = 1;
    static final int CMD_SPEEDSTEP = 2;
    static final int CMD_FORWARD = 3;
    static final int CMD_FTN = 4;
    static final int CMD_LATCH = 5;
    static final int CMD_NOOP = 6;
    static final int CMD_SET_SENSOR = 7;
    static final int CMD_WAIT_SENSOR = 8;
    static final int CMD_RUN_WARRANT = 9;

    public enum Command {
        SPEED(CMD_SPEED, true, "speed"),
        FORWARD(CMD_FORWARD, true, "forward"),
        FKEY(CMD_FTN, true, "setFunction"),
        LATCHF(CMD_LATCH, true, "setKeyMomentary"),
        SET_SENSOR(CMD_SET_SENSOR, false, "SetSensor"),
        WAIT_SENSOR(CMD_WAIT_SENSOR, false, "WaitSensor"),
        RUN_WARRANT(CMD_RUN_WARRANT, false, "runWarrant"),
        NOOP(CMD_NOOP, true, "NoOp"),
        SPEEDSTEP(CMD_SPEEDSTEP, true, "speedstep");

        int _command;
        boolean _hasBlock; // when bean is an OBlock.
        String _bundleKey; // key to get command display name

        Command(int cmd, boolean hasBlock, String bundleName) {
            _command = cmd;
            _hasBlock = hasBlock;
            _bundleKey = bundleName;
        }

        public int getIntId() {
            return _command;
        }

        boolean hasBlockName() {
            return _hasBlock;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    static final int VALUE_FLOAT = 1;
    static final int VALUE_NOOP = 2;
    static final int VALUE_INT = 3;
    static final int VALUE_STEP = 4;
    static final int VALUE_TRUE = 10;
    static final int VALUE_FALSE = 11;
    static final int VALUE_ON = 20;
    static final int VALUE_OFF = 21;
    static final int VALUE_ACTIVE = 30;
    static final int VALUE_INACTIVE = 31;

    static final int IS_TRUE_FALSE = 1;
    static final int IS_ON_OFF = 2;
    static final int IS_SENSOR_STATE = 3;

    public enum ValueType {
        VAL_FLOAT(VALUE_FLOAT, "NumData"),
        VAL_NOOP(VALUE_NOOP, "Mark"),
        VAL_INT(VALUE_INT, "NumData"),
        VAL_STEP(VALUE_STEP, "speedstep"),
        VAL_TRUE(VALUE_TRUE, "StateTrue"),
        VAL_FALSE(VALUE_FALSE, "StateFalse"),
        VAL_ON(VALUE_ON, "StateOn"),
        VAL_OFF(VALUE_OFF, "StateOff"),
        VAL_ACTIVE(VALUE_ACTIVE, "SensorStateActive"),
        VAL_INACTIVE(VALUE_INACTIVE, "SensorStateInactive");

        int _valueId;  // state id
        String _bundleKey; // key to get state display name

        ValueType(int id, String bundleName) {
            _valueId = id;
            _bundleKey = bundleName;
        }
 
        public int getIntId() {
            return _valueId;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    public static class CommandValue {
        ValueType _type;
        SpeedStepMode  _stepMode;
        float   _floatValue;
        NumberFormat formatter = NumberFormat.getNumberInstance(); 
        NumberFormat intFormatter = NumberFormat.getIntegerInstance(); 

        public CommandValue(ValueType t, SpeedStepMode s, float f) {
            _type = t;
            _stepMode = s;
            _floatValue = f;
        }

        @Override
        protected CommandValue clone() {
            return new CommandValue(_type, _stepMode, _floatValue);
        }

        public ValueType getType() {
            return _type;
        }

        public SpeedStepMode getMode() {
            return _stepMode;
        }

        void setFloat(float f) {
            _floatValue = f;
        }

        public float getFloat() {
            return _floatValue;
        }

        public String showValue() {
            if (_type == ValueType.VAL_FLOAT) {
                return formatter.format(_floatValue);                               
            } else if (_type == ValueType.VAL_INT) {
                return intFormatter.format(_floatValue);
            } else if (_type == ValueType.VAL_STEP) {
                return _stepMode.name;
            } else {
                return _type.toString();
            }
        }

        @Override
        public String toString() {
            return "CommandValue type "+_type.getIntId();
        }
    }

    public static Command getCommandTypeFromInt(int typeInt) {
        for (Command type : Command.values()) {
            if (type.getIntId() == typeInt) {
                return type;
            }
        }
        throw new IllegalArgumentException(typeInt + " Command type ID is unknown");
    }

    public static ValueType getValueTypeFromInt(int typeInt) {
        for (ValueType type : ValueType.values()) {
            if (type.getIntId() == typeInt) {
                return type;
            }
        }
        throw new IllegalArgumentException(typeInt + " ValueType ID is unknown");
    }

    //====================== THrottleSteeing Class ====================
    private long    _time;
    private Command _command;
    private int     _keyNum; // function key number
    private CommandValue _value;
    // _namedHandle may be of 3 different types
    private NamedBeanHandle<? extends NamedBean> _namedHandle = null;
    private float _trackSpeed; // track speed of the train (millimeters per second)

    public ThrottleSetting() {
        _keyNum = -1;
    }

    public ThrottleSetting(long time, Command command, int key, ValueType vType, SpeedStepMode ss, float f, String beanName) {
        _time = time;
        _command = command;
        _keyNum = key;
        setValue(vType, ss, f);
        setNamedBean(command, beanName);
        _trackSpeed = 0.0f;
    }

    public ThrottleSetting(long time, Command command, int key, ValueType vType, SpeedStepMode ss, float f, String beanName, float trkSpd) {
        _time = time;
        _command = command;
        _keyNum = key;
        setValue(vType, ss, f);
        setNamedBean(command, beanName);
        _trackSpeed = trkSpd;
    }

    // pre 4.21.3
    public ThrottleSetting(long time, String cmdStr, String value, String beanName) {
        _time = time;
        setCommand(cmdStr);
        setValue(value);    // must follow setCommand() 
        setNamedBean(_command, beanName);
        _trackSpeed = 0.0f;
    }

    // pre 4.21.3
    public ThrottleSetting(long time, String cmdStr, String value, String beanName, float trkSpd) {
        _time = time;
        setCommand(cmdStr);
        setValue(value);
        setNamedBean(_command, beanName);
        _trackSpeed = trkSpd;
    }

    public ThrottleSetting(long time, Command command, int key, String value, String beanName, float trkSpd) {
        _time = time;
        _command = command;
        _keyNum = key;
        setValue(value);    // must follow setCommand() 
        _namedHandle = null;
        _trackSpeed = trkSpd;
    }

    public ThrottleSetting(ThrottleSetting ts) {
        _time = ts.getTime();
        _command = ts.getCommand();
        _keyNum = ts.getKeyNum();
        setValue(ts.getValue());
        _namedHandle = ts.getNamedBeanHandle();
        _trackSpeed = ts.getTrackSpeed();
    }

    /**
     * Convert old format. (former Strings for Command enums)
     * @param cmdStr old style description string
     * @return enum Command
     * @throws JmriException in case of a non-integer Function or Fn lock/latch value
     */
    private Command getCommandFromString(String cmdStr) throws JmriException {
        Command command;
        String cmd = cmdStr.trim().toUpperCase();
        if ("SPEED".equals(cmd) || Bundle.getMessage("speed").toUpperCase().equals(cmd)) {
            command = Command.SPEED;
            _keyNum = -1;
        } else if ("SPEEDSTEP".equals(cmd) || Bundle.getMessage("speedstep").toUpperCase().equals(cmd)) {
            command = Command.SPEEDSTEP;
            _keyNum = -1;
        } else if ("FORWARD".equals(cmd) || Bundle.getMessage("forward").toUpperCase().equals(cmd)) {
            command = Command.FORWARD;
            _keyNum = -1;
        } else if (cmd.startsWith("F") || Bundle.getMessage("setFunction").toUpperCase().equals(cmd)) {
            command = Command.FKEY;
            try {
                _keyNum = Integer.parseInt(cmd.substring(1));
            } catch (NumberFormatException nfe) {
                throw new JmriException(Bundle.getMessage("badFunctionNum"), nfe);
            }
        } else if (cmd.startsWith("LOCKF") || Bundle.getMessage("setKeyMomentary").toUpperCase().equals(cmd)) {
            command = Command.LATCHF;
            try {
                _keyNum = Integer.parseInt(cmd.substring(5));
            } catch (NumberFormatException nfe) {
                throw new JmriException(Bundle.getMessage("badLockFNum"), nfe);
            }
        } else if ("NOOP".equals(cmd) || Bundle.getMessage("NoOp").toUpperCase().equals(cmd)) {
            command = Command.NOOP;
            _keyNum = -1;
        } else if ("SENSOR".equals(cmd) || "SET SENSOR".equals(cmd) || "SET".equals(cmd) 
                || Bundle.getMessage("SetSensor").toUpperCase().equals(cmd)) {
            command = Command.SET_SENSOR;
            _keyNum = -1;
        } else if ("WAIT SENSOR".equals(cmd) || "WAIT".equals(cmd) 
                || Bundle.getMessage("WaitSensor").toUpperCase().equals(cmd)) {
            command = Command.WAIT_SENSOR;
            _keyNum = -1;
        } else if ("RUN WARRANT".equals(cmd) || Bundle.getMessage("runWarrant").toUpperCase().equals(cmd)) {
            command = Command.RUN_WARRANT;
            _keyNum = -1;
        } else {
            throw new jmri.JmriException(Bundle.getMessage("badCommand", cmdStr));
        }
        return command;
    }

    static protected CommandValue getValueFromString(Command command, String valueStr) throws JmriException {
        if (command == null) {
            throw new jmri.JmriException(Bundle.getMessage("badCommand", "Command missing "+valueStr));
        }
        ValueType type;
        SpeedStepMode mode = SpeedStepMode.UNKNOWN;
        float speed = 0.0f;
        String val = valueStr.trim().toUpperCase();
        if ("ON".equals(val) || Bundle.getMessage("StateOn").toUpperCase().equals(val)) {
            switch (command) {
                case FKEY:
                case LATCHF:
                type = ValueType.VAL_ON;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else if ("OFF".equals(val) || Bundle.getMessage("StateOff").toUpperCase().equals(val)) {
            switch (command) {
                case FKEY:
                case LATCHF:
                type = ValueType.VAL_OFF;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else  if ("TRUE".equals(val) || Bundle.getMessage("StateTrue").toUpperCase().equals(val)) {
            switch (command) {
                case FORWARD:
                    type = ValueType.VAL_TRUE;
                    break;
                case FKEY:
                case LATCHF:
                    type = ValueType.VAL_ON;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else if ("FALSE".equals(val) || Bundle.getMessage("StateFalse").toUpperCase().equals(val)) {
            switch (command) {
                case FORWARD:
                    type = ValueType.VAL_FALSE;
                    break;
                case FKEY:
                case LATCHF:
                    type = ValueType.VAL_OFF;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else if ("ACTIVE".equals(val) || Bundle.getMessage("SensorStateActive").toUpperCase().equals(val)) {
            switch (command) {
                case SET_SENSOR:
                case WAIT_SENSOR:
                    type = ValueType.VAL_ACTIVE;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else if ("INACTIVE".equals(val) || Bundle.getMessage("SensorStateInactive").toUpperCase().equals(val)) {
            switch (command) {
                case SET_SENSOR:
                case WAIT_SENSOR:
                    type = ValueType.VAL_INACTIVE;
                    break;
                default:
                    throw new jmri.JmriException(Bundle.getMessage("badValue", valueStr, command));
            }
        } else {
            try {
                switch (command) {
                    case SPEED:
                        speed = Float.parseFloat(valueStr.replace(',', '.'));
                        type = ValueType.VAL_FLOAT;
                        break;
                    case NOOP:
                        type = ValueType.VAL_NOOP;
                        break;
                    case RUN_WARRANT:
                        speed = Float.parseFloat(valueStr.replace(',', '.'));
                        type = ValueType.VAL_INT;
                        break;
                    case SPEEDSTEP:
                        mode = SpeedStepMode.getByName(val);
                        type = ValueType.VAL_STEP;
                        break;
                    default:
                        throw new JmriException(Bundle.getMessage("badValue", valueStr, command));
                }
            } catch (IllegalArgumentException | NullPointerException ex) { // NumberFormatException is sublass of iae
                throw new JmriException(Bundle.getMessage("badValue", valueStr, command), ex);
            }
        }
        return new CommandValue(type, mode, speed);
    }

    /**
     * Time is an object so that a "synch to block entry" notation can be used
     * rather than elapsed time.
     *
     * @param time the time in some unit
     */
    public void setTime(long time) {
        _time = time;
    }

    public long getTime() {
        return _time;
    }

    public void setCommand(String cmdStr) {
        try {
            _command = getCommandFromString(cmdStr);
        } catch (JmriException je) {
            log.error("Cannot set command from string \"{}\" {}", cmdStr, je.toString());
        }
    }

    public void setCommand(Command command) {
        _command = command;
    }

    public Command getCommand() {
        return _command;
    }

    public void setKeyNum(int key) {
        _keyNum = key;
    }

    public int getKeyNum() {
        return _keyNum;
    }

    public void setValue(String valueStr) {
        try {
            _value = getValueFromString(_command, valueStr);
        } catch (JmriException je) {
            log.error("Cannot set value for command {}. {}",
                   (_command!=null?_command.toString():"null"), je.toString());
        }
    }

    public void setValue(CommandValue value) {
        _value = value.clone();
    }

    public void setValue(ValueType t, SpeedStepMode s, float f) {
        _value = new CommandValue(t, s, f);
    }

    public CommandValue getValue() {
        return _value;
    }

    public void setTrackSpeed(float s) {
        _trackSpeed = s;
    }

    public float getTrackSpeed() {
        return _trackSpeed;
    }

    // _namedHandle may be of 3 different types
    public String setNamedBean(Command cmd, String name) {
        if (log.isDebugEnabled()) {
            log.debug("setNamedBean({}, {})", cmd, name);
        }
        String msg = WarrantFrame.checkBeanName(cmd, name);
        if (msg != null) {
            _namedHandle = null;
            return msg;
        }
        try {
            if (cmd.equals(Command.SET_SENSOR) || cmd.equals(Command.WAIT_SENSOR)) {
                Sensor s = InstanceManager.sensorManagerInstance().provideSensor(name);
                _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, s);
            } else if (cmd.equals(Command.RUN_WARRANT)) {
                Warrant w = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).provideWarrant(name);
                _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, w);
            } else {
                OBlock b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(name);
                if (b != null) {
                    _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, b);
                }
            }
        } catch (IllegalArgumentException iae) {
            return Bundle.getMessage("badCommand", cmd+iae.toString());
        }
        return null;
    }

    // _namedHandle may be of 3 different types
    public <T extends NamedBean> void setNamedBeanHandle(NamedBeanHandle<T> bh) {
        _namedHandle = bh;
    }

    // _namedHandle may be of 3 different types
    public NamedBeanHandle<? extends NamedBean> getNamedBeanHandle() {
        return _namedHandle;
    }

    public NamedBean getBean() {
        if (_namedHandle == null) {
            return null;
        }
        return _namedHandle.getBean();
    }

    public String getBeanDisplayName() {
        if (_namedHandle == null) {
            return null;
        }
        return _namedHandle.getBean().getDisplayName();
    }

    public String getBeanSystemName() {
        if (_namedHandle == null) {
            return null;
        }
        return _namedHandle.getBean().getSystemName();
    }

    @Override
    public String toString() {
        return "ThrottleSetting: wait " + _time + "ms then " + _command.toString()
                + " with value " + _value.showValue() + " for bean \"" + getBeanDisplayName()
                + "\" at trackSpeed " + getTrackSpeed() + "\"";
    }

    private static final Logger log = LoggerFactory.getLogger(ThrottleSetting.class);
}

package jmri;

import jmri.jmrit.Sound;
import jmri.Timebase;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.Timer;
/**
 * ConditionalAction.java
 *
 * The consequent of the antecedent of the conditional proposition. 
 * The data for the action to be taken when a Conditional calulates to True
 * <P>
 * 
 * @author Pete Cressman Copyright (C) 2009
 * @version ???
 */


public class ConditionalAction {

	private int _option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
	private int _type = Conditional.ACTION_NONE ;
	private String _deviceName = " ";
	private int _actionData = 0;
	private String _actionString = "";


    private Timer _timer = null;
    private ActionListener _listener = null;
    private boolean _timerActive = false; 
    private Sound _sound = null;

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    public ConditionalAction() {
    }

    public ConditionalAction(int option, int type, String name, int actionData, String actionStr) {
        _option = option;
        _type = type;
        _deviceName = name;
        _actionData = actionData;
        _actionString = actionStr;
    }

	/**
	 * The consequent device or element type
	 */
    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    /**
    * Sets type from user's name for it
    */
    public void setType(String type) {
        _type = stringToActionType(type);
    }

	/**
	 * Name of the device or element that is effected
	 */
    public String getDeviceName() {
        return _deviceName;
    }

    public void setDeviceName(String deviceName) {
        _deviceName = deviceName;
    }

	/**
	 * Options on when action is taken
	 */
    public int getOption() {
        return _option;
    }

    public void setOption(int option) {
        _option = option;
    }

    /**
    * Sets option from user's name for it
    */
    public void setOption(String option) {
        _option = stringToActionOption(option);
    }

	/**
	 * Integer data for action
	 */
    public int getActionData() {
        return _actionData;
    }

    public void setActionData(int actionData) {
        _actionData = actionData;
    }

    /**
    * Sets action data from user's name for it
    */
    public void setActionData(String actionData) {
        _actionData = stringToActionData(actionData);
    }

	/**
	 * String data for action
	 */
    public String getActionString() {
        return _actionString;
    }

    public void setActionString(String actionString) {
        _actionString = actionString;
    }

    /*
    * get timer for delays and other timed events
    */
    protected Timer getTimer() {
        return _timer;
    }

    /*
    * set timer for delays and other timed events
    */
    protected void setTimer(Timer timer) {
        _timer = timer;
    }

    protected boolean isTimerActive() {
        return _timerActive;
    }

    protected void startTimer() {
        if (_timer != null)
        {
            _timer.start();
            _timerActive = true;
        }
        else {
            log.error("timer is null for "+_deviceName+" of type "+getTypeString());
        }
    }

    protected void stopTimer() {
        if (_timer != null)
        {
            _timer.stop();
            _timerActive = false;
        }
    }

    /*
    * set listener for delays and other timed events
    */
    protected ActionListener getListener() {
        return _listener;
    }

    /*
    * set listener for delays and other timed events
    */
    protected void setListener(ActionListener listener) {
        _listener = listener;
    }

    /**
    * get Sound file
    */
    protected Sound getSound() {
        return _sound;
    }

    /**
    * set Sound file
    */
    protected void setSound(Sound sound) {
        _sound = sound;
    }

    /**** Methods that return user interface strings *****/

	/**
	 * return String name of this consequent type
	 */
	public String getTypeString() {
        return getTypeString(_type);
    }

	/**
	 * return String name of the option for this consequent type
	 */
	public String getOptionString() {
        return getOptionString(_option);
    }

    public String getActionDataString() {
        return getActionDataString(_type, _actionData);
    }

	/**
	 * Convert Consequent Type to Text String
	 */
	public static String getTypeString(int t) {
		switch (t) {
    		case Conditional.ACTION_NONE:
    			return (rbx.getString("ActionNone"));
    		case Conditional.ACTION_SET_TURNOUT:
    			return (rbx.getString("ActionSetTurnout"));
    		case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
    			return (rbx.getString("ActionSetSignal"));
    		case Conditional.ACTION_SET_SIGNAL_HELD:
    			return (rbx.getString("ActionSetSignalHeld"));
    		case Conditional.ACTION_CLEAR_SIGNAL_HELD:
    			return (rbx.getString("ActionClearSignalHeld"));
    		case Conditional.ACTION_SET_SIGNAL_DARK:
    			return (rbx.getString("ActionSetSignalDark"));
    		case Conditional.ACTION_SET_SIGNAL_LIT:
    			return (rbx.getString("ActionSetSignalLit"));
    		case Conditional.ACTION_TRIGGER_ROUTE:
    			return (rbx.getString("ActionTriggerRoute"));
    		case Conditional.ACTION_SET_SENSOR:
    			return (rbx.getString("ActionSetSensor"));
    		case Conditional.ACTION_DELAYED_SENSOR:
    			return (rbx.getString("ActionDelayedSensor"));
    		case Conditional.ACTION_SET_LIGHT:
    			return (rbx.getString("ActionSetLight"));
    		case Conditional.ACTION_SET_MEMORY:
    			return (rbx.getString("ActionSetMemory"));
    		case Conditional.ACTION_ENABLE_LOGIX:
    			return (rbx.getString("ActionEnableLogix"));
    		case Conditional.ACTION_DISABLE_LOGIX:
    			return (rbx.getString("ActionDisableLogix"));
    		case Conditional.ACTION_PLAY_SOUND:
    			return (rbx.getString("ActionPlaySound"));
    		case Conditional.ACTION_RUN_SCRIPT:
    			return (rbx.getString("ActionRunScript"));
    		case Conditional.ACTION_DELAYED_TURNOUT:
    			return (rbx.getString("ActionDelayedTurnout"));
    		case Conditional.ACTION_LOCK_TURNOUT:
    			return (rbx.getString("ActionTurnoutLock"));
    		case Conditional.ACTION_RESET_DELAYED_SENSOR:
    			return (rbx.getString("ActionResetDelayedSensor"));
    		case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
    			return (rbx.getString("ActionCancelSensorTimers"));
    		case Conditional.ACTION_RESET_DELAYED_TURNOUT:
    			return (rbx.getString("ActionResetDelayedTurnout"));
    		case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
    			return (rbx.getString("ActionCancelTurnoutTimers"));
    		case Conditional.ACTION_SET_FAST_CLOCK_TIME:
    			return (rbx.getString("ActionSetFastClockTime"));
    		case Conditional.ACTION_START_FAST_CLOCK:
    			return (rbx.getString("ActionStartFastClock"));
    		case Conditional.ACTION_STOP_FAST_CLOCK:
    			return (rbx.getString("ActionStopFastClock"));			
    		case Conditional.ACTION_COPY_MEMORY:
    			return (rbx.getString("ActionCopyMemory"));
    		case Conditional.ACTION_SET_LIGHT_INTENSITY:
    			return (rbx.getString("ActionSetLightIntensity"));
    		case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
    			return (rbx.getString("ActionSetLightTransitionTime"));
		}
        log.warn("Unexpected parameter to getTypeString("+t+")");
		return ("");
	}

	/**
	 * Convert consequent option to String
	 */
	public static String getOptionString(int opt) {
		switch (opt) {
            case Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE:
                return (rbx.getString("OnChangeToTrue"));
            case Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE:
                return (rbx.getString("OnChangeToFalse"));
            case Conditional.ACTION_OPTION_ON_CHANGE:
                return (rbx.getString("OnChange"));
        }
        log.warn("Unexpected parameter to getOptionString("+opt+")");
        return "";
    }

	/**
	 * Identifies action Type from Text String Note: if string does not
	 * correspond to an action type as defined in
	 * ConditionalAction, returns 0.
	 */
	public static int stringToActionType(String str) {
        if (str != null)
        {
            for (int i = 1; i <= Conditional.NUM_ACTION_TYPES; i++) {
                if (str.equals(getTypeString(i))) {
                    return (i);
                }
            }
        }
        log.warn("Unexpected parameter to stringToActionType("+str+")");
		return 0;
	}
	
	/**
	 * Identifies action Option from Text String Note: if string does not
	 * correspond to an action Option as defined in
	 * ConditionalAction, returns 0.
	 */
	public static int stringToActionOption(String str) {
		for (int i = 1; i <= Conditional.NUM_ACTION_OPTIONS; i++) {
			if (str.equals(getOptionString(i))) {
				return (i);
			}
		}
        log.warn("Unexpected parameter to stringToActionOption("+str+")");
		return 0;
	}
	
	/**
	 * Identifies action Data from Text String Note: if string does not
	 * correspond to an action Data as defined in
	 * ConditionalAction, returns -1.
	 */
	public static int stringToActionData(String str) {
		if (str.equals(rbx.getString("TurnoutClosed"))) {
				return Turnout.CLOSED;
	    }
        else if (str.equals(rbx.getString("TurnoutThrown"))) {
            return Turnout.THROWN;
        }
        else if (str.equals(rbx.getString("SensorActive")))
        {
            return Sensor.ACTIVE;
        }
        else if (str.equals(rbx.getString("SensorInactive"))) {
            return Sensor.INACTIVE;
        }
        else if (str.equals(rbx.getString("LightOn"))) {
            return Light.ON;
        }
        else if (str.equals(rbx.getString("LightOff"))) {
            return Light.OFF;
        }
        else if (str.equals(rbx.getString("TurnoutUnlock"))) {
            return Turnout.UNLOCKED;
        }
        else if (str.equals(rbx.getString("TurnoutLock"))) {
            return Turnout.LOCKED;
        }
        else if (str.equals(rbx.getString("AppearanceRed"))) {
            return SignalHead.RED;
        }
        else if (str.equals(rbx.getString("AppearanceYellow"))) {
            return SignalHead.YELLOW;
        }
        else if (str.equals(rbx.getString("AppearanceGreen"))) {
            return SignalHead.GREEN;
        }
        else if (str.equals(rbx.getString("AppearanceDark"))) {
            return SignalHead.DARK;
        }
        else if (str.equals(rbx.getString("AppearanceFlashRed"))) {
            return SignalHead.FLASHRED;
        }
        else if (str.equals(rbx.getString("AppearanceFlashYellow"))) {
            return SignalHead.FLASHYELLOW;
        }
        else if (str.equals(rbx.getString("AppearanceFlashGreen"))) {
            return SignalHead.FLASHGREEN;
        }
        // empty strings can occur frequently with types that have no integer data
        if (str != null && str.length() > 0)
        {
            log.warn("Unexpected parameter to stringToActionData("+str+")");
        }
		return -1;
	}
	
	public static String getActionDataString(int t, int data) {
		switch (t) {
    		case Conditional.ACTION_SET_TURNOUT:
    		case Conditional.ACTION_DELAYED_TURNOUT:
                if (data == Turnout.CLOSED) {
                    return (rbx.getString("TurnoutClosed"));
                } else if (data == Turnout.THROWN) {
                    return (rbx.getString("TurnoutThrown"));
                } else
                    return (rbx.getString("Toggle"));
            case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                return DefaultSignalHead.getAppearanceString(data);
    		case Conditional.ACTION_SET_SENSOR:
    		case Conditional.ACTION_DELAYED_SENSOR:
    		case Conditional.ACTION_RESET_DELAYED_SENSOR:
                if (data == Sensor.ACTIVE) {
                    return (rbx.getString("SensorActive"));
                } else if (data == Sensor.INACTIVE) {
                    return (rbx.getString("SensorInactive"));
                } else
                    return (rbx.getString("Toggle"));
    		case Conditional.ACTION_SET_LIGHT:
                if (data == Light.ON) {
                    return (rbx.getString("LightOn"));
                } else if (data == Light.OFF) {
                    return (rbx.getString("LightOff"));
                } else
                    return (rbx.getString("Toggle"));
    		case Conditional.ACTION_LOCK_TURNOUT:
                if (data == Turnout.UNLOCKED) {
                    return (rbx.getString("TurnoutUnlock"));
                } else if (data == Turnout.LOCKED) {
                    return (rbx.getString("TurnoutLock"));
                } else
                    return (rbx.getString("Toggle"));
		}
//        log.warn("Unexpected parameters to getActionDataString("+t+", "+data+
//                  ")  type= "+getTypeString(t));
        return "";
    }
	static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(ConditionalAction.class.getName());
}

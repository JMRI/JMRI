package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.jmrit.Sound;
import jmri.jmrit.beantable.LogixTableAction;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logix.OBlockManager;

import java.awt.event.ActionListener;
import javax.swing.Timer;
/**
 * ConditionalAction.java
 *
 * The consequent of the antecedent of the conditional proposition. 
 * The data for the action to be taken when a Conditional calculates to True
 * <P>
 * This is in the implementations package because of a Swing dependence
 * via the times.  Java 1.5 or Java 1.6 might make it possible to
 * break that, which will simplify things.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (c) 2009
 * @version $Revision$
 */


public class DefaultConditionalAction implements ConditionalAction {

	private int _option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
	private int _type = Conditional.ACTION_NONE ;
	private String _deviceName = " ";
	private int _actionData = 0;
	private String _actionString = "";
    private NamedBeanHandle<?> _namedBean = null;

    private Timer _timer = null;
    private ActionListener _listener = null;
    private boolean _timerActive = false;
    private boolean _indirectAction = false;
    private Sound _sound = null;

	static final java.util.ResourceBundle rbx = java.util.ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    
    public DefaultConditionalAction() {
    }

    public DefaultConditionalAction(int option, int type, String name, int actionData, String actionStr) {
        _option = option;
        _type = type;
        _deviceName = name;
        _actionData = actionData;
        _actionString = actionStr;
        
        NamedBean bean = getIndirectBean(_deviceName);
        if (bean==null) {
            bean = getActionBean(_deviceName);
        }
        if (bean!=null){
            _namedBean = nbhm.getNamedBeanHandle(_deviceName, bean);
        } else {
            _namedBean = null;
        }
    }
    /**
     * If this is an indirect reference return the Memory bean
     * @param devName
     * @return
     */
    private Memory getIndirectBean(String devName) {
        if (devName!=null && devName.length()>0 && devName.charAt(0)== '@') {
            String memName = devName.substring(1);
            Memory m = InstanceManager.memoryManagerInstance().getMemory(memName);
            if (m != null) {
            	_indirectAction = true;
                return m;
            }
            log.error("\""+devName+"\" invalid indirect memory name in action "+_actionString+" of type "+_type);
        } else {
        	_indirectAction = false;        	
        }
        return null;
    }
    /**
     * Return the device bean that will do the action
     * @param devName
     * @return
     */
    private NamedBean getActionBean(String devName) {
    	NamedBean bean = null;
    	try {
    		switch (Conditional.ACTION_TO_ITEM[_type]) {
    	        case Conditional.ITEM_TYPE_SENSOR:
    	            bean = InstanceManager.sensorManagerInstance().provideSensor(devName);
    	            if (bean == null) {
    	                log.error("invalid sensor name= \""+_deviceName+"\" in conditional action");
    	             }
    	            break;
    	        case Conditional.ITEM_TYPE_TURNOUT:
    	            bean = InstanceManager.turnoutManagerInstance().provideTurnout(devName);
    	            if (bean == null) {
    	                log.error("invalid turnout name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_MEMORY:
    	            bean = InstanceManager.memoryManagerInstance().provideMemory(devName);
    	            if (bean == null) {
    	                log.error("invalid memory name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_LIGHT:
    	            bean = InstanceManager.lightManagerInstance().getLight(devName);
    	            if (bean == null) {
    	                log.error("invalid light name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_SIGNALMAST:
    	            bean = InstanceManager.signalMastManagerInstance().provideSignalMast(devName);
    	            if (bean == null) {
    	                log.error("invalid signal mast name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_SIGNALHEAD:
    	            bean = InstanceManager.signalHeadManagerInstance().getSignalHead(devName);
    	            if (bean == null) {
    	                log.error("invalid signal head name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_WARRANT:
    	            bean = InstanceManager.getDefault(WarrantManager.class).getWarrant(devName);
    	            if (bean == null) {
    	                log.error("invalid Warrant name= \""+_deviceName+"\" in conditional action");
    	            }
    	            break;
    	        case Conditional.ITEM_TYPE_OBLOCK:
    	            bean = InstanceManager.getDefault(OBlockManager.class).getOBlock(devName);
    	            if (bean == null) {
    	                log.error("invalid OBlock name= \""+_deviceName+"\" in conditional action");
    	             }
    		}
        } catch (java.lang.NumberFormatException ex){
        	//Can be considered normal if the logixs are loaded prior to any other beans
        }
        return bean;	
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
        if(_namedBean!=null) {
            return _namedBean.getName();
        }
        /* As we have a trigger for something using the action, then hopefully
        all the managers have been loaded and we can get the bean, which prevented
        the bean from being loaded in the first place */
        setDeviceName(_deviceName);
        return _deviceName;
    }

    public void setDeviceName(String deviceName) {
        _deviceName = deviceName;
        NamedBean bean = getIndirectBean(_deviceName);
        if (bean==null) {
            bean = getActionBean(_deviceName);
        }
        if (bean!=null){
            _namedBean = nbhm.getNamedBeanHandle(_deviceName, bean);
        } else {
            _namedBean = null;
        }
    }
    
    public NamedBeanHandle<?> getNamedBean(){
    	if (_indirectAction) {
    		Memory m = (Memory)(_namedBean.getBean());
    		String actionName = (String)m.getValue();
    		NamedBean bean = getActionBean(actionName);
            if (bean!=null){
                return nbhm.getNamedBeanHandle(actionName, bean);
            } else {
                return null;
            }
    	}
        return _namedBean;
    }
    
    public NamedBean getBean(){
        if (_namedBean!=null){
            return (NamedBean) getNamedBean().getBean();
        } 
        setDeviceName(_deviceName); //ReApply name as that will create namedBean, save replicating it here
        if(_namedBean!=null)
            return (NamedBean) getNamedBean().getBean();
        return null;
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
        if (_actionString==null) {
            _actionString = getTypeString();
        }
        return _actionString;
    }

    public void setActionString(String actionString) {
        _actionString = actionString;
    }

    /*
    * get timer for delays and other timed events
    */
    public Timer getTimer() {
        return _timer;
    }

    /*
    * set timer for delays and other timed events
    */
    public void setTimer(Timer timer) {
        _timer = timer;
    }

    public boolean isTimerActive() {
        return _timerActive;
    }

    public void startTimer() {
        if (_timer != null)
        {
            _timer.start();
            _timerActive = true;
        }
        else {
            log.error("timer is null for "+_deviceName+" of type "+getTypeString());
        }
    }

    public void stopTimer() {
        if (_timer != null)
        {
            _timer.stop();
            _timerActive = false;
        }
    }

    /*
    * set listener for delays and other timed events
    */
    public ActionListener getListener() {
        return _listener;
    }

    /*
    * set listener for delays and other timed events
    */
    public void setListener(ActionListener listener) {
        _listener = listener;
    }

    /**
    * get Sound file
    */
    public Sound getSound() {
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
        return getActionTypeString(_type);
    }

	/**
	 * return String name of the option for this consequent type
	 */
	public String getOptionString(boolean type) {
        return getOptionString(_option, type);
    }

    public String getActionDataString() {
        return getActionDataString(_type, _actionData);
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
            case Conditional.ITEM_TYPE_LOGIX:
                return (rbx.getString("Logix"));
            case Conditional.ITEM_TYPE_WARRANT:
                return (rbx.getString("Warrant"));
            case Conditional.ITEM_TYPE_OBLOCK:
                return (rbx.getString("OBlock"));
            case Conditional.ITEM_TYPE_CLOCK:
                return (rbx.getString("FastClock"));
            case Conditional.ITEM_TYPE_AUDIO:
                return (rbx.getString("Audio"));
            case Conditional.ITEM_TYPE_SCRIPT:
                return (rbx.getString("Script"));
            case Conditional.ITEM_TYPE_OTHER:
                return (rbx.getString("Other"));
        }
        return "";
    }


	/**
	 * Convert Consequent Type to Text String
	 */
	public static String getActionTypeString(int t) {
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
            case Conditional.ACTION_CONTROL_AUDIO:
                return (rbx.getString("ActionControlAudio"));
            case Conditional.ACTION_JYTHON_COMMAND:
            	return (rbx.getString("ActionJythonCommand"));
    		case Conditional.ACTION_ALLOCATE_WARRANT_ROUTE:
    			return (rbx.getString("ActionAllocateWarrant"));
    		case Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE:
    			return (rbx.getString("ActionDeallocateWarrant"));
    		case Conditional.ACTION_SET_ROUTE_TURNOUTS:
    			return (rbx.getString("ActionSetWarrantTurnouts"));
    		case Conditional.ACTION_AUTO_RUN_WARRANT:
    			return (rbx.getString("ActionAutoRunWarrant"));
    		case Conditional.ACTION_MANUAL_RUN_WARRANT:
    			return (rbx.getString("ActionManualRunWarrant"));
    		case Conditional.ACTION_CONTROL_TRAIN:
    			return (rbx.getString("ActionControlTrain"));
            case Conditional.ACTION_SET_TRAIN_ID:
               return (rbx.getString("ActionSetTrainId"));
            case Conditional.ACTION_SET_TRAIN_NAME:
               return (rbx.getString("ActionSetTrainName"));
            case Conditional.ACTION_SET_SIGNALMAST_ASPECT:
               return (rbx.getString("ActionSetSignalMastAspect"));                
            case Conditional.ACTION_THROTTLE_FACTOR:
               return (rbx.getString("ActionSetThrottleFactor"));                
            case Conditional.ACTION_SET_SIGNALMAST_HELD:
               return (rbx.getString("ActionSetSignalMastHeld"));                
            case Conditional.ACTION_CLEAR_SIGNALMAST_HELD:
               return (rbx.getString("ActionClearSignalMastHeld"));                
            case Conditional.ACTION_SET_SIGNALMAST_DARK:
               return (rbx.getString("ActionSetSignalMastDark"));                
            case Conditional.ACTION_SET_SIGNALMAST_LIT:
               return (rbx.getString("ActionClearSignalMastDark"));                
            case Conditional.ACTION_SET_BLOCK_VALUE:
                return (rbx.getString("ActionSetBlockValue"));                
            case Conditional.ACTION_SET_BLOCK_ERROR:
               return (rbx.getString("ActionSetBlockError"));                
            case Conditional.ACTION_CLEAR_BLOCK_ERROR:
               return (rbx.getString("ActionClearBlockError"));
            case Conditional.ACTION_DEALLOCATE_BLOCK:
               return (rbx.getString("ActionDeallocateBlock"));                
            case Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE:
               return (rbx.getString("ActionSetBlockOutOfService"));                
            case Conditional.ACTION_SET_BLOCK_IN_SERVICE:
                return (rbx.getString("ActionBlockInService"));                
		}
        log.warn("Unexpected parameter to getActionTypeString("+t+")");
		return ("");
	}

	/**
	 * Convert consequent option to String
	 */
	public static String getOptionString(int opt, boolean type) {
		switch (opt) {
            case Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE:
            	if (type){
            		return (rbx.getString("OnChangeToTrue"));
            	} else {
            		return (rbx.getString("OnTriggerToTrue"));
            	}              
            case Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE:
            	if (type){
            		return (rbx.getString("OnChangeToFalse"));
            	} else {
            		return (rbx.getString("OnTriggerToFalse"));
            	}                             
            case Conditional.ACTION_OPTION_ON_CHANGE:
            	if (type){
            		return (rbx.getString("OnChange"));
            	} else {
            		return (rbx.getString("OnTrigger"));
            	}              
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
                if (str.equals(getActionTypeString(i))) {
                    return (i);
                }
            }
        }
        log.warn("Unexpected parameter to stringToActionType("+str+")");
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
        else if (str.equals(Bundle.getMessage("SignalHeadStateRed"))) {
            return SignalHead.RED;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateYellow"))) {
            return SignalHead.YELLOW;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateGreen"))) {
            return SignalHead.GREEN;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateDark"))) {
            return SignalHead.DARK;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingRed"))) {
            return SignalHead.FLASHRED;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingYellow"))) {
            return SignalHead.FLASHYELLOW;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingGreen"))) {
            return SignalHead.FLASHGREEN;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateLunar"))) {
            return SignalHead.LUNAR;
        }
        else if (str.equals(Bundle.getMessage("SignalHeadStateFlashingLunar"))) {
            return SignalHead.FLASHLUNAR;
        }
        else if (str.equals(rbx.getString("AudioSourcePlay"))) {
            return Audio.CMD_PLAY;
        }
        else if (str.equals(rbx.getString("AudioSourceStop"))) {
            return Audio.CMD_STOP;
        }
        else if (str.equals(rbx.getString("AudioSourcePlayToggle"))) {
            return Audio.CMD_PLAY_TOGGLE;
        }
        else if (str.equals(rbx.getString("AudioSourcePause"))) {
            return Audio.CMD_PAUSE;
        }
        else if (str.equals(rbx.getString("AudioSourceResume"))) {
            return Audio.CMD_RESUME;
        }
        else if (str.equals(rbx.getString("AudioSourcePauseToggle"))) {
            return Audio.CMD_PAUSE_TOGGLE;
        }
        else if (str.equals(rbx.getString("AudioSourceRewind"))) {
            return Audio.CMD_REWIND;
        }
        else if (str.equals(rbx.getString("AudioSourceFadeIn"))) {
            return Audio.CMD_FADE_IN;
        }
        else if (str.equals(rbx.getString("AudioSourceFadeOut"))) {
            return Audio.CMD_FADE_OUT;
        }
        else if (str.equals(rbx.getString("AudioResetPosition"))) {
            return Audio.CMD_RESET_POSITION;
        }
        // empty strings can occur frequently with types that have no integer data
        if (str.length() > 0)
        {
            log.warn("Unexpected parameter to stringToActionData("+str+")");
        }
		return -1;
	}
	
	public static String getActionDataString(int t, int data) {
		switch (t) {
    		case Conditional.ACTION_SET_TURNOUT:
    		case Conditional.ACTION_DELAYED_TURNOUT:
    		case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                if (data == Turnout.CLOSED) {
                    return (rbx.getString("TurnoutClosed"));
                } else if (data == Turnout.THROWN) {
                    return (rbx.getString("TurnoutThrown"));
                } else if (data == Route.TOGGLE) {
                    return (rbx.getString("Toggle"));
                }
                break;
            case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                return DefaultSignalHead.getDefaultStateName(data);
    		case Conditional.ACTION_SET_SENSOR:
    		case Conditional.ACTION_DELAYED_SENSOR:
    		case Conditional.ACTION_RESET_DELAYED_SENSOR:
                if (data == Sensor.ACTIVE) {
                    return (rbx.getString("SensorActive"));
                } else if (data == Sensor.INACTIVE) {
                    return (rbx.getString("SensorInactive"));
                } else if (data == Route.TOGGLE) {
                    return (rbx.getString("Toggle"));
                }
                break;
    		case Conditional.ACTION_SET_LIGHT:
                if (data == Light.ON) {
                    return (rbx.getString("LightOn"));
                } else if (data == Light.OFF) {
                    return (rbx.getString("LightOff"));
                }  else if (data == Route.TOGGLE) {
                    return (rbx.getString("Toggle"));
                }
                break;
    		case Conditional.ACTION_LOCK_TURNOUT:
                if (data == Turnout.UNLOCKED) {
                    return (rbx.getString("TurnoutUnlock"));
                } else if (data == Turnout.LOCKED) {
                    return (rbx.getString("TurnoutLock"));
                } else if (data == Route.TOGGLE) {
                    return (rbx.getString("Toggle"));
                }
                break;
            case Conditional.ACTION_CONTROL_AUDIO:
                switch (data) {
                    case Audio.CMD_PLAY:
                        return (rbx.getString("AudioSourcePlay"));
                    case Audio.CMD_STOP:
                        return (rbx.getString("AudioSourceStop"));
                    case Audio.CMD_PLAY_TOGGLE:
                        return (rbx.getString("AudioSourcePlayToggle"));
                    case Audio.CMD_PAUSE:
                        return (rbx.getString("AudioSourcePause"));
                    case Audio.CMD_RESUME:
                        return (rbx.getString("AudioSourceResume"));
                    case Audio.CMD_PAUSE_TOGGLE:
                        return (rbx.getString("AudioSourcePauseToggle"));
                    case Audio.CMD_REWIND:
                        return (rbx.getString("AudioSourceRewind"));
                    case Audio.CMD_FADE_IN:
                        return (rbx.getString("AudioSourceFadeIn"));
                    case Audio.CMD_FADE_OUT:
                        return (rbx.getString("AudioSourceFadeOut"));
                    case Audio.CMD_RESET_POSITION:
                        return (rbx.getString("AudioResetPosition"));
                }
                break;
            case Conditional.ACTION_CONTROL_TRAIN:
                if (data == Warrant.HALT) {
                    return (rbx.getString("WarrantHalt"));
                } else if (data == Warrant.RESUME) {
                    return (rbx.getString("WarrantResume"));
                } else
                    return (rbx.getString("WarrantAbort"));
		}
        return "";
    }

    public String description(boolean triggerType) {
        String str = getOptionString(triggerType)+", "+ getTypeString();
        if (_deviceName.length() > 0) {
            switch (_type) {
                case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
                case Conditional.ACTION_SET_SIGNAL_HELD:
                case Conditional.ACTION_CLEAR_SIGNAL_HELD:
                case Conditional.ACTION_SET_SIGNAL_DARK:
                case Conditional.ACTION_SET_SIGNAL_LIT:
                case Conditional.ACTION_TRIGGER_ROUTE:
                case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
                case Conditional.ACTION_SET_MEMORY:
                case Conditional.ACTION_ENABLE_LOGIX:
                case Conditional.ACTION_DISABLE_LOGIX:
                case Conditional.ACTION_COPY_MEMORY:
                case Conditional.ACTION_SET_LIGHT_INTENSITY:
                case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                case Conditional.ACTION_ALLOCATE_WARRANT_ROUTE:
                case Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE:
                case Conditional.ACTION_SET_SIGNALMAST_HELD:
                case Conditional.ACTION_CLEAR_SIGNALMAST_HELD:
                case Conditional.ACTION_SET_SIGNALMAST_DARK:
                case Conditional.ACTION_SET_SIGNALMAST_LIT:
                case Conditional.ACTION_SET_BLOCK_ERROR:
                case Conditional.ACTION_CLEAR_BLOCK_ERROR:
                case Conditional.ACTION_DEALLOCATE_BLOCK:
                case Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE:
                case Conditional.ACTION_SET_BLOCK_IN_SERVICE:
                    str = str + ", \""+ _deviceName+"\".";
                    break;
                case Conditional.ACTION_SET_ROUTE_TURNOUTS:
                case Conditional.ACTION_AUTO_RUN_WARRANT:
                case Conditional.ACTION_MANUAL_RUN_WARRANT:
                    str = str +" "+rbx.getString("onWarrant")+", \""+ _deviceName+"\".";
                    break;
                case Conditional.ACTION_SET_SENSOR:
                case Conditional.ACTION_SET_TURNOUT:
                case Conditional.ACTION_SET_LIGHT:
                case Conditional.ACTION_LOCK_TURNOUT:
                case Conditional.ACTION_RESET_DELAYED_SENSOR:
                case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                case Conditional.ACTION_DELAYED_TURNOUT:
                case Conditional.ACTION_DELAYED_SENSOR:
                case Conditional.ACTION_CONTROL_AUDIO:
                    str = str + ", \""+ _deviceName +"\" " + rbx.getString("to")
                          + " " + getActionDataString();
                    break;
                case Conditional.ACTION_SET_SIGNALMAST_ASPECT:
                    str = str + ", \""+ _deviceName +"\" " + rbx.getString("to")
                          + " " + _actionString;
                    break;
                case Conditional.ACTION_CONTROL_TRAIN:
                    str = str +" "+rbx.getString("onWarrant")+" \""+ _deviceName +"\" "
                          +rbx.getString("to")+ " " + getActionDataString();
                    break;
            }
        }
        if (_actionString.length() > 0)
        {
            switch (_type)
            {
                case Conditional.ACTION_SET_MEMORY:
                case Conditional.ACTION_COPY_MEMORY:
                    str = str + " " + rbx.getString("to")+ " " + _actionString + ".";
                    break;
                case Conditional.ACTION_PLAY_SOUND:
                case Conditional.ACTION_RUN_SCRIPT:
                    str = str + " " + rbx.getString("FromFile")+ " "+ _actionString+ ".";
                    break;
                case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                case Conditional.ACTION_RESET_DELAYED_SENSOR:
                case Conditional.ACTION_DELAYED_TURNOUT:
                case Conditional.ACTION_DELAYED_SENSOR:
                    str = str + rbx.getString("After") + " ";
                    try {
                        Integer.parseInt(_actionString);
                        str = str + _actionString + " " + rbx.getString("Seconds")+ ".";
                    } catch (NumberFormatException nfe) { 
                        str = str + _actionString + " " + rbx.getString("ValueInMemory")
                             + " " + rbx.getString("Seconds") + ".";
                    }
                    break;
                case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                case Conditional.ACTION_SET_LIGHT_INTENSITY:
                    try {
                        //int t = Integer.parseInt(_actionString);
                        str = str + " " + rbx.getString("to")+ " "+ _actionString + ".";
                    } catch (NumberFormatException nfe) { 
                        str = str + " " + rbx.getString("to") + " " + _actionString + " "
                            + rbx.getString("ValueInMemory") + ".";
                    }
                    break;
                case Conditional.ACTION_JYTHON_COMMAND:
                    str = str + " " + rbx.getString("ExecJythonCmd")+ " "+ _actionString+ ".";
                    break;
                case Conditional.ACTION_SET_TRAIN_ID:
                case Conditional.ACTION_SET_TRAIN_NAME:
                case Conditional.ACTION_THROTTLE_FACTOR:
                    str = str + ", \""+_actionString+"\" "+rbx.getString("onWarrant")+
                        " \""+_deviceName+"\".";
                    break;
                case Conditional.ACTION_SET_BLOCK_VALUE:
                    str = str + ", \""+_actionString+"\" "+rbx.getString("onBlock")+
                    " \""+_deviceName+"\".";
                break;
            }
        }
        switch (_type)
        {
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                str = str + " " + rbx.getString("to")+ " " + _actionData + ".";
                break;
            case Conditional.ACTION_SET_FAST_CLOCK_TIME:
                str = str + " " +rbx.getString("to")+ " " +
                      LogixTableAction.formatTime(_actionData / 60, _actionData - ((_actionData / 60) * 60));
                break;
        }
        return str;
    }

	static final Logger log = LoggerFactory.getLogger(ConditionalAction.class.getName());
}

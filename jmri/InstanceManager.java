// InstanceManager.java

package jmri;

/**
 * Provides static members for locating various interface implementations.
 *<P>
 * Provides implementations for the JMRI interfaces:
 *<UL>
 *<LI>PowerManager
 *</UL>
 *
 * The implementations of these interfaces are specific to the layout hardware, etc.
 * During initialization, objects of the right type are created and registered
 * with the ImplementationManager class, so they can later be retrieved by
 * non-system-specific code.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.16 $
 */
public class InstanceManager {

    static public PowerManager powerManagerInstance()  { return _powerManager; }

    static public ProgrammerManager programmerManagerInstance()  { return _programmerManager; }

    static public SensorManager sensorManagerInstance()  { return _sensorManager; }

    static public TurnoutManager turnoutManagerInstance()  { return _turnoutManager; }

    static public ConfigureManager configureManagerInstance()  { return mConfigureManager; }

    static public ThrottleManager throttleManagerInstance()  { return mThrottleManager; }

    static public SignalHeadManager signalHeadManagerInstance()  {
        if (mSignalHeadManager != null) return mSignalHeadManager;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        mSignalHeadManager = new AbstractSignalHeadManager();
        return mSignalHeadManager;
    }

    static public CommandStation commandStationInstance()  { return mCommandStation; }

    static private PowerManager _powerManager = null;
    static public void setPowerManager(PowerManager p) {
        if (p!=_powerManager && _powerManager!=null && log.isDebugEnabled()) log.debug("PowerManager instance is being replaced: "+p);
        if (p!=_powerManager && _powerManager==null && log.isDebugEnabled()) log.debug("PowerManager instance is being installed: "+p);
        _powerManager = p;
    }

    static private ProgrammerManager _programmerManager = null;
    static public void setProgrammerManager(ProgrammerManager p) {
        if (p!=_programmerManager && _programmerManager!=null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being replaced: "+p);
        if (p!=_programmerManager && _programmerManager==null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being installed: "+p);
        _programmerManager = p;
    }

    static private SensorManager _sensorManager = null;
    static public void setSensorManager(SensorManager p) {
        if (p!=_sensorManager && _sensorManager!=null && log.isDebugEnabled()) log.debug("SensorManager instance is being replaced: "+p);
        if (p!=_sensorManager && _sensorManager==null && log.isDebugEnabled()) log.debug("SensorManager instance is being installed: "+p);
        _sensorManager = p;
    }

    static private TurnoutManager _turnoutManager = null;
    static public void setTurnoutManager(TurnoutManager p) {
        if (p!=_turnoutManager && _turnoutManager!=null && log.isDebugEnabled()) log.debug("TurnoutManager instance is being replaced: "+p);
        if (p!=_turnoutManager && _turnoutManager==null && log.isDebugEnabled()) log.debug("TurnoutManager instance is being installed: "+p);
        _turnoutManager = p;
    }

    static private ConfigureManager mConfigureManager = null;
    static public void setConfigureManager(ConfigureManager p) {
        if (p!=mConfigureManager && mConfigureManager!=null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being replaced: "+p);
        if (p!=mConfigureManager && mConfigureManager==null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being installed: "+p);
        mConfigureManager = p;
    }

    static private ThrottleManager mThrottleManager = null;
    static public void setThrottleManager(ThrottleManager p) {
        if (p!=mThrottleManager && mThrottleManager!=null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being replaced: "+p);
        if (p!=mThrottleManager && mThrottleManager==null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being installed: "+p);
        mThrottleManager = p;
    }

    static private SignalHeadManager mSignalHeadManager = null;
    static public void setSignalHeadManager(SignalHeadManager p) {
        if (p!=mSignalHeadManager && mSignalHeadManager!=null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being replaced: "+p);
        if (p!=mSignalHeadManager && mSignalHeadManager==null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being installed: "+p);
        mSignalHeadManager = p;
    }

    static private CommandStation mCommandStation = null;
    static public void setCommandStation(CommandStation p) {
        if (p!=mCommandStation && mCommandStation!=null && log.isDebugEnabled()) log.debug("CommandStation instance is being replaced: "+p);
        if (p!=mCommandStation && mCommandStation==null && log.isDebugEnabled()) log.debug("CommandStation instance is being installed: "+p);
        mCommandStation = p;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstanceManager.class.getName());
}

/* @(#)InstanceManager.java */

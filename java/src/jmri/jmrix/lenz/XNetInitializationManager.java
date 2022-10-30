package jmri.jmrix.lenz;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.*;
import jmri.jmrix.roco.RocoXNetThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class performs Command Station dependent initialization for XpressNet.
 * It adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003-2010,2020
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class XNetInitializationManager {

    public XNetInitializationManager() {
    }

    private XNetSystemConnectionMemo systemMemo;
    private Class<? extends XNetPowerManager> powerManagerClass;
    private Class<? extends XNetThrottleManager> throttleManagerClass;
    private Class<? extends RocoXNetThrottleManager> rocoThrottleManagerClass;
    private Class<? extends XNetProgrammerManager> programmerManagerClass;
    private Class<? extends XNetProgrammer> programmerClass;
    private Class<? extends XNetConsistManager> consistManagerClass;
    private Class<? extends XNetTurnoutManager> turnoutManagerClass;
    private Class<? extends XNetLightManager> lightManagerClass;
    private Class<? extends XNetSensorManager> sensorManagerClass;
    private boolean versionCheck = false;
    private boolean noCommandStation = false;
    private int initTimeout = 30000;

    /**
     * Set the version check flag to true.
     * @return this initializer
     */
    public XNetInitializationManager versionCheck(){
        versionCheck = true;
        return this;
    }

    /**
     * Set the initialization timeout
     * @param timeout value in ms.
     * @return this initializer.
     */
    public XNetInitializationManager setTimeout(int timeout){
        initTimeout = timeout;
        return this;
    }

    /**
     * Set the memo to initialize
     * @param systemMemo the memo
     * @return this initializer
     */
    public XNetInitializationManager memo(XNetSystemConnectionMemo systemMemo){
        this.systemMemo = systemMemo;
        return this;
    }

    /**
     * Set the defaults to the default classes in jmri.jmrix.lenz.
     * <p>
     * This methods sets the default values for Lenz command stations
     * and the Roco MultiMaus and LokMaus.  Use with {@link #versionCheck}
     * and {@link #setTimeout} to automatically configure these systems.
     * </p>
     * @return this initializer
     */
    public XNetInitializationManager setDefaults(){
        powerManagerClass = XNetPowerManager.class;
        throttleManagerClass = XNetThrottleManager.class;
        rocoThrottleManagerClass = RocoXNetThrottleManager.class;
        programmerManagerClass = XNetProgrammerManager.class;
        programmerClass = XNetProgrammer.class;
        consistManagerClass = XNetConsistManager.class;
        turnoutManagerClass = XNetTurnoutManager.class;
        lightManagerClass = XNetLightManager.class;
        sensorManagerClass = XNetSensorManager.class;
        return this;
    }

    /**
     * Set the power Manager class
     * @param powerManagerClass the power manager class to use
     * @return this initializer
     */
    public XNetInitializationManager powerManager(Class<? extends XNetPowerManager> powerManagerClass){
        this.powerManagerClass = powerManagerClass;
        return this;
    }

    private void initPowerManager(){
        if(powerManagerClass != null){
            try {
                Constructor<? extends XNetPowerManager> ctor = powerManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetPowerManager pm = ctor.newInstance(systemMemo);
                systemMemo.setPowerManager(pm);
                InstanceManager.store(pm,PowerManager.class);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct power manager for XPressNet connection {}", systemMemo.getSystemPrefix(),e);
            }
        }
    }

    /**
     * Set the Programmer class to use with the XNetProgrammerManager.
     * @param programmerClass the programmer class to use
     * @return this initializer.
     */
    public XNetInitializationManager programmer(Class<? extends XNetProgrammer> programmerClass){
        this.programmerClass = programmerClass;
        return this;
    }

    private XNetProgrammer initProgrammer(){
        XNetProgrammer prog = null;
        if(programmerClass != null){
            try {
                Constructor<? extends XNetProgrammer> ctor = programmerClass.getConstructor(XNetTrafficController.class);
                prog = ctor.newInstance(systemMemo.getXNetTrafficController());
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct programmer for XPressNet connection {}", systemMemo.getSystemPrefix(),e);
            }
        }
        return prog;
    }

    public XNetInitializationManager noCommandStation(){
        this.noCommandStation = true;
        return this;
    }

    private void initCommandStation(){
        if(!noCommandStation) {
            /* The "raw" Command Station only works on systems that support Ops Mode Programming */
            systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
            if(systemMemo.getCommandStation()!= null) {
                InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
            }
        }
    }

    /**
     * Set the programmer manager to initialize
     * @param programmerManagerClass the programmer class to use.
     * @return this initializer.
     */
    public XNetInitializationManager programmerManager(Class<? extends XNetProgrammerManager> programmerManagerClass){
        this.programmerManagerClass = programmerManagerClass;
        return this;
    }

    private void initProgrammerManager() {
        XNetProgrammer programmer = initProgrammer();
        if (programmerManagerClass != null && programmer != null) {
            try {
                Constructor<? extends XNetProgrammerManager> ctor = programmerManagerClass.getConstructor(Programmer.class, XNetSystemConnectionMemo.class);
                XNetProgrammerManager pm = ctor.newInstance(programmer, systemMemo);
                systemMemo.setProgrammerManager(pm);
                if (pm.isAddressedModePossible()) {
                    InstanceManager.store(pm, jmri.AddressedProgrammerManager.class);
                    initCommandStation();
                }
                if (pm.isGlobalProgrammerAvailable()) {
                    InstanceManager.store(pm, GlobalProgrammerManager.class);
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.warn("Unable to construct programmer manager for XPressNet connection {}", systemMemo.getSystemPrefix(),e);
            }
        }
    }

    /**
     * Set the Throttle Manager Class
     * @param throttleManagerClass the Throttle Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager throttleManager(Class<? extends XNetThrottleManager> throttleManagerClass){
        this.throttleManagerClass = throttleManagerClass;
        return this;
    }

    private void initThrottleManager(){
        if(throttleManagerClass != null){
            try {
                Constructor<? extends XNetThrottleManager> ctor = throttleManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetThrottleManager tm = ctor.newInstance(systemMemo);
                systemMemo.setThrottleManager(tm);
                InstanceManager.store(tm, ThrottleManager.class);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct throttle manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    /**
     * Set the Roco Throttle Manager Class
     * @param rocoThrottleManagerClass the Roco Throttle Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager rocoThrottleManager(Class<? extends RocoXNetThrottleManager> rocoThrottleManagerClass){
        this.rocoThrottleManagerClass = rocoThrottleManagerClass;
        return this;
    }

    private void initRocoThrottleManager(){
        if(rocoThrottleManagerClass != null){
            try {
                Constructor<? extends RocoXNetThrottleManager> ctor = rocoThrottleManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                RocoXNetThrottleManager tm = ctor.newInstance(systemMemo);
                systemMemo.setThrottleManager(tm);
                InstanceManager.store(tm, ThrottleManager.class);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct throttle manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    /**
     * Set the Turnout Manager Class
     * @param turnoutManagerClass the Turnout Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager turnoutManager(Class<? extends XNetTurnoutManager> turnoutManagerClass){
        this.turnoutManagerClass = turnoutManagerClass;
        return this;
    }

    private void initTurnoutManager(){
        if(turnoutManagerClass != null){
            try {
                Constructor<? extends XNetTurnoutManager> ctor = turnoutManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetTurnoutManager tm = ctor.newInstance(systemMemo);
                systemMemo.setTurnoutManager(tm);
                InstanceManager.setTurnoutManager(tm);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct turnout manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    /**
     * Set the Sensor Manager Class
     * @param sensorManagerClass the Sensor Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager sensorManager(Class<? extends XNetSensorManager> sensorManagerClass){
        this.sensorManagerClass = sensorManagerClass;
        return this;
    }

    private void initSensorManager(){
        if(sensorManagerClass != null){
            try {
                Constructor<? extends XNetSensorManager> ctor = sensorManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetSensorManager sm = ctor.newInstance(systemMemo);
                systemMemo.setSensorManager(sm);
                InstanceManager.setSensorManager(sm);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct sensor manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    /**
     * Set the Light Manager Class
     * @param lightManagerClass the Light Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager lightManager(Class<? extends XNetLightManager> lightManagerClass){
        this.lightManagerClass = lightManagerClass;
        return this;
    }

    private void initLightManager(){
        if(lightManagerClass != null){
            try {
                Constructor<? extends XNetLightManager> ctor = lightManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetLightManager lm = ctor.newInstance(systemMemo);
                systemMemo.setLightManager(lm);
                InstanceManager.setLightManager(lm);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct light manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    /**
     * Set the Consist Manager Class
     * @param consistManagerClass the Consist Manager Class to use.
     * @return this initializer
     */
    public XNetInitializationManager consistManager(Class<? extends XNetConsistManager> consistManagerClass){
        this.consistManagerClass = consistManagerClass;
        return this;
    }

    private void initConsistManager(){
        if(consistManagerClass != null){
            try {
                Constructor<? extends XNetConsistManager> ctor = consistManagerClass.getConstructor(XNetSystemConnectionMemo.class);
                XNetConsistManager tm = ctor.newInstance(systemMemo);
                systemMemo.setConsistManager(tm);
                InstanceManager.store(tm, ConsistManager.class);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e){
                log.warn("Unable to construct consist manager for XPressNet connection {}", systemMemo.getSystemPrefix());
            }
        }
    }

    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }
        /* Load managers that should work on all systems */
        initPowerManager();
        if (versionCheck) {
            checkVersionAndInit();
        } else {
            initServices();
        }
    }

    private void checkVersionAndInit() {
        /* spawn a thread to request version information and wait for the
         command station to respond */
        log.debug("Starting XpressNet Initialization Process");
        new XNetInitializer(this);

        // Since we can't currently reconfigure the user interface after
        // initialization, We need to wait for the initialization thread
        // to finish before we can continue.  The wait  can be removed IF
        // we revisit the GUI initialization process.
        synchronized (this) {
            log.debug("start wait");
            new jmri.util.WaitHandler(this);
            log.debug("end wait");
        }
        float CSSoftwareVersion = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationSoftwareVersion();
        int CSType = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationType();

        if (CSSoftwareVersion < 0) {
            log.warn("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
            initServices();
        } else if (CSSoftwareVersion < 3.0) {
            log.error("Command Station does not support XpressNet Version 3 Command Set");
            initThrottleManager();
        } else {
            /* Next we check the command station type, and add the
             appropriate managers */
            if (CSType == 0x02) {
                log.debug("Command Station is Compact/Commander/Other");
                initThrottleManager();
                initTurnoutManager();
                initLightManager();
                initConsistManager();
            } else if (CSType == 0x01) {
                log.debug("Command Station is LH200");
                initThrottleManager();
            } else if (CSType == 0x00) {
                log.debug("Command Station is LZ100/LZV100");
                initServices();
            } else if (CSType == 0x04) {
                log.debug("Command Station is LokMaus II");
                initRocoThrottleManager();
                initTurnoutManager();
                initLightManager();
                initSensorManager();
                initProgrammerManager();
                // LokMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
            } else if (CSType == 0x10 ) {
                log.debug("Command Station is multiMaus");
                initRocoThrottleManager();
                initTurnoutManager();
                initLightManager();
                initSensorManager();
                initProgrammerManager();
                // multMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
            } else {
                /* If we still don't  know what we have, load everything */
                log.debug("Command Station is Unknown type");
                initServices();
            }
        }
        log.debug("XpressNet Initialization Complete");
    }

    private void initServices(){
        initThrottleManager();
        initProgrammerManager();
        initConsistManager();
        initTurnoutManager();
        initLightManager();
        initSensorManager();
    }

    /* Internal class to retrieve version Information */
    protected class XNetInitializer implements XNetListener {

        private final javax.swing.Timer initTimer; // Timer used to let he
        // command station response time
        // out, and configure the defaults.

        private final Object parent;

        public XNetInitializer(Object Parent) {

            parent = Parent;

            initTimer = setupInitTimer();

            // Register as an XpressNet Listener
            systemMemo.getXNetTrafficController().addXNetListener(XNetInterface.CS_INFO, this);

            //Send Information request to LI100/LI100
         /* First, we need to send a request for the Command Station
             hardware and software version */
            XNetMessage msg = XNetMessage.getCSVersionRequestMessage();
            //Then Send the version request to the controller
            systemMemo.getXNetTrafficController().sendXNetMessage(msg, this);
        }

        protected javax.swing.Timer setupInitTimer() {
            // Initialize and start initialization timeout timer.
            javax.swing.Timer retVal = new javax.swing.Timer(initTimeout,
                    (ActionEvent e) -> {
                                    /* If the timer times out, notify any
                                     waiting objects, and dispose of
                                     this thread */
                        if (log.isDebugEnabled()) {
                            log.debug("Timeout waiting for Command Station Response");
                        }
                        finish();
                    });
            retVal.setInitialDelay(initTimeout);
            retVal.start();
            return retVal;
        }

        @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL", justification = "There should only ever be one thread waiting for this method (the designated parent, which started the thread).")
        private void finish() {
            initTimer.stop();
            // Notify the parent
            try {
                synchronized (parent) {
                    parent.notify();
                }
            } catch (Exception e) {
                log.error("Exception {] while notifying initialization thread.",e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Notification Sent");
            }
            // Then dispose of this object
            dispose();
        }

        // listen for the responses from the LI100/LI101
        @Override
        public void message(XNetReply l) {
            // Check to see if this is a response with the Command Station
            // Version Info
            if (l.getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE &&
                    l.getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                // This is the Command Station Software Version Response
                systemMemo.getXNetTrafficController()
                        .getCommandStation()
                        .setCommandStationSoftwareVersion(l);
                systemMemo.getXNetTrafficController()
                        .getCommandStation()
                        .setCommandStationType(l);
                finish();
            }
        }

        // listen for the messages to the LI100/LI101
        @Override
        public void message(XNetMessage l) {
            // we aren't concerned with incoming messages in this class.
        }

        // Handle a timeout notification
        @Override
        public void notifyTimeout(XNetMessage msg) {
            if (log.isDebugEnabled()) {
                log.debug("Notified of timeout on message {}",msg);
            }
        }

        public void dispose() {
            systemMemo.getXNetTrafficController().removeXNetListener(XNetInterface.CS_INFO, this);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(XNetInitializationManager.class);
}

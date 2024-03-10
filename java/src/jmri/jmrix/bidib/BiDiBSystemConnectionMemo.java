package jmri.jmrix.bidib;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.GlobalProgrammerManager;

import jmri.InstanceManager;
import jmri.LightManager;
//import jmri.MultiMeter;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.CommandStation;
import jmri.NamedBean;
import jmri.ReporterManager;
//import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
//import jmri.ConsistManager;
import jmri.util.NamedBeanComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 * @author Eckart Meyer Copyright (C) 2019-2023
 *
 * Based on DCCppSystemConnectionMemo by Paul Bender and Mark Underwood.
 */
public class BiDiBSystemConnectionMemo extends DefaultSystemConnectionMemo /* implements ConfiguringSystemConnectionMemo */  {

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BiDiBSystemConnectionMemo(@Nonnull BiDiBTrafficController tc) {
        super("B", "BiDiB");
        log.trace("**** ctor 1 BiDiBSystemConnectionMemo");
        this.tc = tc;
        tc.setSystemConnectionMemo(this);
        register(); // registers general type
        InstanceManager.store(this, BiDiBSystemConnectionMemo.class); // also register as specific type

        // create and register the BiDiBComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.bidib.swing.BiDiBComponentFactory(this),
        jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created BiDiBSystemConnectionMemo");
    }

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BiDiBSystemConnectionMemo() {
        super("B", "BiDiB");
        log.trace("**** ctor 2 BiDiBSystemConnectionMemo");
        register(); // registers general type
        InstanceManager.store(this, BiDiBSystemConnectionMemo.class); // also register as specific type

        // create and register the BiDiBComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.bidib.swing.BiDiBComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created BiDiBSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
     * 
     * @return BiDiB Traffic Controller
     */
    public BiDiBTrafficController getBiDiBTrafficController() {
        log.trace("getBiDiBTrafficController");
        if (tc == null) {
            setBiDiBTrafficController(new BiDiBTrafficController(null));
            log.debug("Auto create of BiDiBTrafficController for initial configuration");
        }
        return tc;
    }

    private BiDiBTrafficController tc;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param tc the {@link jmri.jmrix.bidib.BiDiBTrafficController} object to use.
     */
    public void setBiDiBTrafficController(@Nonnull BiDiBTrafficController tc) {
        this.tc = tc;
        // in addition to setting the traffic controller in this object,
        // set the systemConnectionMemo in the traffic controller
        tc.setSystemConnectionMemo(this);
    }

    /**
     * Configure the common managers for BiDiB connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit.
     */
    public void configureManagers() {
        if (tc.getCurrentGlobalProgrammerNode() != null) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }
        if (tc.getFirstCommandStationNode() != null) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
            InstanceManager.setThrottleManager(getThrottleManager());
            InstanceManager.store(getCommandStation(), jmri.CommandStation.class); //unsed??
        }
        if (tc.getFirstBoosterNode()!= null) {
            InstanceManager.store(getPowerManager(), jmri.PowerManager.class);
            // setup the PredefinedMeters
            createPredefinedMeters();
        }
//        if (tc.getFirstOutputNode() != null  ||  tc.getFirstCommandStationNode() != null) {
//        }

        // always
        InstanceManager.setTurnoutManager(getTurnoutManager());
        InstanceManager.setSensorManager(getSensorManager());
        InstanceManager.setReporterManager(getReporterManager());
        InstanceManager.setLightManager(getLightManager());
        
//        InstanceManager.store(getConsistManager(), ConsistManager.class);


    }
    
    /**
     * Called after all components (Sensors, Lights, ...) have been loaded 
     * Used to get some data from the device in one run
     * 
     * NOT USED ANYMORE
     */
    public void postConfigure() {
//        log.info("+++++++++ {}: post configure starts ++++++++++++", tc.getSystemConnectionMemo().getUserName());
//        log.debug("memo: {}, bidib: {}", this, tc.getBidib());
//        // get all BiDiB Port Configuration data
//        tc.allPortConfigX();
//        // and then the status of all ports
//        tc.allPortLcStat();
//        // then the status of all accessories
//        tc.allAccessoryState();
//        // finally all feedback channels
//        tc.allFeedback();
//        log.info("--------- {}: post configure ends -------------", tc.getSystemConnectionMemo().getUserName());
//        try {
//            tc.getBidib().getRootNode().getMagic(1000);
//            log.info("MAGIC received from root node: {}", tc.getBidib().getRootNode());
//        }
//        catch (ProtocolException ex) {
//            log.error("post configure - no answer from root node: " + tc.getBidib().getRootNode()); // NOSONAR
//        }
    }


    /**
     * Provides access to the Programmer for this particular connection.
     * 
     * @return programmer manager
     */
    public BiDiBProgrammerManager getProgrammerManager() {
        log.trace("getProgrammerManager");
        //Do not want to return a programmer if the system is disabled
        if (getDisabled()) {
            return null;
        }
        return (BiDiBProgrammerManager) classObjectMap.computeIfAbsent(BiDiBProgrammerManager.class,(Class<?> c) ->  {
            BiDiBProgrammerManager programmerManager = new BiDiBProgrammerManager(this);
            log.debug("programmer manager created: {}", programmerManager);
            return programmerManager;
        });
    }

    public void setProgrammerManager(BiDiBProgrammerManager p) {
        store(p,BiDiBProgrammerManager.class);
    }

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        log.trace("getThrottleManager");
        if (getDisabled()) {
            return null;
        }
        return (ThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, (Class<?> c) -> {
            ThrottleManager throttleManager = new BiDiBThrottleManager(this);
            log.debug("throttle manager created: {}", throttleManager);
            return throttleManager;
        });
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    /*
     * Provides access to the Power Manager for this particular connection.
     */
//    @Nonnull
    public PowerManager getPowerManager() {
        log.trace("getPowerManager");
        if (getDisabled()) {
            return null;
        }
        return (PowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class<?> c) -> {
            PowerManager powerManager = new BiDiBPowerManager(this);
            log.debug("power manager created: {}", powerManager);
            return powerManager;
        });
    }

    public void setPowerManager(@Nonnull PowerManager p) {
        store(p,PowerManager.class);
    }


    
    /*
     * Provides access to the Sensor Manager for this particular connection.
     */
    public SensorManager getSensorManager() {
        log.trace("getSensorManager");
        if (getDisabled()) {
            return null;
        }
        return (SensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (Class<?> c) -> {
            SensorManager sensorManager = new BiDiBSensorManager(this);
            log.debug("sensor manager created: {}", sensorManager);
            return sensorManager;
        });
    }

    public void setSensorManager(@Nonnull SensorManager s) {
        store(s,SensorManager.class);
    }

    /*
     * Provides access to the Reporter Manager for this particular connection.
     * BiDiB uses the reporter for RailCom
     */
    public ReporterManager getReporterManager() {
        log.trace("getReporterManager");
        if (getDisabled()) {
            return null;
        }
        return (ReporterManager) classObjectMap.computeIfAbsent(ReporterManager.class, (Class<?> c) -> {
            ReporterManager reporterManager = new BiDiBReporterManager(this);
            log.debug("reporter manager created: {}", reporterManager);
            return reporterManager;
        });
    }

    public void setReporterManager(@Nonnull ReporterManager s) {
        store(s,ReporterManager.class);
    }

    /*
     * Provides access to the Turnout Manager for this particular connection.
     */
    public TurnoutManager getTurnoutManager() {
        log.trace("getTurnoutManager");
        if (getDisabled()) {
            return null;
        }
        return (TurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class, (Class<?> c) -> {
            TurnoutManager turnoutManager = new BiDiBTurnoutManager(this);
            log.debug("turnout manager created: {}", turnoutManager);
            return turnoutManager;
        });
    }

    public void setTurnoutManager(@Nonnull TurnoutManager t) {
        store(t,TurnoutManager.class);
    }

    /*
     * Provides access to the Light Manager for this particular connection.
     */
    public LightManager getLightManager() {
        log.trace("getLightManager");
        if (getDisabled()) {
            return null;
        }
        return (LightManager) classObjectMap.computeIfAbsent(LightManager.class, (Class<?> c) -> {
            LightManager lightManager = new BiDiBLightManager(this);
            log.debug("light manager created: {}", lightManager);
            return lightManager;
        });
    }

    public void setLightManager(@Nonnull LightManager l) {
        store(l,LightManager.class);
    }

    /*
     * Provides access to the mulit meter for this particular connection.
     */
//    public void enableMultiMeter(){
//        jmri.InstanceManager.store( getMultiMeter(), jmri.MultiMeter.class );
//    }
    
    protected BiDiBPredefinedMeters predefinedMeters;
    
    public BiDiBPredefinedMeters createPredefinedMeters() {
        if (getDisabled()) {
            return null;
        }
        if (predefinedMeters == null) {
            InstanceManager.setMeterManager(new jmri.managers.AbstractMeterManager(this));
            predefinedMeters = new BiDiBPredefinedMeters(this);
        }
        return predefinedMeters;
    }
        
    /*
     * Provides access to the Command Station for this particular connection.
     * NOTE: Command Station defaults to NULL
     */
    public CommandStation getCommandStation() {
        log.trace("getCommandStation");
        if (getDisabled()  ||  tc.getFirstCommandStationNode()== null) {
            return null;
        }
        return (CommandStation) classObjectMap.computeIfAbsent(CommandStation.class, (Class<?> c) -> {
            return tc;
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> type) {
        log.trace("check for {}", type);
        //log.trace("  this: {}, tc: {}", this, tc);
//return (get(type) != null);
//TODO: what if we finally support "lost node" and "new node"?
        if (getDisabled() ||  tc == null) {
            return false;
        } else if (type.equals(jmri.GlobalProgrammerManager.class)) {
            BiDiBProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isGlobalProgrammerAvailable();
        } else if (type.equals(jmri.AddressedProgrammerManager.class)) {
            BiDiBProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isAddressedModePossible();
            //TODO: Update return value of the following as Managers are brought online.
        } else if (type.equals(jmri.ThrottleManager.class)) {
//            return true;
            return (tc.getFirstCommandStationNode() != null);
        } else if (type.equals(jmri.PowerManager.class)) {
//            return true;
            return (tc.getFirstBoosterNode() != null);
        } else if (type.equals(jmri.ReporterManager.class)) {
            return true;
//            return (tc.getFirstCommandStationNode() != null);
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
//            return (tc.getFirstOutputNode() != null  ||  tc.getFirstCommandStationNode() != null);
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
//            return (tc.getFirstOutputNode() != null  ||  tc.getFirstCommandStationNode() != null);
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else if (type.equals(jmri.ConsistManager.class)) {
            return false; //for now, we do not provide a consist manager - TODO!
        } else if (type.equals(jmri.CommandStation.class)) {
//            return true;
//            return false;
            return (tc.getFirstCommandStationNode() != null);
//        } else if (type.equals(jmri.MultiMeter.class)) {
////            return true;
//            return (tc.getFirstBoosterNode() != null);
        } else {
            return super.provides(type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
//    public <T> T get(Class<?> T) { //v5.2
    public <T> T get(Class<T> T) {
        if (getDisabled()) {
            return null;
        }
        log.trace("get {}", T);
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return null;//for now, we do not provide a consist manager - TODO!
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
        }
//        if (T.equals(jmri.MultiMeter.class)) {
//            return (T) getMultiMeter();
//        }
        return super.get(T);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected ResourceBundle getActionModelResourceBundle() {
        log.debug("getActionModelResourceBundle");
        return ResourceBundle.getBundle("jmri.jmrix.bidib.BiDiBActionListBundle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        log.info("---- dispose -----");
        InstanceManager.deregister(this, BiDiBSystemConnectionMemo.class);
        if (predefinedMeters != null) {
            predefinedMeters.dispose();
        }
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
        tc = null;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBSystemConnectionMemo.class);

}


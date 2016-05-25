// Dcc4PcSystemConnectionMemo.javaf
package jmri.jmrix.dcc4pc;

import java.util.List;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.RailComManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.managers.DefaultRailComManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version $Revision: 18322 $
 */
public class Dcc4PcSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public Dcc4PcSystemConnectionMemo(Dcc4PcTrafficController tc) {
        super("DP", "Dcc4Pc");
        this.tc = tc;
        tc.setAdapterMemo(this);
        register();
    }

    public Dcc4PcSystemConnectionMemo() {
        super("DP", "Dcc4Pc");
        register(); // registers general type
        InstanceManager.store(this, Dcc4PcSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.dcc4pc.swing.Dcc4PcComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public Dcc4PcTrafficController getDcc4PcTrafficController() {
        return tc;
    }

    public void setDcc4PcTrafficController(Dcc4PcTrafficController tc) {
        this.tc = tc;
    }
    private Dcc4PcTrafficController tc;

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.ProgrammerManager.class)) {
            if (progManager == null) {
                return false;
            }
            return true;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class) && provides(jmri.ProgrammerManager.class)){
            if(getProgrammerManager()!=null){
                return getProgrammerManager().isGlobalProgrammerAvailable();
            }
        }
        if (type.equals(jmri.AddressedProgrammerManager.class) && provides(jmri.ProgrammerManager.class)) {
            if(getProgrammerManager()!=null){
                return getProgrammerManager().isAddressedModePossible();
            }
        }
        return false; // nothing, by default
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for Dcc4Pc connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

        getRailCommManager();

        InstanceManager.setReporterManager(
                getReporterManager());

        InstanceManager.setSensorManager(
                getSensorManager());

    }

    private DefaultRailComManager railCommManager;

    public RailComManager getRailCommManager() {
        if (getDisabled()) {
            return null;
        }
        if (railCommManager == null) {
            railCommManager = new jmri.managers.DefaultRailComManager();
        }
        return railCommManager;
    }

    private Dcc4PcReporterManager reporterManager;

    public Dcc4PcReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new jmri.jmrix.dcc4pc.Dcc4PcReporterManager(getDcc4PcTrafficController(), this);
        }
        return reporterManager;
    }

    private Dcc4PcSensorManager sensorManager;

    public Dcc4PcSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new jmri.jmrix.dcc4pc.Dcc4PcSensorManager(getDcc4PcTrafficController(), this);
        }
        return sensorManager;
    }
    private Dcc4PcProgrammerManager programManager;

    public Dcc4PcProgrammerManager getProgrammerManager() {
        if (getDisabled()) {
            return null;
        }
        if (defaultProgrammer == null) {
            if (progManager == null) {
                return null;
            }
            List<SystemConnectionMemo> connList = jmri.InstanceManager.getList(SystemConnectionMemo.class);
            if (connList == null) {
                return null;
            }
            for (int i = 0; i < connList.size(); i++) {
                if (connList.get(i).getUserName().equals(progManager)) {
                    defaultProgrammer = connList.get(i).get(jmri.ProgrammerManager.class);
                    break;
                }
            }
        }
        if (programManager == null && defaultProgrammer!=null) {
            programManager = new jmri.jmrix.dcc4pc.Dcc4PcProgrammerManager(defaultProgrammer/*, new Dcc4PcProgrammer(defaultProgrammer)*/);
        }
        return programManager;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.dcc4pc.Dcc4PcActionListBundle");
    }

    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, Dcc4PcSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private ProgrammerManager defaultProgrammer;

    public void setRealProgramManager(ProgrammerManager dpm) {
        defaultProgrammer = dpm;
    }

    private String progManager;

    public void setDefaultProgrammer(String prog) {
        progManager = prog;
    }
}


/* @(#)Dcc4PcSystemConnectionMemo.java */

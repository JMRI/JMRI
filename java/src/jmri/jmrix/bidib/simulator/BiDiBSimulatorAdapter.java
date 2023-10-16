package jmri.jmrix.bidib.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Set;
import jmri.util.FileUtil;

import jmri.jmrix.bidib.BiDiBSerialPortController;
import jmri.jmrix.bidib.BiDiBTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.simulation.comm.SimulationBidib;
import org.bidib.jbidibc.simulation.SimulationInterface;

/**
 * Provide access to a simulated BiDiB system.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Eckart Meyer Copyright (C) 2019-2023
 *
 */
public class BiDiBSimulatorAdapter extends BiDiBSerialPortController {

    protected String simulationFile = "simulation.xml"; //default
    protected String loadedSimulationFilename = null;
    protected String absoluteSimulationFile = null;


    public BiDiBSimulatorAdapter() {
        //super(new BiDiBSystemConnectionMemo());
//        log.debug("ctor BiDiBSimulatorAdapter");
    }


    public String getSimulationFile() {
        return simulationFile;
    }
    
    public void setSimulationFile(String f) {
        if (loadedSimulationFilename == null) {
            loadedSimulationFilename = f;
        }
        simulationFile = f;
    }

    // not sure if this is the recommended way yu check our additional field... but works, I think.
    @Override
    public boolean isDirty() {
        log.debug("isDirty");
        if (super.isDirty()) {
            return true;
        }
        return (! simulationFile.equals(loadedSimulationFilename));
    }

    //is this ever called?
    @Override
    public boolean isRestartRequired() {
        log.debug("isRestartRequired");
        return super.isRestartRequired();
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        if (absoluteSimulationFile == null) {
            return getSimulationFile();
        }
        else {
            return absoluteSimulationFile;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * Get the "port name" in the format which is used by jbidibc - this is absolute path to the simulation XML file
     * @return real port name
     */
    @Override
    public String getRealPortName() {
        File f = new File(FileUtil.getExternalFilename("profile:" + getSimulationFile()));
        return f.getAbsolutePath();
    }
    
//    @Override
//    public void recover() {
//        log.debug("recover called - ignored.");
//        // nothing
//    }
//    
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void connect() throws java.io.IOException {
//        log.warn("connect called - ignored.");
//        //openPort(mPort, "JMRI app");
//    }

    /**
     * Here we do not really open something.
     * 
     * @param fileName name of simulation file
     * @param appName not used
     * @return error string, null if no error
     */
    @Override
    public String openPort(String fileName, String appName) {
        // NOT USED AND NOT CALLED! Would be normally called by ConnectConfigXml
        // we can't do anything meaningfull here. Even the existance of a file can't
        // be checked since the seachpath is a secret of the jbidibc library... (the SimulatorBidib)
          log.debug("simulation openPort: {}", fileName);
//        log.debug("user files path: {}", FileUtil.getUserFilesPath());
//        log.debug("in profile: {}", FileUtil.getExternalFilename("profile:" + fileName));
//        log.debug("in settings: {}", FileUtil.getExternalFilename("settings:" + fileName));
//        Profile p = ProfileManager.getDefault().getActiveProfile();
//        log.debug("active profile: path: {}, name: {}, uid: {}, id: {}", p.getPath(), p.getName(), p.getUniqueId(), p.getId());
//        File f = new File(fileName);
//        if (!f.exists()) {
//            f = new File(FileUtil.getExternalFilename("profile:" + fileName));
//        }
//        if (!f.exists()) {
//            f = new File(FileUtil.getExternalFilename("settings:" + fileName));
//        }
//        if (!f.exists()) {
//            log.error("File not found: {}", fileName);
//            return "File not found: " + fileName;
//        }
//        absoluteSimulationFile = f.getAbsolutePath();
//        log.info("Simulation file used: {}", f.getAbsoluteFile());
//        // open the port in XpressNet mode, check ability to set moderators
//        //setPort(portName);
//        //return "---- TEST ----";
        return null; // normal operation
    }


    /**
     * Set up all of the other objects to operate with a BiDiBSimulator
     * connected to this port
     */
    @Override
    public void configure() {
        log.debug("configure");
        MSG_RAW_LOGGER.debug("RAW> create BiDiB Instance");
        
        bidib = SimulationBidib.createInstance(getContext());
        BiDiBTrafficController tc = new BiDiBTrafficController(bidib);
        context = tc.connnectPort(this); //must be done before configuring managers since they may need features from the device
        log.debug("memo: {}, bidib simulator: {}", this.getSystemConnectionMemo(), bidib);
        this.getSystemConnectionMemo().setBiDiBTrafficController(tc);
        if (context != null) {
            opened = true;
        }
        else {
            opened = false;
            log.warn("Simulation cannot be opened: {} ({}})",
                    getCurrentPortName(), getCurrentPortName());
        }
        this.getSystemConnectionMemo().configureManagers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
                Set<MessageListener> messageListeners, Set<TransferListener> transferListeners) {
        
        SimulationInterface b = (SimulationInterface)bidib;
        b.setConnectionListener(connectionListener);
        b.registerListeners(nodeListeners, messageListeners, transferListeners);
    }
    
    // base class methods for the BiDiBSerialPortController interface
    // not used but must be implemented

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
//        log.trace("getInputStream - pin: {}", pin);
//        if (pin == null) {
//            log.error("getInputStream called before load(), stream not available");
//            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
//        }
//        return pin;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
//        log.trace("getInputStream - pin: {}", pout);
//        if (pout == null) {
//            log.error("getOutputStream called before load(), stream not available");
//            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
//        }
//        return pout;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }


    private final static Logger log = LoggerFactory.getLogger(BiDiBSimulatorAdapter.class);
    private static final Logger MSG_RAW_LOGGER = LoggerFactory.getLogger("RAW");

}

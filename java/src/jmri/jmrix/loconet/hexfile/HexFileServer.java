package jmri.jmrix.loconet.hexfile;

import jmri.GlobalProgrammerManager;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * copied from HexFileFrame, then all ui-related elements removed.
 * ConnectionConfigXml.load() calls HexFileServer instead of HexFileFrame if
 * jmri is running in headless mode.
 *
 * @author Steve Todd Copyright 2012
 */
public class HexFileServer {

    // member declarations
    // to find and remember the log file
//    final javax.swing.JFileChooser inputFileChooser =
//            jmri.jmrit.XmlFile.userFileChooser("Hex files", "hex");
    public HexFileServer() {
    }

    boolean connected = false;

    public void dispose() {
    }

    LnPacketizer packets = null;

    public void configure() {
        if (port == null) {
            log.error("initComponents called before adapter has been set");
            return;
        }
        // connect to a packetizing LnTrafficController
        packets = new LnPacketizer();
        packets.connectPort(port);
        connected = true;

        // create memo
        port.getSystemConnectionMemo().setLnTrafficController(packets);

        // do the common manager config
        port.getSystemConnectionMemo().configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, // full featured by default
                false, false);
        port.getSystemConnectionMemo().configureManagers();

        // Install a debug programmer, replacing the existing LocoNet one
        port.getSystemConnectionMemo().setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager(port.getSystemConnectionMemo()));
        if (port.getSystemConnectionMemo().getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.setAddressedProgrammerManager(port.getSystemConnectionMemo().getProgrammerManager());
        }
        if (port.getSystemConnectionMemo().getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(port.getSystemConnectionMemo().getProgrammerManager(), GlobalProgrammerManager.class);
        }

        // Install a debug throttle manager, replacing the existing LocoNet one
        port.getSystemConnectionMemo().setThrottleManager(new jmri.jmrix.debugthrottle.DebugThrottleManager(port.getSystemConnectionMemo()));
        jmri.InstanceManager.setThrottleManager(
                port.getSystemConnectionMemo().getThrottleManager());

        // start operation of packetizer
        packets.startThreads();
        sourceThread = new Thread(port);
        sourceThread.start();
    }

    private Thread sourceThread;
    //private Thread sinkThread;

    public void setAdapter(LnHexFilePort adapter) {
        port = adapter;
    }

    public LnHexFilePort getAdapter() {
        return port;
    }
    private LnHexFilePort port = null;

    private final static Logger log = LoggerFactory.getLogger(HexFileServer.class);

}

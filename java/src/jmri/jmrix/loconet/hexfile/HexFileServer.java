package jmri.jmrix.loconet.hexfile;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.GlobalProgrammerManager;
import jmri.LocoAddress;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
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
        
    private int connectedAddresses = 0;

    // member declarations
    // to find and remember the log file
    // final javax.swing.JFileChooser inputFileChooser =
    // jmri.jmrit.XmlFile.userFileChooser("Hex files", "hex");
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
        packets = new LnPacketizer(port.getSystemConnectionMemo());
        packets.connectPort(port);
        connected = true;

        // set tc in memo
        port.getSystemConnectionMemo().setLnTrafficController(packets);

        // do the common manager config
        port.getSystemConnectionMemo().configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, // full featured by default
                false, false, false);
        port.getSystemConnectionMemo().configureManagers();

        // Install a debug programmer, replacing the existing LocoNet one
        port.getSystemConnectionMemo().setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager(port.getSystemConnectionMemo()));
        if (port.getSystemConnectionMemo().getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(port.getSystemConnectionMemo().getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (port.getSystemConnectionMemo().getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(port.getSystemConnectionMemo().getProgrammerManager(), GlobalProgrammerManager.class);
        }

        // Install a debug throttle manager and override 
        DebugThrottleManager tm = new DebugThrottleManager(port.getSystemConnectionMemo() ) {
            /**
             * Only address 128 and above can be a long address
             */
            @Override
            public boolean canBeLongAddress(int address) {
                return (address >= 128);
            }

            @Override
            public void requestThrottleSetup(LocoAddress a, boolean control) {
                if (!(a instanceof DccLocoAddress)) {
                    log.error("{} is not a DccLocoAddress",a);
                    failedThrottleRequest(a, "LocoAddress " + a + " is not a DccLocoAddress");
                    return;
                }
                connectedAddresses++;
                DccLocoAddress address = (DccLocoAddress) a;
                //create some testing situations
                if (connectedAddresses > 5) {
                    log.warn("SLOT MAX of 5 exceeded");
                    failedThrottleRequest(address, "SLOT MAX of 5 exceeded");
                    return;
                }
                // otherwise, continue with setup
                super.requestThrottleSetup(a, control);
            }

            @Override
            public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l) {
                connectedAddresses--;
                return super.disposeThrottle(t, l);
            }    
        };

        port.getSystemConnectionMemo().setThrottleManager(tm);

        jmri.InstanceManager.setThrottleManager(
                port.getSystemConnectionMemo().getThrottleManager());

        // start operation of packetizer
        packets.startThreads();
        sourceThread = new Thread(port, "LocoNet HexFileServer");
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

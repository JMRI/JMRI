// HexFileServer.java

package jmri.jmrix.loconet.hexfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.*;

/**
 *  copied from HexFileFrame, then all ui-related elements removed.  ConnectionConfigXml.load()
 *     calls HexFileServer instead of HexFileFrame if jmri is running in headless mode.
 * @author			Steve Todd  Copyright 2012
 * @version                     $Revision: 18841 $
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
        if (port==null){
            log.error("initComponents called before adapter has been set");
            return;
        }
        // connect to a packetizing LnTrafficController
        packets = new LnPacketizer();
        packets.connectPort(port);
        connected = true;

        // create memo
        port.getAdapterMemo().setSlotManager(new SlotManager(packets));
        port.getAdapterMemo().setLnTrafficController(packets);

        // do the common manager config
        port.getAdapterMemo().configureCommandStation(true, false, "<unknown>",   // full featured by default
                false, false);
        port.getAdapterMemo().configureManagers();

        // Install a debug programmer, replacing the existing LocoNet one
        port.getAdapterMemo().setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager(port.getAdapterMemo()));
        jmri.InstanceManager.setProgrammerManager(
                port.getAdapterMemo().getProgrammerManager());

        // Install a debug throttle manager, replacing the existing LocoNet one
        port.getAdapterMemo().setThrottleManager(new jmri.jmrix.debugthrottle.DebugThrottleManager(port.getAdapterMemo()));
        jmri.InstanceManager.setThrottleManager(
                port.getAdapterMemo().getThrottleManager());

        // start operation of packetizer
        packets.startThreads();
        sourceThread = new Thread(port);
        sourceThread.start();
            
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    private Thread sourceThread;
    //private Thread sinkThread;
    
    public void setAdapter(LnHexFilePort adapter) { port = adapter; }
    public LnHexFilePort getAdapter() { return port; }
    private LnHexFilePort port = null;

    static Logger log = LoggerFactory.getLogger(HexFileServer.class.getName());

}

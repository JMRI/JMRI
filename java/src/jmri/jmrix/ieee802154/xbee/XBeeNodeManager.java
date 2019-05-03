package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.models.DiscoveryOptions;
import java.util.EnumSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * The Node Manager checks incoming messages for node discovery 
 * response packets.  If a node is discovered, it is added to the traffic
 * controller's node list.
 *
 * @author Paul Bender Copyright(C) 2012,2016
 */
public class XBeeNodeManager implements IDiscoveryListener {

    private XBeeTrafficController xtc;
    private XBeeNetwork xbeeNetwork = null;

    public XBeeNodeManager(XBeeTrafficController tc) {
        xtc = tc;
        startNodeDiscovery();
    }

    /*
     * send out a node discovery request.
     */
    public void startNodeDiscovery() {
       log.info("Starting XBee Node Discovery Process");
       xbeeNetwork = xtc.getXBee().getNetwork();

       try {
          log.debug("configuring discovery timeout");
          // set the discovery timeout
          // setting the timeout hangs the network on XBee Series 1
          xbeeNetwork.setDiscoveryTimeout(2000);
          
          log.debug("setting discovery options");
          // set options
          // Append the device type identifier and the local device to the
          // network information.
          xbeeNetwork.setDiscoveryOptions(EnumSet.of(DiscoveryOptions.APPEND_DD,DiscoveryOptions.DISCOVER_MYSELF));
       } catch (com.digi.xbee.api.exceptions.TimeoutException te ) {
         log.debug("timeout during discovery process setup");
       } catch (com.digi.xbee.api.exceptions.XBeeException xbe) {
         log.error("exception during discovery process setup");
       }

       // add this class as a listener for node discovery.
       log.debug("adding Listener for discovery results");
       xbeeNetwork.addDiscoveryListener(this);

       // and start the discovery process.
       xbeeNetwork.startDiscoveryProcess();
       log.debug("Discovery Process started");
    }

    /*
     * @return true if the network discovery process is running
     */
    public boolean isDiscoveryRunning(){
       if(xbeeNetwork==null) { 
          return false;
       }
       return xbeeNetwork.isDiscoveryRunning();
    }

    /*
     * stop the discovery process, if it is running.
     */
    public void stopNodeDiscovery() {
      if(isDiscoveryRunning()){
         xbeeNetwork.stopDiscoveryProcess();
      }
    }

    // IDiscoveryListener interface methods
    
    /*
     * Device discovered callback.
     */
    @Override
    public void deviceDiscovered(RemoteXBeeDevice discoveredDevice){
        log.debug("New Device discovered {}", discoveredDevice.toString());
    }

    /*
     * Discovery error callback.
     */
    @Override
    public void discoveryError(String error){
        log.error("Error during node discovery process: {}",error);
    }

    /*
     * Discovery finished callback.
     */
    @Override
    public void discoveryFinished(String error){
       if(error != null){
         log.error("Node discovery processed finished with error: {}", error);
       } else {
         log.info("Node discovery process completed successfully wtih {} devices discovered", xbeeNetwork.getNumberOfDevices());
         // retrieve the node list from the network.
         List<RemoteXBeeDevice> nodeList = xbeeNetwork.getDevices();

         // add the previously unkonwn nodes to the network.

         for(RemoteXBeeDevice device :nodeList ) {
             XBeeNode node = (XBeeNode) xtc.getNodeFromXBeeDevice(device);

             if (node == null) {
                // the node does not exist, we're adding a new one.
                try {
                   node = new XBeeNode(device);
                   // register the node with the traffic controller
                   xtc.registerNode(node);
                } catch(com.digi.xbee.api.exceptions.TimeoutException t){
                  log.error("Timeout registering device {}",device);
                } catch(com.digi.xbee.api.exceptions.XBeeException e) {
                  log.error("Exception registering device {}",device);
                }
             }
         }

         // and remove this class from the list of discovery listeners.

         // removing the listener here is causing a 
         // ConcurrentModificaitonException on an ArrayList in the library.
         // xbeeNetwork.removeDiscoveryListener(this);
       }
    }

    private static final Logger log = LoggerFactory.getLogger(XBeeNodeManager.class);

}



/* XBeeNodeManager.java */
package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.ATCommandResponse;
import com.digi.xbee.api.models.DiscoveryOptions;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * The Node Manager checks incoming messages for node discovery 
 * response packets.  If a node is discovered, it is added to the traffic
 * controller's node list.
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
       // XBeeMessage m = new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("ND"));
       // xtc.sendXBeeMessage(m, this);
       xbeeNetwork = xtc.getXBee().getNetwork();

       // set the discovery timeout
       xbeeNetwork.setDiscoveryTimeout(10000);

       // set options
       // Append the device type identifier and the local device to the
       // network information.
       xbeeNetwork.setDiscoveryOptions(EnumSet.of(DiscoveryOptions.APEND_DD,DiscoveryOptions.DISCOVER_MYSELF));

       // add this class as a listener for node discovery.
       xbeeNetwork.addDiscoveryListener(this);

       // and start the discovery process.
       xbeeNetwork.startDiscoveryProcess();
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
    public void discoveryError(String error){
        log.error("Error durring node discovery process: {}",error);
    }

    /*
     * Discovery finished callback.
     */
    public void discoveryFinished(String error){
       if(error != null){
         log.error("Node discovery processed finished with error: {}", error);
       } else {
         log.debug("Node discovery process completed successfully.");
         // retrieve the node list from the network.
         List<RemoteXBeeDevice> nodeList = xbeeNetwork.getDevice();

         // add the previously unkonwn nodes to the network.

         // and remove this class from the list of discovery listeners.
         xbeeNetwork.removeDiscoveryListener(this);
       }
    }



    /* Incoming messages are searched for Node Discovery packets 
     * If one is found, the responding node is added to the
     * Traffic Controller's node list.
     */
/*    public void reply(XBeeReply m) {
        // NOTE: portions of this code are derived from the XBeeAPI node
        // discovery examples for wpan and zigbee.
        XBeeResponse response = m.getXBeeResponse();
        if (response instanceof AtCommandResponse) {
            AtCommandResponse atResponse = (AtCommandResponse) response;
            if (xtc.isSeries1() && atResponse.getCommand().equals("ND") && atResponse.getValue() != null
                    && atResponse.getValue().length > 0) {
                WpanNodeDiscover nd = WpanNodeDiscover.parse(atResponse);
                if (log.isDebugEnabled()) {
                    log.debug("Node Discover is " + nd);
                }
                XBeeNode node = (XBeeNode) xtc.getNodeFromAddress(nd.getNodeAddress16().getAddress());
                if (node == null) {
                    // try looking up the node using the 64 bit address
                    node = (XBeeNode) xtc.getNodeFromAddress(nd.getNodeAddress64().getAddress());
                    if (node == null) {
                        // the node does not exist, we're adding a new one.
                        node = (XBeeNode) xtc.newNode();
                        // register the node with the traffic controller
                        xtc.registerNode(node);
                    }
                }

                // update the node information.
                node.setNodeAddress(nd.getNodeAddress16().get16BitValue());
                int ad16i[] = nd.getNodeAddress16().getAddress();
                byte ad16b[] = node.getUserAddress();
                for (int i = 0; i < 2; i++) {
                    ad16b[i] = (byte) ad16i[i];
                }
                node.setUserAddress(ad16b);
                int ad64i[] = nd.getNodeAddress64().getAddress();
                byte ad64b[] = node.getGlobalAddress();
                for (int i = 0; i < 8; i++) {
                    ad64b[i] = (byte) ad64i[i];
                }
                node.setGlobalAddress(ad64b);
                node.setIdentifier(nd.getNodeIdentifier());
            } else if (xtc.isSeries2() && atResponse.getCommand().equals("ND") && atResponse.getValue() != null
                    && atResponse.getValue().length > 0) {
                ZBNodeDiscover nd = ZBNodeDiscover.parse((AtCommandResponse) response);
                if (log.isDebugEnabled()) {
                    log.debug("Node Discover is " + nd);
                }
                // 16 bit addresses may be assigned by the coordinator in series
                // 2 nodes, so only look up the 64 bit address.
                XBeeNode node = (XBeeNode) xtc.getNodeFromAddress(nd.getNodeAddress64().getAddress());
                if (node == null) {
                    // the node does not exist, we're adding a new one.
                    node = (XBeeNode) xtc.newNode();
                    // register the node with the traffic controller
                    xtc.registerNode(node);
                }

                // update the node information.
                node.setNodeAddress(nd.getNodeAddress16().get16BitValue());
                int ad16i[] = nd.getNodeAddress16().getAddress();
                byte ad16b[] = node.getUserAddress();
                for (int i = 0; i < 2; i++) {
                    ad16b[i] = (byte) ad16i[i];
                }
                node.setUserAddress(ad16b);
                int ad64i[] = nd.getNodeAddress64().getAddress();
                byte ad64b[] = node.getGlobalAddress();
                for (int i = 0; i < 8; i++) {
                    ad64b[i] = (byte) ad64i[i];
                }
                node.setGlobalAddress(ad64b);
                node.setIdentifier(nd.getNodeIdentifier());
            }
        } else if (response instanceof com.rapplogic.xbee.api.wpan.RxBaseResponse) {
            // check to see if the node sending this message is one we know
            // about.  If not, add it to the list of nodes.
            com.rapplogic.xbee.api.XBeeAddress xaddr = ((com.rapplogic.xbee.api.wpan.RxBaseResponse) response).getSourceAddress();
            if (xaddr instanceof com.digi.xbee.api.models.XBee16BitAddress) {
                XBeeNode node = (XBeeNode) xtc.getNodeFromAddress(xaddr.getAddress());
                if (node == null) {
                    // the node does not exist, we're adding a new one.
                    node = (XBeeNode) xtc.newNode();
                    // register the node with the traffic controller
                    xtc.registerNode(node);
                    // update the node information.
                    node.setNodeAddress(((com.digi.xbee.api.models.XBee16BitAddress) xaddr).get16BitValue());
                    int ad16i[] = xaddr.getAddress();
                    byte ad16b[] = node.getUserAddress();
                    for (int i = 0; i < 2; i++) {
                        ad16b[i] = (byte) ad16i[i];
                    }
                    node.setUserAddress(ad16b);
                }
            } else { // this is a 64 bit address.
                XBeeNode node = (XBeeNode) xtc.getNodeFromAddress(xaddr.getAddress());
                if (node == null) {
                    // the node does not exist, we're adding a new one.
                    node = (XBeeNode) xtc.newNode();
                    // register the node with the traffic controller
                    xtc.registerNode(node);
                    // update the node information.
                    int ad64i[] = xaddr.getAddress();
                    byte ad64b[] = node.getGlobalAddress();
                    for (int i = 0; i < 8; i++) {
                        ad64b[i] = (byte) ad64i[i];
                    }
                    node.setGlobalAddress(ad64b);
                }
            }
        } else if (response instanceof com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse) {
            // can't cast the message to RxBaseResponse, try ZNetRxBaseREsponse
            com.digi.xbee.api.models.XBee64BitAddress xaddr64 = ((com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse) response).getRemoteAddress64();
            com.digi.xbee.api.models.XBee16BitAddress xaddr16 = ((com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse) response).getRemoteAddress16();
            XBeeNode node = (XBeeNode) xtc.getNodeFromAddress(xaddr64.getAddress());
            if (node == null) {
                // the node does not exist, we're adding a new one.
                node = (XBeeNode) xtc.newNode();
                // register the node with the traffic controller
                xtc.registerNode(node);
                // update the node information.
                node.setNodeAddress(xaddr16.get16BitValue());
                int ad16i[] = xaddr16.getAddress();
                byte ad16b[] = node.getUserAddress();
                for (int i = 0; i < 2; i++) {
                    ad16b[i] = (byte) ad16i[i];
                }
                node.setUserAddress(ad16b);
                int ad64i[] = xaddr64.getAddress();
                byte ad64b[] = node.getGlobalAddress();
                for (int i = 0; i < 8; i++) {
                    ad64b[i] = (byte) ad64i[i];
                }
                node.setGlobalAddress(ad64b);
            }
        }
    }
*/

    private static final Logger log = LoggerFactory.getLogger(XBeeNodeManager.class);

}

/* @(#)XBeeNodeManager.java */

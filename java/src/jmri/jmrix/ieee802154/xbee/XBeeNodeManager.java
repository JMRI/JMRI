/* XBeeNodeManager.java */

package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBeeResponse;
//NOTE: this next line will need to change when the xbee api 
//library is updated.
import com.rapplogic.xbee.api.wpan.NodeDiscover;


/*
 * The Node Manager checks incoming messages for node discovery 
 * response packets.  If a node is discovered, it is added to the traffic
 * controller's node list.
 */

public class XBeeNodeManager implements XBeeListener {

  private XBeeTrafficController xtc;

  public XBeeNodeManager(XBeeTrafficController tc){
     xtc=tc;
     // register to receive responses
     xtc.addXBeeListener(this);
     startNodeDiscovery();
  }

  /*
   * send out a node discovery request.
   */
  public void startNodeDiscovery(){
     XBeeMessage m=new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("ND"));
     xtc.sendXBeeMessage(m,this);  
  }

  /* This class ignores outgoing messages */
  public void message(XBeeMessage m){
  }

  /* Incoming messages are searched for Node Discovery packets 
   * If one is found, the responding node is added to the
   * Traffic Controller's node list.
   */
  public void reply(XBeeReply m){
    // NOTE: portions of this code are derived from the XBeeAPI node
    // discovery examples for wpan and zigbee.
    XBeeResponse response=m.getXBeeResponse();
    if(response instanceof AtCommandResponse) {
       AtCommandResponse atResponse = (AtCommandResponse) response;
       if(atResponse.getCommand().equals("ND") && atResponse.getValue()!=null &&
          atResponse.getValue().length > 0){
          NodeDiscover nd = NodeDiscover.parse((AtCommandResponse)response);
          if(log.isDebugEnabled()) log.debug("Node Discover is " +nd);
          int address=nd.getNodeAddress16().get16BitValue();
          XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(address);
          if(node==null) {
             // the node does not exist, we're adding a new one.
             node=(XBeeNode)xtc.newNode();
             // register the node with the traffic controller
             xtc.registerNode(node); 
          }

          // update the node information.
          node.setNodeAddress(nd.getNodeAddress16().get16BitValue());
          int ad16i[]=nd.getNodeAddress16().getAddress();
          byte ad16b[]=node.getUserAddress();
          for(int i=0;i<2;i++)ad16b[i]=(byte)ad16i[i];
          node.setUserAddress(ad16b); 
          int ad64i[]=nd.getNodeAddress64().getAddress();
          byte ad64b[]=node.getGlobalAddress();
          for(int i=0;i<8;i++)ad64b[i]=(byte)ad64i[i];
          node.setGlobalAddress(ad64b);
          node.setIdentifier(nd.getNodeIdentifier());
       }
    }

  } 

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XBeeNodeManager.class.getName());


}

/* @(#)XBeeNodeManager.java */

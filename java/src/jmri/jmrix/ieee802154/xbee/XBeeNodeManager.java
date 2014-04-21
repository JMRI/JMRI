/* XBeeNodeManager.java */

package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.WpanNodeDiscover;
import com.rapplogic.xbee.api.zigbee.ZBNodeDiscover;


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
       if(xtc.isSeries1() && atResponse.getCommand().equals("ND") && atResponse.getValue()!=null &&
          atResponse.getValue().length > 0){
          WpanNodeDiscover nd = WpanNodeDiscover.parse((AtCommandResponse)response);
          if(log.isDebugEnabled()) log.debug("Node Discover is " +nd);
          XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(nd.getNodeAddress16().getAddress());
          if(node==null) {
             // try looking up the node using the 64 bit address
             node=(XBeeNode)xtc.getNodeFromAddress(nd.getNodeAddress64().getAddress());
             if(node==null) {
                // the node does not exist, we're adding a new one.
                node=(XBeeNode)xtc.newNode();
                // register the node with the traffic controller
                xtc.registerNode(node);
             } 
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
    else if(xtc.isSeries2() && atResponse.getCommand().equals("ND") && atResponse.getValue()!=null &&
          atResponse.getValue().length > 0){
          ZBNodeDiscover nd = ZBNodeDiscover.parse((AtCommandResponse)response);
          if(log.isDebugEnabled()) log.debug("Node Discover is " +nd);
          // 16 bit addresses may be assigned by the coordinator in series
          // 2 nodes, so only look up the 64 bit address.
          XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(nd.getNodeAddress64().getAddress());
          if(node==null) {
             // the node does not exist, we're adding a new one.
             node=(XBeeNode)xtc.newNode();
             // register the node with the traffic controller
             xtc.registerNode((jmri.jmrix.AbstractNode)node);
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
    } else if(response instanceof com.rapplogic.xbee.api.wpan.RxBaseResponse ) {
      // check to see if the node sending this message is one we know
      // about.  If not, add it to the list of nodes.
      com.rapplogic.xbee.api.XBeeAddress xaddr=((com.rapplogic.xbee.api.wpan.RxBaseResponse)response).getSourceAddress();
      if(xaddr instanceof com.rapplogic.xbee.api.XBeeAddress16) {
         XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(xaddr.getAddress());
         if(node==null) {
            // the node does not exist, we're adding a new one.
            node=(XBeeNode)xtc.newNode();
            // register the node with the traffic controller
            xtc.registerNode(node); 
            // update the node information.
            node.setNodeAddress(((com.rapplogic.xbee.api.XBeeAddress16)xaddr).get16BitValue());
            int ad16i[]=xaddr.getAddress();
            byte ad16b[]=node.getUserAddress();
            for(int i=0;i<2;i++)ad16b[i]=(byte)ad16i[i];
            node.setUserAddress(ad16b); 
         }
      } else { // this is a 64 bit address.
        XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(xaddr.getAddress());
        if(node==null) {
          // the node does not exist, we're adding a new one.
          node=(XBeeNode)xtc.newNode();
          // register the node with the traffic controller
          xtc.registerNode(node); 
          // update the node information.
          int ad64i[]=xaddr.getAddress();
          byte ad64b[]=node.getGlobalAddress();
          for(int i=0;i<8;i++)ad64b[i]=(byte)ad64i[i];
          node.setGlobalAddress(ad64b);
        }
      }
    } else if(response instanceof com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse ) {
      // can't cast the message to RxBaseResponse, try ZNetRxBaseREsponse
      com.rapplogic.xbee.api.XBeeAddress64 xaddr64 = ((com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse)response).getRemoteAddress64();
      com.rapplogic.xbee.api.XBeeAddress16 xaddr16 = ((com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse)response).getRemoteAddress16();
      XBeeNode node=(XBeeNode)xtc.getNodeFromAddress(xaddr64.getAddress());
      if(node==null) {
        // the node does not exist, we're adding a new one.
        node=(XBeeNode)xtc.newNode();
        // register the node with the traffic controller
        xtc.registerNode(node); 
        // update the node information.
        node.setNodeAddress(xaddr16.get16BitValue());
        int ad16i[]=xaddr16.getAddress();
        byte ad16b[]=node.getUserAddress();
        for(int i=0;i<2;i++)ad16b[i]=(byte)ad16i[i];
        node.setUserAddress(ad16b); 
        int ad64i[]=xaddr64.getAddress();
        byte ad64b[]=node.getGlobalAddress();
        for(int i=0;i<8;i++)ad64b[i]=(byte)ad64i[i];
        node.setGlobalAddress(ad64b);
      }
    }
  } 

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XBeeNodeManager.class.getName());


}

/* @(#)XBeeNodeManager.java */

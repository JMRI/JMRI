package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.AbstractNetworkPortController;

/**
 * Base tests for NetworkConnectionConfigXml objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractNetworkConnectionConfigXmlTestBase extends AbstractConnectionConfigXmlTestBase {

    /**
     * { @inheritdoc }
     */
    @Override
    protected void validateConnectionDetails(ConnectionConfig cc, Element e){
       Assume.assumeNotNull(cc.getAdapter());
       // network ports may have an address, a protocol, and a service type. or
       // mdns parameters
       AbstractNetworkPortController npc = (AbstractNetworkPortController) cc.getAdapter();
       if (npc.getMdnsConfigure()) {
          Assert.assertEquals("mdnsConfigure", "true", e.getAttribute("mdnsConfigure").getValue());
          Assert.assertEquals("advertisementName", npc.getAdvertisementName(), e.getAttribute("advertisementName").getValue());
          Assert.assertEquals("serviceType", npc.getServiceType(), e.getAttribute("serviceType").getValue());
          if(npc.getHostName()!=null && !npc.getHostName().equals("")) {
             Assert.assertEquals("address", npc.getHostName(), e.getAttribute("address").getValue());
          }
       } else {
          if (e.getAttribute("mdnsConfigure")!= null) {
             Assert.assertEquals("mdnsConfigure", "false", e.getAttribute("mdnsConfigure").getValue());
          }
          if (npc.getHostName()!=null) {
             Assert.assertEquals("address", npc.getHostName(), e.getAttribute("address").getValue());
          } else {
             Assert.assertEquals("address", Bundle.getMessage("noneSelected"), e.getAttribute("address").getValue());
          }
          if (npc.getPort()!=0) {
             Assert.assertEquals("port", "" + npc.getPort(), e.getAttribute("port").getValue());
          } else {
             Assert.assertEquals("address", Bundle.getMessage("noneSelected"), e.getAttribute("port").getValue());
          }
       }
    }

}

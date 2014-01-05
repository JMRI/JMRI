// LIUSBEthernetAdapter.java

package jmri.jmrix.lenz.liusbethernet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;

import java.util.ResourceBundle;
import jmri.util.zeroconf.ZeroConfClient;

/**
 * Provide access to XPressNet via a the Lenz LIUSBEthernet.
 * NOTES:  By default, the LIUSBEthernet has an IP address of
 * 192.168.0.200 and listens to port 5550.  
 * The LIUSBEtherenet disconnects both ports if there is 60 seconds of inactivity
 * on the port.
 *
 * @author			Paul Bender (C) 2011-2013
 * @version			$Revision$
 */

public class LIUSBEthernetAdapter extends XNetNetworkPortController {

         static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.XNetConfigurationBundle");
	static final int COMMUNICATION_TCP_PORT= 5550;
	static final String DEFAULT_IP_ADDRESS = "192.168.0.200";

        private javax.swing.Timer keepAliveTimer; // Timer used to periodically
                                                  // send a message to both
                                                  // ports to keep the ports 
                                                  // open
        private static final int keepAliveTimeoutValue = 30000; // Interval 
                                                            // to send a message
                                                            // Must be < 60s.

        public LIUSBEthernetAdapter(){
	    super();
            if(log.isDebugEnabled()) log.debug("Constructor Called");
            setHostName(DEFAULT_IP_ADDRESS);
            setPort(COMMUNICATION_TCP_PORT);
        }

        @Override
    public void connect() throws Exception {
        super.connect();
        if(log.isDebugEnabled()) log.debug("openPort called");
        keepAliveTimer();
    }

        /**
         * Can the port accept additional characters?
         * return true if the port is opened.
         */
        @Override
        public boolean okToSend() {
          return status();
        }
   
        @Override
    public boolean status() {return (opened);}
    
	/**
	 * set up all of the other objects to operate with a LIUSB Ethernet 
	 * interface
	 */
        @Override
	public void configure() {
            if(log.isDebugEnabled()) log.debug("configure called");
            // connect to a packetizing traffic controller
            XNetTrafficController packets = (new LIUSBEthernetXNetPacketizer(new LenzCommandStation()));
            packets.connectPort(this);

       	    // start operation
            // packets.startThreads();
            adaptermemo.setXNetTrafficController(packets);
 
            new XNetInitilizationManager(adaptermemo);

            jmri.jmrix.lenz.ActiveFlag.setActive();
	
       }

	/**
	 * Local method to do specific configuration
	 */

        @Deprecated
	static public LIUSBEthernetAdapter instance() {
		if (mInstance == null) mInstance = new LIUSBEthernetAdapter();
		return mInstance;
	}
	volatile static LIUSBEthernetAdapter mInstance = null;

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if(keepAliveTimer==null) {
            keepAliveTimer = new javax.swing.Timer(keepAliveTimeoutValue,new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        // If the timer times out, send a request for status
                        adaptermemo.getXNetTrafficController()
                                   .sendXNetMessage(
                                   jmri.jmrix.lenz.XNetMessage.getCSStatusRequestMessage(),
                                   null);
                    }
                });
        }
        keepAliveTimer.stop();
        keepAliveTimer.setInitialDelay(keepAliveTimeoutValue);
        keepAliveTimer.setRepeats(true);
        keepAliveTimer.start();
    }

    String manufacturerName = jmri.jmrix.DCCManufacturerList.LENZ;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    private boolean mDNSConfigure = false;

    /*
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     * @param autoconfig boolean value.
     */
     @Override
    public void setMdnsConfigure(boolean autoconfig){
       log.debug("Setting LIUSB Ethernet adapter autoconfiguration to: " +
                autoconfig);
       mDNSConfigure = autoconfig;
    }

    /*
     * Get whether or not this adapter is configured
     * to use autoconfiguration via MDNS
     * @return true if configured using MDNS.
     */
     @Override
    public boolean getMdnsConfigure() { return mDNSConfigure; }

    /*
     * set the server's host name and port
     * using mdns autoconfiguration.
     */
     @Override
    public void autoConfigure() {
       log.info("Configuring XPressNet interface via JmDNS");
       if(getHostName().equals(DEFAULT_IP_ADDRESS))
          setHostName(""); // reset the hostname to none.
       String serviceType = rb.getString("defaultMDNSServiceType");
       log.debug("Listening for service: " +serviceType );

       if( mdnsClient == null ) 
       {
             mdnsClient = new ZeroConfClient();
             mdnsClient.startServiceListener(serviceType);  
       }
       // leave the wait code below commented out for now.  It
       // does not appear to be needed for proper ZeroConf discovery.
       //try {
       //  synchronized(mdnsClient){
       //  // we may need to add a timeout here.
       //  mdnsClient.wait(keepAliveTimeoutValue);
       //  if(log.isDebugEnabled()) mdnsClient.listService(serviceType);
       //  }
       //} catch(java.lang.InterruptedException ie){
       //  log.error("MDNS auto Configuration failed.");
       //  return;
       //}
       try {
         // if there is a hostname set, use the host name (which can
         // be changed) to find the service.
         String qualifiedHostName = m_HostName +
              "." + rb.getString("defaultMDNSDomainName");
         setHostAddress(mdnsClient.getServiceOnHost(serviceType,
                            qualifiedHostName).getHostAddresses()[0]);
       } catch(java.lang.NullPointerException npe) {  
         // if there is no hostname set, use the service name (which can't
         // be changed) to find the service.
         String qualifiedServiceName = rb.getString("defaultMDNSServiceName") +
              "." + serviceType;
         setHostAddress(mdnsClient.getServicebyAdName(serviceType,
                            qualifiedServiceName).getHostAddresses()[0]);
       } 
    }

    ZeroConfClient mdnsClient = null;

   /*
    * Get the ZeroConf/mDNS advertisement name.
    * this value is fixed on the LIUSB-Ethernet, so return the default
    * value.
    */
   @Override
   public String getAdvertisementName() { return rb.getString("defaultMDNSServiceName"); }

   /*
    * Get the ZeroConf/mDNS service type.
    * this value is fixed on the LIUSB-Ethernet, so return the default
    * value.
    */
   @Override
   public String getServiceType(){ return rb.getString("defaultMDNSServiceType"); }

    static Logger log = LoggerFactory.getLogger(LIUSBEthernetAdapter.class.getName());

}

// LIUSBEthernetAdapter.java

package jmri.jmrix.lenz.liusbethernet;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;

import jmri.jmrix.lenz.XNetReply;


import java.io.*;


/**
 * Provide access to XPressNet via a the Lenz LIUSBEthernet.
 * NOTES:  By default, the LIUSBEthernet has an IP address of
 * 192.168.0.200 and listens to port 5550.  
 * The LIUSBEtherenet disconnects both ports if there is 60 seconds of inactivity
 * on the port.
 *
 * @author			Paul Bender (C) 2011
 * @version			$Revision: 18133 $
 */

public class LIUSBEthernetAdapter extends XNetNetworkPortController {

	static final int COMMUNICATION_TCP_PORT= 5550;
	static final String DEFAULT_IP_ADDRESS = "192.168.0.200";

        private javax.swing.Timer keepAliveTimer; // Timer used to periodically
                                                  // send a message to both
                                                  // ports to keep the ports 
                                                  // open
        private static final int keepAliveTimeoutValue = 30000; // Interval 
                                                            // to send a message
                                                            // Must be < 60s.

        //private CommunicationPortAdapter commAdapter= null;

	private DataOutputStream pout=null; // for output to other classes
    	private DataInputStream pin = null; // for input from other classes
	// internal ends of the pipe
        private DataOutputStream outpipe = null;  // feed pin
        private Thread commThread;

        public LIUSBEthernetAdapter(){
	    super();
            setHostName(DEFAULT_IP_ADDRESS);
            setPort(COMMUNICATION_TCP_PORT);
        }


    public void connect() throws Exception {
        super.connect();
        if(log.isDebugEnabled()) log.debug("openPort called");
        // open the port in XPressNet mode
        try {
            pout=getOutputStream();
            PipedOutputStream tempPipeO=new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        }
        catch (java.io.IOException e) {
              log.error("init (pipe): Exception: "+e.toString());
        }
        catch (Exception ex) {
            log.error("init (connect): Exception: "+ex.toString());
        }
        keepAliveTimer();
        //return null; // normal operation
    }

        /**
         * Can the port accept additional characters?
         * return true if the port is opened.
         */
        public boolean okToSend() {
          return status();
        }
   
    public boolean status() {return (pout!=null && pin!=null);}
    
	/**
	 * set up all of the other objects to operate with a LIUSB Ethernet 
	 * interface
	 */
	public void configure() {
            if(log.isDebugEnabled()) log.debug("configure called");
            // connect to a packetizing traffic controller
            XNetTrafficController packets = (new LIUSBEthernetXNetPacketizer(new LenzCommandStation()));
            packets.connectPort(this);


       	    // start operation
            // packets.startThreads();
            adaptermemo.setXNetTrafficController(packets);
 
            commThread = new Thread(new Runnable () {
       public void run(){ // start a new thread
       // this thread has one task.  It repeatedly reads from the two incomming
       // network connections and writes the resulting messages from the network
       // ports and writes any data received to the output pipe. 
       if(log.isDebugEnabled()) log.debug("Communication Adapter Thread Started");
       XNetReply r;
       BufferedReader bufferedin = new BufferedReader(new InputStreamReader(getInputStream()));
       for(;;){
             try{
                synchronized(this) {
                   r=loadChars(bufferedin);
                }
            } catch(java.io.IOException e) {
              continue;
            }
          if(log.isDebugEnabled()) log.debug("Network Adapter Received Reply: " + r.toString() );
          writeReply(r);
          }
        }
            });

            commThread.start();

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

       
       private synchronized void writeReply(XNetReply r){
       if(log.isDebugEnabled()) log.debug("Write reply to outpipe: "+ r.toString());
       int i;
       int len = (r.getElement(0)&0x0f)+2;  // opCode+Nbytes+ECC
       for(i=0;i<len;i++)
            try {
               outpipe.writeByte((byte)r.getElement(i));
            } catch  ( java.io.IOException ex){
            }
       }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg message to fill
     * @param istream character source.
     * @throws IOException when presented by the input source.
     */
    private XNetReply loadChars(java.io.BufferedReader istream) throws java.io.IOException {
        // The LIUSBEthernet sends us data as strings of hex values.
        // These hex values are followed by a <cr><lf>
        String s = "";
        s = istream.readLine(); 
        if(log.isDebugEnabled()) log.debug("Received from port: " +s);
        if (s == null) return null;
        else return new XNetReply(s);
    }
 
        // Internal class for communication port connection
        /*private static class CommunicationPortAdapter extends jmri.jmrix.AbstractNetworkPortController{
		 public CommunicationPortAdapter(){
          		super();
          		setHostName(DEFAULT_IP_ADDRESS);
          		setPort(COMMUNICATION_TCP_PORT);
                  }

                  public void configure(){
                  }
                  
                  public String getManufacturer() { return null; }
                  public void setManufacturer(String manu) { }

        }*/

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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBEthernetAdapter.class.getName());

}

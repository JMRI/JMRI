// LIUSBServerAdapter.java

package jmri.jmrix.lenz.liusbserver;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;

import jmri.jmrix.lenz.XNetReply;


import java.io.*;


/**
 * Provide access to XPressNet via a the Lenz LIUSB Server.
 * NOTES:  The LIUSB server binds only to localhost (127.0.0.1) on TCP 
 * ports 5550 and 5551.  Port 5550 is used for general communication.  
 * Port 5551 is used for broadcast messages only.
 * The LIUSB Server disconnects both ports if there is 60 seconds of inactivity
 * on the port.
 * The LIUSB Server disconnects port 5550 if another device puts the system 
 * into service mode. 
 *
 * @author			Paul Bender (C) 2009-2010
 * @version			$Revision$
 */

public class LIUSBServerAdapter extends XNetNetworkPortController {

	static final int COMMUNICATION_TCP_PORT= 5550;
	static final int BROADCAST_TCP_PORT = 5551;
	static final String DEFAULT_IP_ADDRESS = "localhost";

        private javax.swing.Timer keepAliveTimer; // Timer used to periodically
                                                  // send a message to both
                                                  // ports to keep the ports 
                                                  // open
        private static final int keepAliveTimeoutValue = 30000; // Interval 
                                                            // to send a message
                                                            // Must be < 60s.

        private BroadCastPortAdapter bcastAdapter= null;
        private CommunicationPortAdapter commAdapter= null;

	private DataOutputStream pout=null; // for output to other classes
    	private DataInputStream pin = null; // for input from other classes
	// internal ends of the pipe
        private DataOutputStream outpipe = null;  // feed pin
        private Thread commThread;
        private Thread bcastThread;

        public LIUSBServerAdapter(){
            super();
            option1Name = "BroadcastPort";
            options.put(option1Name, new Option("Broadcast Port", new String[]{String.valueOf(LIUSBServerAdapter.BROADCAST_TCP_PORT),""}));
        }

    synchronized public String openPort(String portName, String appName)  {
        if(log.isDebugEnabled()) log.debug("openPort called");
        // open the port in XPressNet mode
        try {
            bcastAdapter=new BroadCastPortAdapter();
            commAdapter=new CommunicationPortAdapter();
            bcastAdapter.connect();
            commAdapter.connect(); 
            pout=commAdapter.getOutputStream();
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
        return null; // normal operation
    }

        /**
         * Can the port accept additional characters?
         * return true if the port is opened.
         */
        public boolean okToSend() {
          return status();
        }

	// base class methods for the XNetNetworkPortController interface
        public DataInputStream getInputStream() {
                if (pin == null )
                    log.error("getInputStream called before load(), stream not available");
                    return pin;
                }

         public DataOutputStream getOutputStream() {
            if (pout==null) log.error("getOutputStream called before load(), stream not available");
            return pout;
         }
   
         public boolean status() {return (pout!=null && pin!=null);}

	/**
	 * set up all of the other objects to operate with a LIUSB Server 
	 * interface
	 */
	public void configure() {
            if(log.isDebugEnabled()) log.debug("configure called");
            // connect to a packetizing traffic controller
            XNetTrafficController packets = (new LIUSBServerXNetPacketizer(new LenzCommandStation()));
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
       BufferedReader bufferedin = new BufferedReader(new InputStreamReader(commAdapter.getInputStream()));
       for(;;){
             try{
                synchronized(commAdapter) {
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

            bcastThread = new Thread(new Runnable() {
       public void run(){ // start a new thread
       // this thread has one task.  It repeatedly reads from the two incomming
       // network connections and writes the resulting messages from the network
       // ports and writes any data received to the output pipe. 
       if(log.isDebugEnabled()) log.debug("Broadcast Adapter Thread Started");
       XNetReply r;
       BufferedReader bufferedin = new BufferedReader(new InputStreamReader(bcastAdapter.getInputStream()));
       for(;;){
             try{
                synchronized(bcastAdapter) {
                   r=loadChars(bufferedin);
                }
             } catch(java.io.IOException e) {
               continue;
             }          
          if(log.isDebugEnabled()) log.debug("Network Adapter Received Reply: " + r.toString() );
          r.setUnsolicited(); // Anything coming through the broadcast port
                              // is an unsolicited message.
          writeReply(r);
          }
        }
            });

            commThread.start();
            bcastThread.start();

            new XNetInitilizationManager(adaptermemo);

            jmri.jmrix.lenz.ActiveFlag.setActive();
	
       }


	/**
	 * Local method to do specific configuration
	 */

        @Deprecated
	static public LIUSBServerAdapter instance() {
		if (mInstance == null) mInstance = new LIUSBServerAdapter();
		return mInstance;
	}
	volatile static LIUSBServerAdapter mInstance = null;

       
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
        // The LIUSBServer sends us data as strings of hex values.
        // These hex values are followed by a <cr><lf>
        String s = "";
        s = istream.readLine(); 
        if(log.isDebugEnabled()) log.debug("Received from port: " +s);
        if (s == null) return null;
        else return new XNetReply(s);
    }
 
        //Internal class for broadcast port connection
        private static class BroadCastPortAdapter extends jmri.jmrix.AbstractNetworkPortController {
		 public BroadCastPortAdapter(){
          		super();
          		setHostName(DEFAULT_IP_ADDRESS);
          		setPort(BROADCAST_TCP_PORT);
                  }
		 
                  public void configure(){
                  }
                  public String getManufacturer() { return null; }
                  public void setManufacturer(String manu) { }
        }

        // Internal class for communication port connection
        private static class CommunicationPortAdapter extends jmri.jmrix.AbstractNetworkPortController{
		 public CommunicationPortAdapter(){
          		super();
          		setHostName(DEFAULT_IP_ADDRESS);
          		setPort(COMMUNICATION_TCP_PORT);
                  }

                  public void configure(){
                  }
                  
                  public String getManufacturer() { return null; }
                  public void setManufacturer(String manu) { }

        }

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if(keepAliveTimer==null) {
            keepAliveTimer = new javax.swing.Timer(keepAliveTimeoutValue,new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        /* If the timer times out, just send a character to the
                         *  ports.
                         */
                        try {
                           bcastAdapter.getOutputStream().write('z');
                           commAdapter.getOutputStream().write('z');
                        } catch(java.io.IOException ex){
                            //We need to do something here, because the
                            //communication port drops when another device
                            //puts the command station into service mode.
                            ex.printStackTrace();
                        }
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBServerAdapter.class.getName());

}

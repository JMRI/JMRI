// XnTcpAdapter.java

package jmri.jmrix.lenz.xntcp;

import org.apache.log4j.Logger;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;

import java.io.*;
import java.util.*;
import java.net.*;
import jmri.jmrix.ConnectionStatus;

/**
 * Provide access to XPressNet via a XnTcp interface attached on the Ethernet port.
 * @author			Giorgio Terdina Copyright (C) 2008-2011, based on LI100 adapter by Bob Jacobsen, Copyright (C) 2002, Portions by Paul Bender, Copyright (C) 2003
 * @version			$Revision$
 * GT - May 2008 - Added possibility of manually defining the IP address and the TCP port number
 * GT - May 2008 - Added updating of connection status in the main menu panel (using ConnectionStatus by Daniel Boudreau)
 * PB - December 2010 - refactored to be based off of AbstractNetworkController.
 * GT - May 2011 - Fixed problems arising from recent refactoring
 */

public class XnTcpAdapter extends XNetNetworkPortController implements jmri.jmrix.lenz.XNetPortController {

	static final int DEFAULT_UDP_PORT = 61234;
	static final int DEFAULT_TCP_PORT = 61235;
	static final String DEFAULT_IP_ADDRESS = "10.1.0.1";
	static final int UDP_LENGTH = 18;			// Length of UDP packet
	static final int BROADCAST_TIMEOUT = 1000;
	static final int READ_TIMEOUT = 8000;
	// Increasing MAX_PENDING_PACKETS makes output to CS faster, but may delay reception of unexpected notifications from CS
	static final int MAX_PENDING_PACKETS = 15;	// Allow a buffer of up to 128 bytes to be sent before waiting for acknowledgment

	private  Vector<String> hostNameVector = null;		// Contains the list of interfaces found on the LAN
	private  Vector<HostAddress> HostAddressVector = null;	// Contains their IP and port numbers
	private  InputStream inTcpStream = null;
	private  OutputTcpStream outTcpStream = null;
	private  int pendingPackets = 0;			// Number of packets sent and not yet acknowledged
	private String outName = "Manual";  // Interface name, used for possible error messages (can be either the netBios name or the IP address)


	public XnTcpAdapter(){
        super();
        option1Name = "XnTcpInterface";
        options.put(option1Name, new Option("XnTcp Interface:", getInterfaces()));
        m_HostName=DEFAULT_IP_ADDRESS;
        m_port=DEFAULT_TCP_PORT;
    }

	
	// Internal class, used to keep track of IP and port number
	//  of each interface found on the LAN
	private static class HostAddress {
		private String ipNumber;
		private int	portNumber;
		private HostAddress (String h, int p) {
			ipNumber = h;
			portNumber = p;
		}
	}
    
    String[] getInterfaces(){
        Vector<String> v = getInterfaceNames();
        String a[]=new String[v.size()+1];
            for (int i=0; i<v.size(); i++) 
                 a[i+1]=v.elementAt(i); 
        a[0]="Manual";
        return a;
    
    }
	
	public Vector<String> getInterfaceNames() {
		// Return the list of XnTcp interfaces connected to the LAN
		findInterfaces();
		return hostNameVector;
	}

	
		@Override
	public void connect() throws Exception {
		// Connect to the choosen XPressNet/TCP interface
		int ind;
		// Retrieve XnTcp interface name from Option1
		if(getOptionState(option1Name) != null) outName = getOptionState(option1Name);
		// Did user manually provide IP number and port?
		if(outName.equals("Manual")) {
			// Yes - retrieve IP number and port
			if(m_HostName == null) m_HostName = DEFAULT_IP_ADDRESS;
			if(m_port == 0) m_port = DEFAULT_TCP_PORT;
			outName = m_HostName;
		} else {
			// User specified a XnTcp interface name. Check if it's available on the LAN.
			if(hostNameVector == null) findInterfaces();
			if((ind = hostNameVector.indexOf(outName)) < 0) throw(new IOException("XpressNet/TCP interface "+outName+" not found"));
			// Interface card found. Get the relevantIP number and port
			m_HostName = HostAddressVector.get(ind).ipNumber;
			m_port = HostAddressVector.get(ind).portNumber;
		}
		try {
			// Connect!
			try {
				socketConn = new Socket(m_HostName, m_port);
				socketConn.setSoTimeout(READ_TIMEOUT);
	  			}
			catch (UnknownHostException e) {
    			ConnectionStatus.instance().setConnectionState(outName, ConnectionStatus.CONNECTION_DOWN);
				throw(e);
			}
			// get and save input stream
			inTcpStream = socketConn.getInputStream();
			// purge contents, if any
			int count = inTcpStream.available();
			log.debug("input stream shows "+count+" bytes available");
			while ( count > 0) {
				inTcpStream.skip(count);
				count = inTcpStream.available();
			}
			// Connection established.
			opened = true;
			ConnectionStatus.instance().setConnectionState(outName, ConnectionStatus.CONNECTION_UP);

		}
		// Report possible errors encountered while opening the connection
		catch (Exception e) {
			log.error("Unexpected exception while opening TCP connection with "+outName+" trace follows: "+e);
   			ConnectionStatus.instance().setConnectionState(outName, ConnectionStatus.CONNECTION_DOWN);
			throw(e);
		}
	}


	private void findInterfaces()  {
		// Retrieve all XnTcp interfaces available on the network
		// by broadcasting a UDP request on port 61234, listening
		// to all possible replies, storing in hostNameVector
		// the NETBIOS names of interfaces found and in
		// HostAddressVector their IP and port numbers

		DatagramSocket	udpSocket = null;
		
		hostNameVector = new Vector<String>(10, 1);
		HostAddressVector = new Vector<HostAddress>(10, 1);
		
		try {
			byte[] udpBuffer = new byte[UDP_LENGTH];
			// Create a UDP socket
			udpSocket = new DatagramSocket();
			// Prepare the output message (it should contain ASCII '%')
			udpBuffer[0]=0x25;
			DatagramPacket udpPacket = new DatagramPacket(udpBuffer, 1, InetAddress.getByName("255.255.255.255"), DEFAULT_UDP_PORT);
			// Broadcast the request
			udpSocket.send(udpPacket);
			// Set a timeout limit for replies
			udpSocket.setSoTimeout(BROADCAST_TIMEOUT);
			// Loop listening until timeout occurs
			while (true) {
				// Wait for a reply
				udpPacket.setLength(UDP_LENGTH);
				udpSocket.receive(udpPacket);
				// Reply received, make sure that we got all data
				if(udpPacket.getLength() >= UDP_LENGTH) {
					// Retrieve the NETBIOS name of the interface
					hostNameVector.addElement((new String(udpBuffer, 0, 16, "US-ASCII")).trim());
					// Retrieve the IP and port numbers of the interface
					HostAddressVector.addElement(new HostAddress(cleanIP((udpPacket.getAddress()).getHostAddress()), 
												((udpBuffer[16]) & 0xff)*256 + ((udpBuffer[17]) & 0xff)));
				}
			}
		}
		// When timeout or any error occurs, simply exit the loop
		catch (SocketTimeoutException e) {}
		catch (SocketException e) {}
		catch (IOException e) {}
		finally {
			// Before exiting, release resources
			if (udpSocket != null) {
				udpSocket.close();
				udpSocket = null;
			}
		}
	}

        /**
		 * TCP/IP stack and the XnTcp interface provide enough buffering to avoid
		 * overrun. However, queueing commands faster than they can be processed 
		 * should in general be avoided. To this purpose, a counter is incremented each time 
		 * a packet is queued and decremented when a reply from the interface is received.
		 * When the counter reaches the pre-defined maximum (e.g. 15) queuing of commands
		 * is blocked. Owing to broadcasts from the command station, the number of commands 
		 * received can actually be higher than that of commands sent, but this fact simply
		 * implies that we may have a higher number of pending commands for a while,
		 * without any negative consequence (the maximum is however arbitrary).
         **/
	synchronized protected void xnTcpSetPendingPackets(int s)
		{
			pendingPackets += s;
			if(pendingPackets < 0) pendingPackets = 0;
		}


        /**
		 * If an error occurs, either in the input or output thread, the display of a message
		 * is queued in the SWING thread. Since the input and output streams are not connected
		 * any more and the input and output threads will soon exit, user must save possible
		 * changes, quit, fix the problem and then restart JMRI.
         */
	synchronized protected void xnTcpError()
        {
			// If the error message was already posted to the SWING thread
			// simply ignore this call
			if(opened) {
			// Post an error message routine to the SWING thread
				javax.swing.SwingUtilities.invokeLater( new Runnable() {
					public void run() {
					ConnectionStatus.instance().setConnectionState(outName, ConnectionStatus.CONNECTION_DOWN);
					ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.xntcp.XnTcpBundle");
					javax.swing.JOptionPane.showMessageDialog(null,rb.getString("Error1"), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
				}}) ;
				// Clear open status, in order to avoid issuing the error message
				// more than than once.
				opened = false;
				log.debug("XnTcpError: TCP/IP communication dropped");
			}
        }

        /**
         * Can the port accept additional characters?
         * There is no CTS signal available. We only limit the number of commands queued in TCP/IP stack
         */
		@Override
	public boolean okToSend() {
			// If a communication error occurred, return always "true" in order to avoid program hang-up while quitting
			if(!opened) return true;
			synchronized(this) {
				// Return "true" if the maximum number of commands queued has not been reached
				log.debug("XnTcpAdapter.okToSend = " + (pendingPackets < MAX_PENDING_PACKETS) + " (pending packets =" + pendingPackets + ")");
				return pendingPackets < MAX_PENDING_PACKETS;
			}
        }

	/**
	 * set up all of the other objects to operate with a XnTcp interface
	 */
	public void configure() {
            // connect to a packetizing traffic controller
            XNetTrafficController packets = new XnTcpXNetPacketizer(new LenzCommandStation());
            packets.connectPort(this);
            adaptermemo.setXNetTrafficController(packets);
            new XNetInitilizationManager(adaptermemo);
		
            jmri.jmrix.lenz.ActiveFlag.setActive();
	}

// Base class methods for the XNetNetworkPortController interface
		@Override
	public DataInputStream getInputStream() {
		if (!opened) {
			log.error("getInputStream called before load(), stream not available");
			return null;
		}
		return new DataInputStream(inTcpStream);
	}

		@Override
	public DataOutputStream getOutputStream() {
		if (!opened) log.error("getOutputStream called before load(), stream not available");
		try {
				outTcpStream = (new OutputTcpStream(socketConn.getOutputStream()));
				return new DataOutputStream(outTcpStream);
     		}
     	catch (java.io.IOException e) {
     		log.error("getOutputStream exception: "+e.getMessage());
     	}
     	return null;
	}

		@Override
	public boolean status() {return opened;}
		
	// Extract the IP number from a URL, by removing
	// the domain name, if present
	private static String cleanIP(String ip){
		String outIP = ip;
		int i = outIP.indexOf("/");
		if((i>=0)  && (i < (outIP.length() -2))){
			outIP = outIP.substring(i+1);
		}
		return outIP;
	}
	
	// Our output class, used to count output packets and make sure that they are immediatelly sent
	public class OutputTcpStream extends OutputStream {
		private OutputStream tcpOut = null;
		private int count;
		
		public OutputTcpStream() {
		}
	
		public OutputTcpStream(OutputStream out) {
			tcpOut = out; // Save the handle to the actual output stream
			count = -1; // First byte should contain packet's length
     	}
			
		public void write(int b) throws java.io.IOException {
			// Make sure that we don't interleave bytes, if called
			// at the same time by different threads
			synchronized(tcpOut) {
				try {
					tcpOut.write(b);
					log.debug("XnTcpAdatper: sent " + Integer.toHexString(b & 0xff));
					// If this is the start of a new packet, save it's length
					if(count < 0) count = b & 0x0f;
					// If the whole packet was queued, send it and count it
					else if(count-- == 0) {
						tcpOut.flush();
						log.debug("XnTcpAdatper: flush ");
						xnTcpSetPendingPackets(1);
					}
				}
				catch (java.io.IOException e) {
					xnTcpError();
					throw e;
				}
			}
		}
		
		public void write(byte[] b, int off, int len) throws java.io.IOException {
			// Make sure that we don't mix bytes of different packets, 
			// if called at the same time by different threads
			synchronized(tcpOut) {
				while (len-- > 0) {
					write((b[off++]) & 0xff);
				}
			}
     	}
		
		public void write(byte[] b, int len) throws java.io.IOException {
			write(b, 0, len);
     	}
	}

    static Logger log = Logger.getLogger(XnTcpAdapter.class.getName());

}

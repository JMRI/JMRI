package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This is a server for Z21 clients like the Z21 App or the Roco Z21 WlanMaus. It is not meant to be a
 * fully equipped Z21 server.
 * 
 * @author Jean-Yves Roda (C) 2023
 * @author Eckart Meyer (C) 2025 (enhancements, WlanMaus support)
 */

// TODO:
// - handle MultiPacket datagrams (though neither the Z21 App not the WlanMaus seem to use them)
// - implement CV programming
// - create unit test classes

public class MainServer implements Runnable, PropertyChangeListener {

    private final static Logger log = LoggerFactory.getLogger(MainServer.class);
    public final static int port = 21105;
    DatagramSocket mySS;
    
/**
 * The main server running in a separate thread.
 * Do some setup and then read from the network in loop.
 */
    @Override
    public void run() {
        try {
            mySS = new DatagramSocket(port);

            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            log.info("Created socket, listening for connections");
            
            ClientManager.getInstance().setChangeListener(this);
            Service40.setChangeListener(this);

            while (true) {

                if (Thread.interrupted()) break;
                boolean bReceivedData;

                log.trace("Loop ****");
                ClientManager.getInstance().handleExpiredClients(false);

                //mySS.setSoTimeout(500);
                mySS.setSoTimeout(2000);// since almost everything is asynchroneous now, we need the forced exec only for expired clients
                try {
                    mySS.receive(packet);
                    bReceivedData = true;
                } catch (Exception e) {
                    bReceivedData = false;
                }

                if (!bReceivedData) continue;
                

                InetAddress clientAddress = packet.getAddress();

                ClientManager.getInstance().heartbeat(clientAddress);

                byte[] rawData = packet.getData();
                int dataLenght = rawData[0];
                byte[] actualData = Arrays.copyOf(rawData, dataLenght);
                String ident = "[" + clientAddress + "]  ";
                log.debug("{}: recv raw frame {} ", ident, bytesToHex(actualData));

                if (actualData.length < 3) {
                    log.debug("error, frame : {}", bytesToHex(actualData));
                }

                byte[] response = null;

                // parse header byte.
                switch (actualData[2]) {
                    case 0x50:
                        // LAN_SET_BROADCASTFLAGS - currently ignored, but accepted
                        byte[] maskArray = Arrays.copyOfRange(actualData, HEADER_SIZE, dataLenght);
                        int mask = fromByteArrayLittleEndian(maskArray);
                        log.debug("{} Broadcast request with mask : {}", ident, Integer.toBinaryString(mask));
                        break;
                    case 0x30:
                        // LAN_LOGOFF - the client wants to exit
                        log.debug("{} Disconnect frame", ident);
                        ClientManager.getInstance().unregisterClient(clientAddress);
                        break;
                    case 0x10:
                        // LAN_GET_SERIAL_NUMBER - send a serial number as 32bit number - we always send 0x00000000
                        log.debug("Send 32bit serialnumber");
                        response = new byte[] {0x08, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00};
                        break;
                    case 0x40:
                        // X-Bus commands - here is the real work
                        byte[] payloadData = Arrays.copyOfRange(actualData, HEADER_SIZE, dataLenght);
                        response = Service40.handleService(payloadData, clientAddress);
                        break;

                    default:
                        log.debug("{} Service not yet implemented : 0x{}", ident,  Integer.toHexString(actualData[2] & 0xFF));
                }

                if (response != null) {
                    // send back a response packet to the sending client
                    sendResponse(clientAddress, response);
                }
            }

            log.info("Z21 App Server shut down.");

        } catch (SocketException e) {
            log.info("Z21 App Server encountered an error, exiting.", e);
        }

        if (mySS != null) {
            mySS.close();
        }

    }
    
/**
 * Send a Z21 packet to all registered clients.
 * 
 * @param response - a Z21 packet
 */
    public void sendResponseToRegisteredClients(byte[] response) {
        HashMap<InetAddress, AppClient> registeredClients = ClientManager.getInstance().getRegisteredClients(); //send to all registered clients
        log.trace("Sending to all registered clients (broadcast)");
        for (HashMap.Entry<InetAddress, AppClient> entry : registeredClients.entrySet()) {   
            sendResponse(entry.getKey(), response);
        }
    }
    
/**
 * Send a Z21 packet to a single client.
 * 
 * @param respAddress - client's InetAdress
 * @param response - a Z21 packet
 */
    public void sendResponse(InetAddress respAddress, byte[] response) {
        if (response != null) {
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, respAddress, port);
            if (log.isTraceEnabled()) {
                String sendIdent = "[-> " + respAddress + "]  ";
                log.trace("{}: send raw frame {} ", sendIdent, bytesToHex(response));
            }
            try {
                mySS.send(responsePacket);
            } catch (Exception e) {
                log.debug("Unable to send packet to client {}", respAddress.toString());
            }
        }
    }
    
/**
 * Change listener.
 * If new value contains a Z21 packet, send it to all registered clients.
 * 
 * @param pce - property change event from the caller
 */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        log.trace("change event: {}", pce);
        if (pce.getNewValue() != null) {
            sendResponseToRegisteredClients( (byte [])pce.getNewValue() );
        }
    }
    
// helper functions

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static int fromByteArrayLittleEndian(byte[] bytes) {
        return ((bytes[2] & 0xFF) << 24) |
                ((bytes[3] & 0xFF) << 16) |
                ((bytes[0] & 0xFF) << 8) |
                ((bytes[1] & 0xFF) << 0);
    }

    private static final short HEADER_SIZE = 4;


}

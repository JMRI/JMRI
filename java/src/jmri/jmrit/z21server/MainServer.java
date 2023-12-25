package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class MainServer implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(MainServer.class);
    private final static int port = 21105;
    DatagramSocket mySS;
    @Override
    public void run() {
        try {
            mySS = new DatagramSocket(port);

            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            log.info("Created socket, listening for connections");

            while (true) {

                if (Thread.interrupted()) break;
                boolean bReceivedData;

                mySS.setSoTimeout(500);
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
                log.debug("{}: raw frame {} ", ident, bytesToHex(actualData));

                if (actualData.length < 3) {
                    log.debug("error, frame : {}", bytesToHex(actualData));
                }

                byte[] response = null;

                switch (actualData[2]) {
                    case 0x50:
                        byte[] maskArray = Arrays.copyOfRange(actualData, HEADER_SIZE, dataLenght);
                        int mask = fromByteArrayLittleEndian(maskArray);
                        log.debug("{} Broadcast request with mask : {}", ident, Integer.toBinaryString(mask));
                        break;
                    case 0x30:
                        log.debug("{} Disconnect frame", ident);
                        break;
                    case 0x40:
                        byte[] payloadData = Arrays.copyOfRange(actualData, HEADER_SIZE, dataLenght);
                        response = Service40.handleService(payloadData, clientAddress);
                        break;

                    default:
                        log.debug("{} Service not yet implemented : 0x{}", ident,  Integer.toHexString(actualData[2]));
                }

                if (response != null) {
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, port);
                    try {
                        mySS.send(responsePacket);
                    } catch (Exception e) {
                        log.debug("Unable to send packet to client {}", clientAddress.toString());
                    }
                }

                ClientManager.getInstance().handleExpiredClients();
            }

            log.info("Z21 App Server shut down.");

        } catch (SocketException e) {
            log.info("Z21 App Server encountered an error, exiting.", e);
        }

        if (mySS != null) {
            mySS.close();
        }

    }

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

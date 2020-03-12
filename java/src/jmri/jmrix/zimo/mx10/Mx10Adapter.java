package jmri.jmrix.zimo.mx10;

import jmri.jmrix.AbstractNetworkPortController;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.SystemConnectionMemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Mx10Adapter extends AbstractNetworkPortController {

    static final int SEND_UDP_PORT = 14520;
    static final int RECEIVE_UDP_PORT = 14521;

    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;

    public Mx10Adapter(SystemConnectionMemo memo){
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {

    }

    @Override
    public void connect() throws IOException {
        sendSocket = new DatagramSocket(SEND_UDP_PORT);
        receiveSocket = new DatagramSocket(RECEIVE_UDP_PORT);
    }




}

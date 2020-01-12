package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Override default XpressNet classes to use z21 specific versions.
 *
 * @author Paul Bender Copyright (C) 2004, 2010, 2014
 */
public class Z21XNetStreamPortController extends jmri.jmrix.lenz.XNetStreamPortController {

    public Z21XNetStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(in, out, pname);
    }

    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        jmri.jmrix.lenz.XNetTrafficController packets = new Z21XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation());
        packets.connectPort(this);

        this.getSystemConnectionMemo().setXNetTrafficController(packets);
        this.getSystemConnectionMemo().setThrottleManager(new Z21XNetThrottleManager(this.getSystemConnectionMemo()));

        new Z21XNetInitializationManager(this.getSystemConnectionMemo());
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(),jmri.jmrix.ConnectionStatus.CONNECTION_UP);
    }

    @Override
    public void dispose(){
        jmri.jmrix.lenz.XNetTrafficController packets = this.getSystemConnectionMemo().getXNetTrafficController();
        this.getSystemConnectionMemo().dispose();
        packets.terminateThreads();
        super.dispose();
    }

}

package jmri.jmrix.roco.z21;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.lenz.XNetInitializationManager;

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
        jmri.jmrix.lenz.XNetTrafficController packets = new Z21XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()){
            @Override
            protected AbstractMRReply newReply() {
               return new Z21XNetReply();
            }
        };
        packets.connectPort(this);

        this.getSystemConnectionMemo().setXNetTrafficController(packets);
        this.getSystemConnectionMemo().setThrottleManager(new Z21XNetThrottleManager(this.getSystemConnectionMemo()));

        new XNetInitializationManager()
                .memo(this.getSystemConnectionMemo())
                .setDefaults()
                .throttleManager(Z21XNetThrottleManager.class)
                .programmer(Z21XNetProgrammer.class)
                .programmerManager(Z21XNetProgrammerManager.class)
                .turnoutManager(Z21XNetTurnoutManager.class)
                .consistManager(null)
                .noCommandStation()
                .init();
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(),jmri.jmrix.ConnectionStatus.CONNECTION_UP);
    }

    @Override
    public void dispose() {
        jmri.jmrix.lenz.XNetTrafficController packets = this.getSystemConnectionMemo().getXNetTrafficController();
        this.getSystemConnectionMemo().dispose();
        packets.terminateThreads();
        super.dispose();
    }

}

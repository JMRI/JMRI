package jmri.jmrix.openlcb;

import java.util.concurrent.Semaphore;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import org.openlcb.Connection;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.can.CanInterface;

/**
 * Created by bracz on 11/7/16.
 */

public class OlcbTestInterface {
    public OlcbTestInterface() {
        tc = new TestTrafficController();
        nodeID = new NodeID("02.01.0D.00.00.01");
        canInterface = OlcbConfigurationManager.createOlcbCanInterface(nodeID, tc);
        iface = canInterface.getInterface();
    }

    public void waitForStartup() {
        final Semaphore s = new Semaphore(0);
        iface.getOutputConnection().registerStartNotification(new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                s.release();
            }
        });
        s.acquireUninterruptibly();
        flush();
    }

    public void sendMessage(CanMessage msg) {
        canInterface.frameInput().send(OlcbConfigurationManager.convertFromCan(msg));
    }

    public void flush() {
        iface.flushSendQueue();
    }

    static OlcbSystemConnectionMemo createForLegacyTests() {
        OlcbTestInterface testIf = new OlcbTestInterface();
        OlcbSystemConnectionMemo memo = new OlcbSystemConnectionMemo();
        memo.setTrafficController(testIf.tc);
        memo.setInterface(testIf.iface);
        testIf.waitForStartup();
        return memo;
    }

    TestTrafficController tc;
    NodeID nodeID;
    CanInterface canInterface;
    OlcbInterface iface;
}

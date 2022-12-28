package jmri.jmrix.openlcb.swing.hub;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;
import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;
import jmri.jmrix.can.swing.CanPanelInterface;
import org.openlcb.hub.Hub;

/**
 * Frame displaying,and more importantly starting, an OpenLCB TCP/IP hub
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012
 */
public class HubPane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    String nextLine;

    public HubPane() {
        super();
        hub = new Hub() {
            @Override
            public void notifyOwner(String line) {
                nextLine = line;
                SwingUtilities.invokeLater(() -> label.setText(nextLine));
            }
        };
    }

    CanSystemConnectionMemo memo;

    final transient Hub hub;

    final JLabel label = new JLabel("                                                 ");

    @Override
    public void initContext(Object context) {
        log.trace("initContext");
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        log.trace("initComponents");
        this.memo = memo;

        // This hears OpenLCB traffic at packet level from traffic controller
        memo.getTrafficController().addCanListener(this);

        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        try {
            add(new JLabel("Hub IP address " + InetAddress.getLocalHost().getHostAddress() + ":" + hub.getPort()));
        } catch (UnknownHostException e) {
            log.error("Unknown Host", e);
        }
        add(label);

        startHubThread(hub.getPort());
    }

    Thread t;

    void startHubThread(int port) {
        t = jmri.util.ThreadingUtil.newThread(hub::start,
                "OpenLCB Hub Thread");
        t.setDaemon(true);

        // add forwarder for internal JMRI traffic
        hub.addForwarder(m -> {
            if (m.source == null) {
                log.trace("not forwarding {} back to JMRI due to null source", m.line);
                return;  // was from this
            }
            // process and forward m.line
            GridConnectReply msg = new GridConnectReply();
            byte[] bytes;
            bytes = m.line.getBytes(StandardCharsets.US_ASCII);  // GC adapters use ASCII // NOI18N
            for (int i = 0; i < m.line.length(); i++) {
                msg.setElement(i, bytes[i]);
            }

            CanReply workingReply = msg.createReply();
            workingReplySet.add(workingReply);  // save for later recognition

            CanMessage result = new CanMessage(workingReply.getNumDataElements(), workingReply.getHeader());
            for (int i = 0; i < workingReply.getNumDataElements(); i++) {
                result.setElement(i, workingReply.getElement(i));
            }
            result.setExtended(workingReply.isExtended());
            workingMessageSet.add(result);
            log.trace("Hub forwarder create reply {}", workingReply);

            // Send over outbound link
            memo.getTrafficController().sendCanMessage(result, null); // HubPane.this
            // And send into JMRI
            memo.getTrafficController().distributeOneReply(workingReply, HubPane.this);
        });

        t.start();
        log.debug("hub thread started");
        advertise(port);
    }

    // For testing
    @SuppressWarnings("deprecation") // Thread.stop
    void stopHubThread() {
        if (t != null) {
            t.stop();
            t = null;
        }
    }

    java.util.ArrayList<CanReply> workingReplySet = new java.util.ArrayList<>(); // collection of self-sent replies
    java.util.ArrayList<CanMessage> workingMessageSet = new java.util.ArrayList<>(); // collection of self-sent messages

    void advertise(int port) {
        jmri.util.zeroconf.ZeroConfService.create("_openlcb-can._tcp.local.", port).publish();
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Hub Control");
        }
        return "LCC / OpenLCB Hub Control";
    }

    @Override
    public void dispose() {
       memo.getTrafficController().removeCanListener(this);
    }

    // connection from this JMRI instance - messages received here
    @Override
    public synchronized void message(CanMessage l) {  // receive a message and log it
        if ( workingMessageSet.contains(l)) {
            // ours, don't send
            workingMessageSet.remove(l);
            log.debug("suppress forward of message {} from JMRI; WMS={} items", l, workingMessageSet.size());
            return;
        }

        GridConnectMessage gm = new GridConnectMessage(l);
        log.debug("forward message {}",gm);
        hub.putLine(gm.toString());
    }

    // connection from this JMRI instance - replies received here
    @Override
    public synchronized void reply(CanReply reply) {
        if ( workingReplySet.contains(reply)) {
            // ours, don't send
            workingReplySet.remove(reply);
            log.trace("suppress forward of reply {} from JMRI; WRS={} items", reply, workingReplySet.size());
        } else {
            // not ours, forward
            GridConnectMessage gm = new GridConnectMessage(new CanMessage(reply));
            log.debug("forward reply {} from JMRI, WRS={} items", gm, workingReplySet.size());
            hub.putLine(gm.toString());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HubPane.class);

}

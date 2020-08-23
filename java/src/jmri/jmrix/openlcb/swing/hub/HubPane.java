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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        // This hears OpenLCB traffic at packet level from traffic controller
        memo.getTrafficController().addCanListener(this);

        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        try {
            add(new JLabel("Hub IP address " + InetAddress.getLocalHost().getHostAddress() + ":" + hub.getPort()));
        } catch (UnknownHostException e) {
            log.error(e.getLocalizedMessage(), e);
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
                return;  // was from this
            }                // process and forward m.line
            GridConnectReply msg = new GridConnectReply();
            byte[] bytes;
            bytes = m.line.getBytes(StandardCharsets.US_ASCII);  // GC adapters use ASCII // NOI18N
            for (int i = 0; i < m.line.length(); i++) {
                msg.setElement(i, bytes[i]);
            }
            workingReply = msg.createReply();

            CanMessage result = new CanMessage(workingReply.getNumDataElements(), workingReply.getHeader());
            for (int i = 0; i < workingReply.getNumDataElements(); i++) {
                result.setElement(i, workingReply.getElement(i));
            }
            result.setExtended(workingReply.isExtended());

            // Send over outbound link
            memo.getTrafficController().sendCanMessage(result, HubPane.this);
            // And send into JMRI
            memo.getTrafficController().distributeOneReply(workingReply, HubPane.this);
        });

        t.start();

        advertise(port);
    }

    // For testing
    @SuppressWarnings("deprecation") // Thread.stop not likely to be removed
    void stopHubThread() {
        if (t != null) {
            t.stop();
            t = null;
        }
    }

    CanReply workingReply;

    void advertise(int port) {
        jmri.util.zeroconf.ZeroConfService.create("_openlcb-can._tcp.local.", port).publish();
    }

    @Override
    public String getTitle() {
        return "OpenLCB Hub Control";
    }

    @Override
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
    }

    @Override
    public synchronized void message(CanMessage l) {  // receive a message and log it
        GridConnectMessage gm = new GridConnectMessage(l);
        if (log.isDebugEnabled()) {
            log.debug("message {}",gm);
        }
        hub.putLine(gm.toString());
    }

    @Override
    public synchronized void reply(CanReply reply) {
        if (reply != workingReply) {
            GridConnectMessage gm = new GridConnectMessage(new CanMessage(reply));
            log.debug("reply {}", gm.toString());
            hub.putLine(gm.toString());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(HubPane.class);

}

package jmri.jmrix.openlcb.swing.hub;

import java.awt.BorderLayout;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;
import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceManager;

import org.openlcb.hub.Hub;

/**
 * Frame displaying,and more importantly starting, an OpenLCB TCP/IP hub
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012
 */
public class HubPane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    /**
     * Create a new HubPane with default options.
     */
    public HubPane() {
        this(Hub.DEFAULT_PORT);
    }

    /**
     * Create a new HubPane with a specified port number.
     * @param port the port number to use.
     */
    public HubPane(int port) {
        this(port, true);
    }

    /**
     * Create a new HubPane with port number and a default line ending option.
     * This option may subsequently be ignored by user preference.
     * @param port the port number to use.
     * @param lineEndings if no user option is set, true to send line endings, else false.
     */
    public HubPane(int port, boolean lineEndings ) {
        super();
        textArea = new java.awt.TextArea();
        _line_endings = getLineEndingsFromUserPref(lineEndings);
        hub = new Hub(port, _line_endings) {
            @Override
            public void notifyOwner(String line) {
                SwingUtilities.invokeLater(() ->  {
                    textArea.append(
                        System.lineSeparator()+DateFormat.getDateTimeInstance().format(new Date()) + " " + line
                    );
                });
            }
        };
    }

    private static final String USER_SAVED = ".UserSaved";
    private static final String USER_LINE_ENDINGS = ".LineTermination";
    private boolean _line_endings;

    /**
     * Get the line endings setting to use in the Hub.
     * @param defaultValue normally true for OpenLCB, false for CBUS.
     * @return the preference, else default value.
     */
    private boolean getLineEndingsFromUserPref( boolean defaultValue ){
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        if ( p.getSimplePreferenceState(getClass().getName() + USER_SAVED)) {
            // user has loaded before so use the preference
            return p.getSimplePreferenceState(getClass().getName() + USER_LINE_ENDINGS);
        }
        return defaultValue;
    }

    CanSystemConnectionMemo memo;

    final transient Hub hub;

    @Override
    public void initContext(Object context) {
        log.trace("initContext");
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    final private java.awt.TextArea textArea;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        log.trace("initComponents");
        this.memo = memo;

        startHubThread(hub.getPort());

        // add GUI components
        setLayout(new BorderLayout());
        textArea.setEditable(false);

        add(new JScrollPane(textArea));
        add(BorderLayout.CENTER, new JScrollPane(textArea));

        textArea.append(Bundle.getMessage("HubStarted", // NOI18N
            DateFormat.getDateTimeInstance().format(new Date()), getTitle()));
        textArea.append( System.lineSeparator() + Bundle.getMessage("LineTermination")+" : "+ _line_endings);
        addInetAddresses();

        // This hears OpenLCB traffic at packet level from traffic controller
        memo.getTrafficController().addCanListener(this);
    }

    private void addInetAddresses(){
        ZeroConfServiceManager manager = InstanceManager.getDefault(ZeroConfServiceManager.class);
        Set<InetAddress> addresses = manager.getAddresses(ZeroConfServiceManager.Protocol.All, false, true);
        for (InetAddress ha : addresses) {
            textArea.append( System.lineSeparator() + Bundle.getMessage(("IpAddressLine"), // NOI18N
            !ha.getHostAddress().equals(ha.getHostName()) ? ha.getHostName() : "",
            ha.isLoopbackAddress() ? " Loopback" : "", // NOI18N
            ha.isLinkLocalAddress() ? " LinkLocal" : "", // NOI18N
            ha.getHostAddress() + ":" + hub.getPort()));
        }
    }

    Thread t;

    void startHubThread(int port) {
        t = jmri.util.ThreadingUtil.newThread(hub::start,
            memo.getUserName() + " Hub Thread");
        t.setDaemon(true);

        // add forwarder for internal JMRI traffic
        hub.addForwarder(m -> {
            if (m.source == null) {
                log.trace("not forwarding {} back to JMRI due to null source", m.line);
                return;  // was from this
            }
            // process and forward m.line
            GridConnectReply msg = getBlankReply();

            byte[] bytes = m.line.getBytes(StandardCharsets.US_ASCII);  // GC adapters use ASCII // NOI18N
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

            // Send into JMRI
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

    ArrayList<CanReply> workingReplySet = new ArrayList<>(); // collection of self-sent replies
    ArrayList<CanMessage> workingMessageSet = new ArrayList<>(); // collection of self-sent messages

    private ZeroConfService _zero_conf_service;
    protected String zero_conf_addr = "_openlcb-can._tcp.local.";

    protected void advertise(int port) {
        _zero_conf_service = ZeroConfService.create(zero_conf_addr, port);
        _zero_conf_service.publish();
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Hub Control");
        }
        return "LCC / OpenLCB Hub Control";
    }

    /**
     * Creates a Menu List
     * <p>
     * Settings > Line Termination
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        menuList.add(getSettingsMenu());
        return menuList;
    }

    private JMenu getSettingsMenu() {
        JMenu menu = new JMenu(Bundle.getMessage("MenuItemSettings")); // NOI18N
        JMenuItem lineFeedItem = new JMenuItem(Bundle.getMessage("LineTermination")); // NOI18N
        lineFeedItem.addActionListener(this::showDialog);
        menu.add(lineFeedItem);
        return menu;
    }

    void showDialog(java.awt.event.ActionEvent e) {
        JCheckBox checkbox = new JCheckBox(Bundle.getMessage("LineTermination")); // NOI18N
        checkbox.setSelected(_line_endings);
        
        Object[] params = {Bundle.getMessage("LineTermSettingDialog"), checkbox };
        
        int result = JOptionPane.showConfirmDialog(this, 
            params,
            Bundle.getMessage("LineTermination"), // NOI18N
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            _line_endings = checkbox.isSelected();
            UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
            p.setSimplePreferenceState(getClass().getName() + USER_SAVED, true); // NOI18N
            p.setSimplePreferenceState(getClass().getName() + USER_LINE_ENDINGS, _line_endings); // NOI18N
        }
    }

    @Override
    public void dispose() {
        if ( memo != null ) { // set on void initComponents
            memo.getTrafficController().removeCanListener(this);
        }
        if ( _zero_conf_service != null ) { // set on void advertise(int port)
            _zero_conf_service.stop();
        }
        stopHubThread();
        hub.dispose();
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
        GridConnectMessage gm = getMessageFrom(l);
        log.debug("forward message {}",gm);
        hub.putLine(gm.toString());
    }

    /**
     * Get a GridConnect Message from a CanMessage.
     * Enables override of the particular type of GridConnectMessage.
     * @param m the CanMessage
     * @return a GridConnectMessage.
     */
    protected GridConnectMessage getMessageFrom( CanMessage m ) {
        return new GridConnectMessage(m);
    }

    /**
     * Get an empty GridConnect Reply.
     * Enables override of the particular type of GridConnectReply.
     * @return a GridConnectReply.
     */
    protected GridConnectReply getBlankReply( ) {
        return new GridConnectReply();
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
            GridConnectMessage gm = getMessageFrom(new CanMessage(reply));
            log.debug("forward reply {} from JMRI, WRS={} items", gm, workingReplySet.size());
            hub.putLine(gm.toString());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HubPane.class);

}

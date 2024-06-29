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
import jmri.util.swing.JmriJOptionPane;
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
     * Sends with Line Endings.
     * @param port the port number to use.
     */
    public HubPane(int port) {
        this(port, true);
    }

    /**
     * Create a new HubPane with port number and default for sending line ends.
     * This option may subsequently be ignored by user preference.
     * Default is to NOT require line endings.
     * @param port the port number to use.
     * @param sendLineEndings if no user option is set, true to send line endings, else false.
     */
    public HubPane(int port, boolean sendLineEndings ) {
        super();
        userPreferencesManager = InstanceManager.getDefault(UserPreferencesManager.class);
        textArea = new java.awt.TextArea();
        _send_line_endings = getSendLineEndingsFromUserPref(sendLineEndings);
        hub = new Hub(port, sendLineEndings, getRequireLineEndingsFromUserPref()) {
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

    private final UserPreferencesManager userPreferencesManager;

    private static final String USER_SAVED = ".UserSaved"; // NOI18N
    private static final String USER_SEND_LINE_ENDINGS = ".SendLineTermination"; // NOI18N
    private static final String USER_REQUIRE_LINE_ENDINGS = ".RequireLineTermination"; // NOI18N
    private boolean _send_line_endings;

    /**
     * Get the Send line endings setting to use in the Hub.
     * @param defaultValue normally true for OpenLCB, false for CBUS.
     * @return the preference, else default value.
     */
    private boolean getSendLineEndingsFromUserPref( boolean defaultValue ){
        if ( userPreferencesManager.getSimplePreferenceState(getClass().getName() + USER_SAVED)) {
            // user has loaded before so use the preference
            return userPreferencesManager.getSimplePreferenceState(getClass().getName() + USER_SEND_LINE_ENDINGS);
        }
        return defaultValue;
    }

    /**
     * Get the Require line termination setting to use in the Hub.
     * @return the preference, default false.
     */
    private boolean getRequireLineEndingsFromUserPref(){
        return userPreferencesManager.getSimplePreferenceState(getClass().getName() + USER_REQUIRE_LINE_ENDINGS);
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
        textArea.append( System.lineSeparator() + Bundle.getMessage("SendLineTermination") // NOI18N
            +" : "+ _send_line_endings);
        textArea.append( System.lineSeparator() + Bundle.getMessage("RequireLineTermination") // NOI18N
            +" : "+ getRequireLineEndingsFromUserPref());
        addInetAddresses();

        // This hears OpenLCB traffic at packet level from traffic controller
        memo.getTrafficController().addCanListener(this);
    }

    private void addInetAddresses(){
        var t = jmri.util.ThreadingUtil.newThread(() -> {

                log.trace("start addInetAddresses");
                ZeroConfServiceManager manager = InstanceManager.getDefault(ZeroConfServiceManager.class);
                Set<InetAddress> addresses = manager.getAddresses(ZeroConfServiceManager.Protocol.All, true, true);
                for (InetAddress ha : addresses) {
    
                    var hostAddress = ha.getHostAddress();
                    var hostName = ha.getHostName();
                    var hostNameDup = !hostAddress.equals(hostName) ? hostName : "";
                    var isLoopBack = ha.isLoopbackAddress() ? " Loopback" : ""; // NOI18N
                    var isLinkLocal = ha.isLinkLocalAddress() ? " LinkLocal" : ""; // NOI18N
                    var port = String.valueOf(hub.getPort());
        
                    jmri.util.ThreadingUtil.runOnGUIEventually( () -> {
                        textArea.append( System.lineSeparator() + Bundle.getMessage(("IpAddressLine"), // NOI18N
                            hostNameDup, isLoopBack, isLinkLocal, hostAddress, port));
                        log.trace("    added a line");
                    });
                }
                log.trace("end addInetAddresses");
            },
            memo.getUserName() + " Hub Thread");
        t.start();    
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

    ArrayList<CanReply> workingReplySet = new ArrayList<>(); // collection of self-sent replies
    ArrayList<CanMessage> workingMessageSet = new ArrayList<>(); // collection of self-sent messages

    private ZeroConfService _zero_conf_service;
    protected String zero_conf_addr = "_openlcb-can._tcp.local.";

    protected void advertise(int port) {
        log.trace("start advertise");
        _zero_conf_service = ZeroConfService.create(zero_conf_addr, port);
        log.trace("start publish");
        _zero_conf_service.publish();
        log.trace("end publish and advertise");
        
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return Bundle.getMessage("HubControl", memo.getUserName()); // NOI18N
        }
        return "LCC / OpenLCB Hub Control";
    }

    /**
     * Creates a Menu List
     * <p>
     * Settings : Line Termination
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        menuList.add(getLineTerminationSettingsMenu());
        return menuList;
    }

    private JMenu getLineTerminationSettingsMenu() {
        JMenu menu = new JMenu(Bundle.getMessage("LineTermination")); // NOI18N
        JMenuItem sendLineFeedItem = new JMenuItem(Bundle.getMessage("SendLineTermination")); // NOI18N
        sendLineFeedItem.addActionListener(this::showSendTerminationDialog);
        menu.add(sendLineFeedItem);

        JMenuItem requireLineFeedItem = new JMenuItem(Bundle.getMessage("RequireLineTermination")); // NOI18N
        requireLineFeedItem.addActionListener(this::showRequireTerminationDialog);
        menu.add(requireLineFeedItem);

        return menu;
    }

    void showSendTerminationDialog(java.awt.event.ActionEvent e) {
        JCheckBox checkbox = new JCheckBox(Bundle.getMessage("SendLineTermination")); // NOI18N
        checkbox.setSelected(_send_line_endings);
        Object[] params = {Bundle.getMessage("LineTermSettingDialog"), checkbox }; // NOI18N
        int result = JmriJOptionPane.showConfirmDialog(this, 
            params,
            Bundle.getMessage("SendLineTermination"), // NOI18N
            JmriJOptionPane.OK_CANCEL_OPTION);
        if (result == JmriJOptionPane.OK_OPTION) {
            _send_line_endings = checkbox.isSelected();
            userPreferencesManager.setSimplePreferenceState(getClass().getName() + USER_SAVED, true); // NOI18N
            userPreferencesManager.setSimplePreferenceState(getClass().getName() + USER_SEND_LINE_ENDINGS, _send_line_endings); // NOI18N
        }
    }

    void showRequireTerminationDialog(java.awt.event.ActionEvent e) {
        JCheckBox checkbox = new JCheckBox(Bundle.getMessage("RequireLineTermination")); // NOI18N
        checkbox.setSelected(this.getRequireLineEndingsFromUserPref());
        Object[] params = {Bundle.getMessage("LineTermSettingDialog"), checkbox }; // NOI18N
        int result = JmriJOptionPane.showConfirmDialog(this, 
            params,
            Bundle.getMessage("RequireLineTermination"), // NOI18N
            JmriJOptionPane.OK_CANCEL_OPTION);
        if (result == JmriJOptionPane.OK_OPTION) {
            userPreferencesManager.setSimplePreferenceState(getClass().getName() + USER_REQUIRE_LINE_ENDINGS, checkbox.isSelected()); // NOI18N
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

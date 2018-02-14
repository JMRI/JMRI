package jmri.jmrit.withrottle;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.zeroconf.ZeroConfServiceEvent;
import jmri.util.zeroconf.ZeroConfServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserInterface.java Create a window for WiThrottle information, advertise
 * service, and create a thread for it to run in.
 * <p>
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 * @author Randall Wood Copyright (C) 2013
 * @author Paul Bender Copyright (C) 2018
 */
public class UserInterface extends JmriJFrame implements DeviceListener, RosterGroupSelector, ZeroConfServiceListener {

    private final static Logger log = LoggerFactory.getLogger(UserInterface.class);

    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel portLabel = new JLabel(Bundle.getMessage("LabelPending"));
    JLabel manualPortLabel = new JLabel();
    JLabel numConnected;
    JScrollPane scrollTable;
    JTable withrottlesList;
    WiThrottlesListModel withrottlesListModel;
    UserPreferencesManager userPreferences = InstanceManager.getDefault(UserPreferencesManager.class);
    String rosterGroupSelectorPreferencesName = this.getClass().getName() + ".rosterGroupSelector";
    RosterGroupComboBox rosterGroupSelector = new RosterGroupComboBox(userPreferences.getComboBoxLastSelection(rosterGroupSelectorPreferencesName));


    //keep a reference to the actual server
    private FacelessServer facelessServer;

    // Server iVars
    int port;
    boolean isListen;
    private ArrayList<DeviceServer> deviceList = new ArrayList<>();

    UserInterface() {
        super(false, false);

        facelessServer = (FacelessServer) InstanceManager.getOptionalDefault(DeviceManager.class).orElseGet(() -> {
                return InstanceManager.setDefault(DeviceManager.class, new FacelessServer());
        });

        port = facelessServer.getPort();
        if (log.isDebugEnabled()) {
            log.debug("WiThrottle listening on TCP port: " + port);
        }

        try {
           facelessServer.getZeroConfService().addEventListener(this);
        } catch( java.lang.NullPointerException npe) {
            //ZeroConfService may not exist yet
            log.debug("Unable to register for ZeroConf events");
        }

        // add ourselves as device listeners for any existing devices
        for(DeviceServer ds:facelessServer.getDeviceList()) {
           deviceList.add(ds);
           ds.addDeviceListener(this); 
        }

        facelessServer.addDeviceListener(this);

        createWindow();

    } // End of constructor

    protected void createWindow() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(MessageFormat.format(Bundle.getMessage("LabelAdvertising"), new Object[]{DeviceServer.getWiTVersion()}));
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 2;
        panel.add(label, con);

        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 2;
        panel.add(portLabel, con);

        con.gridy = 2;
        panel.add(manualPortLabel, con);

        numConnected = new JLabel(Bundle.getMessage("LabelClients") + " " + deviceList.size());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 2;
        con.ipadx = 5;
        con.gridwidth = 1;
        panel.add(numConnected, con);

        JPanel rgsPanel = new JPanel();
        rgsPanel.add(new JLabel(Bundle.getMessage("RosterGroupLabel")));
        rgsPanel.add(rosterGroupSelector);
        rgsPanel.setToolTipText(Bundle.getMessage("RosterGroupToolTip"));
        JToolBar withrottleToolBar = new JToolBar();
        withrottleToolBar.setFloatable(false);
        withrottleToolBar.add(new StopAllButton());
        withrottleToolBar.add(new LargePowerManagerButton());
        withrottleToolBar.add(rgsPanel);
        con.weightx = 0.5;
        con.ipadx = 0;
        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 2;
        panel.add(withrottleToolBar, con);
        /*
         JLabel vLabel = new JLabel("v"+DeviceServer.getWiTVersion());
         con.weightx = 0;
         con.gridx = 2;
         con.gridy = 3;
         panel.add(vLabel, con);
         */
        JLabel icon;
        java.net.URL imageURL = FileUtil.findURL("resources/IconForWiThrottle.gif");

        if (imageURL != null) {
            ImageIcon image = new ImageIcon(imageURL);
            icon = new JLabel(image);
            con.weightx = 0.5;
            con.gridx = 2;
            con.gridy = 0;
            con.ipady = 5;
            con.gridheight = 2;
            panel.add(icon, con);
        }

//  Add a list of connected devices and the address they are set to.
        withrottlesListModel = new WiThrottlesListModel(deviceList);
        withrottlesList = new JTable(withrottlesListModel);
        withrottlesList.setPreferredScrollableViewportSize(new Dimension(300, 80));

        withrottlesList.setRowHeight(20);
        scrollTable = new JScrollPane(withrottlesList);

        con.gridx = 0;
        con.gridy = 4;
        con.weighty = 1.0;
        con.ipadx = 10;
        con.ipady = 10;
        con.gridheight = 3;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.BOTH;
        panel.add(scrollTable, con);

//  Create the menu to use with WiThrottle window. Has to be before pack() for Windows.
        buildMenu();

//  Set window size & location
        this.setTitle("WiThrottle");
        this.pack();

        this.setResizable(true);
        Rectangle screenRect = new Rectangle(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());

//  Centers on top edge of screen
        this.setLocation((screenRect.width / 2) - (this.getWidth() / 2), 0);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);
        setMinimumSize(getSize());

        rosterGroupSelector.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                userPreferences.addComboBoxLastSelection(rosterGroupSelectorPreferencesName, s);
                facelessServer.setSelectedRosterGroup(s);
//              Send new selected roster group to all devices
                for (DeviceServer device : deviceList) {
                    device.sendPacketToDevice(device.sendRoster());
                }
            }
        });
    }

    protected void buildMenu() {
        this.setJMenuBar(new JMenuBar());

        JMenu menu = new JMenu(Bundle.getMessage("MenuMenu"));
        serverOnOff = new JMenuItem(Bundle.getMessage("MenuMenuStop"));
        serverOnOff.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (isListen) { // Stop server
                    disableServer();
                    serverOnOff.setText(Bundle.getMessage("MenuMenuStart"));
                    portLabel.setText(Bundle.getMessage("LabelNone"));
                    manualPortLabel.setText(null);
                } else { // Restart server
                    serverOnOff.setText(Bundle.getMessage("MenuMenuStop"));
                    isListen = true;

                    //createServerThread();
                }
            }
        });

        menu.add(serverOnOff);

        menu.add(new ControllerFilterAction());

        Action prefsAction = new apps.gui3.TabbedPreferencesAction(
                ResourceBundle.getBundle("apps.AppsBundle").getString("MenuItemPreferences"),
                "WITHROTTLE");

        menu.add(prefsAction);

        this.getJMenuBar().add(menu);

        // add help menu
        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
    }

    @Override
    public void notifyDeviceConnected(DeviceServer device) {

        deviceList.add(device);
        numConnected.setText(Bundle.getMessage("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        pack();
    }

    @Override
    public void notifyDeviceDisconnected(DeviceServer device) {
        if (deviceList.size() < 1) {
            return;
        }
        if (!deviceList.remove(device)) {
            return;
        }

        numConnected.setText(Bundle.getMessage("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        device.removeDeviceListener(this);
        pack();
    }

    @Override
    public void notifyDeviceAddressChanged(DeviceServer device) {
        withrottlesListModel.updateDeviceList(deviceList);
    }

    /**
     * Received an UDID, filter out any duplicate.
     * <p>
     * @param device the device to filter for
     */
    @Override
    public void notifyDeviceInfoChanged(DeviceServer device) {

        //  Filter duplicate connections
        if ((device.getUDID() != null) && (deviceList.size() > 0)) {
            for (int i = 0; i < deviceList.size(); i++) {
                DeviceServer listDevice = deviceList.get(i);
                if ((device != listDevice) && (listDevice.getUDID() != null) && (listDevice.getUDID().equals(device.getUDID()))) {
                    //  If in here, array contains duplicate of a device
                    log.debug("Has duplicate of device, clearing old one.");
                    listDevice.closeThrottles();
                    break;
                }
            }
        }
        withrottlesListModel.updateDeviceList(deviceList);
    }

    // this is package protected so tests can trigger easilly.
    void disableServer() {
        facelessServer.disableServer();
    }

    @Override
    public String getSelectedRosterGroup() {
        return rosterGroupSelector.getSelectedRosterGroup();
    }

    @Override
    public void serviceQueued(ZeroConfServiceEvent se) {
        this.portLabel.setText(Bundle.getMessage("LabelPending"));
        this.manualPortLabel.setText(null);
    }

    @Override
    public void servicePublished(ZeroConfServiceEvent se) {
        try {
            try {
                InetAddress addr = se.getDNS().getInetAddress();
                //output last good ipV4 address to the window to support manual entry
                if (addr instanceof Inet4Address) {
                    this.portLabel.setText(addr.getHostName());
                    this.manualPortLabel.setText(addr.getHostAddress() + ":" + port); // NOI18N
                    log.debug("Published IPv4 ZeroConf service for '{}' on {}:{}", se.getService().key(), addr.getHostAddress(), port); // NOI18N
                } else {
                    log.debug("Published IPv6 ZeroConf service for '{}' on {}:{}", se.getService().key(), addr.getHostAddress(), port); // NOI18N
                }
            } catch (NullPointerException | IOException ex) {
                log.error("Address is invalid: {}", ex.getLocalizedMessage());
                this.portLabel.setText(Inet4Address.getLocalHost().getHostName());
                this.manualPortLabel.setText(Inet4Address.getLocalHost().getHostAddress() + ":" + port); // NOI18N
            }
        } catch (UnknownHostException ex) {
            log.error("Failed to determine this system's IP address: {}", ex.getLocalizedMessage());
            this.portLabel.setText(Bundle.getMessage("LabelUnknown")); // NOI18N
            this.manualPortLabel.setText(null);
        }
    }

    @Override
    public void serviceUnpublished(ZeroConfServiceEvent se) {
        this.portLabel.setText(Bundle.getMessage("LabelNone"));
        this.manualPortLabel.setText(null);
    }

}

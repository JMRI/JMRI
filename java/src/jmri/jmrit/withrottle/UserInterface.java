package jmri.jmrit.withrottle;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
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
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.zeroconf.ZeroConfServiceManager;

/**
 * UserInterface.java Create a window for WiThrottle information and and create
 * a FacelessServer thread to handle jmdns and device requests
 * <p>
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 * @author Randall Wood Copyright (C) 2013
 * @author Paul Bender Copyright (C) 2018
 */
public class UserInterface extends JmriJFrame implements DeviceListener, RosterGroupSelector {

    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel portLabel = new JLabel(Bundle.getMessage("LabelPending"));
    JLabel manualPortLabel = new JLabel();
    String manualPortLabelString = ""; //append IPv4 addresses as they respond to zeroconf
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
    boolean isListen;
    private final ArrayList<DeviceServer> deviceList = new ArrayList<>();

    /**
     * Save the last known size and the last known location since 4.15.4.
     */
    UserInterface() {
        super(true, true);

        isListen = true;
        facelessServer = (FacelessServer) InstanceManager.getOptionalDefault(DeviceManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(DeviceManager.class, new FacelessServer());
        });

        // add ourselves as device listeners for any existing devices
        for (DeviceServer ds : facelessServer.getDeviceList()) {
            deviceList.add(ds);
            ds.addDeviceListener(this);
        }

        facelessServer.addDeviceListener(this);

        //update the server with the currently selected roster group
        facelessServer.setSelectedRosterGroup(rosterGroupSelector.getSelectedItem());

        //show all IPv4 addresses in window, for use by manual connections
        addIPAddressesToUI();

        createWindow();

    } // End of constructor

    private void addIPAddressesToUI() {
        //get port# directly from prefs
        int port = InstanceManager.getDefault(WiThrottlePreferences.class).getPort();
        //list IPv4 addresses on the UI, for manual connections
        //TODO: use some mechanism that is not tied to zeroconf networking
        StringBuilder as = new StringBuilder(); //build multiline string of valid addresses
        ZeroConfServiceManager manager = InstanceManager.getDefault(ZeroConfServiceManager.class);
        Set<InetAddress> addresses = manager.getAddresses(ZeroConfServiceManager.Protocol.IPv4, false, false);
        if (addresses.isEmpty()) {
            // include IPv6 and link-local addresses if no non-link-local IPv4 addresses are available
            addresses = manager.getAddresses(ZeroConfServiceManager.Protocol.All, true, false);
        }
        for (InetAddress ha : addresses) {
            this.portLabel.setText(ha.getHostName());
            as.append(ha.getHostAddress()).append(":").append(port).append("<br/>");
        }
        this.manualPortLabel.setText("<html>" + as + "</html>"); // NOI18N
    }

    protected void createWindow() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(MessageFormat.format(Bundle.getMessage("LabelListening"), new Object[]{DeviceServer.getWiTVersion()}));
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

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);
        setMinimumSize(new Dimension(400, 250));
        
        rosterGroupSelector.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                userPreferences.setComboBoxLastSelection(rosterGroupSelectorPreferencesName, s);
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
                if (isListen) { // Stop server, remove addresses from UI
                    disableServer();
                    serverOnOff.setText(Bundle.getMessage("MenuMenuStart"));
                    portLabel.setText(Bundle.getMessage("LabelNone"));
                    manualPortLabel.setText(null);
                } else { // Restart server
                    enableServer();
                    serverOnOff.setText(Bundle.getMessage("MenuMenuStop"));
                    addIPAddressesToUI();
                }
            }
        });

        menu.add(serverOnOff);

        menu.add(new ControllerFilterAction());

        Action prefsAction = new apps.gui3.tabbedpreferences.TabbedPreferencesAction(
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
    }

    @Override
    public void notifyDeviceAddressChanged(DeviceServer device) {
        withrottlesListModel.updateDeviceList(deviceList);
    }

    /**
     * Received an UDID, update the device list
     * <p>
     * @param device the device to update for
     */
    @Override
    public void notifyDeviceInfoChanged(DeviceServer device) {
        withrottlesListModel.updateDeviceList(deviceList);
    }

    // this is package protected so tests can trigger easily.
    void disableServer() {
        facelessServer.disableServer();
        isListen = false;
    }

    //tell the server thread to start listening again
    private void enableServer() {
        facelessServer.listen();
        isListen = true;
    }

    @Override
    public String getSelectedRosterGroup() {
        return rosterGroupSelector.getSelectedRosterGroup();
    }

    // private final static Logger log = LoggerFactory.getLogger(UserInterface.class);
}

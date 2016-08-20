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
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceEvent;
import jmri.util.zeroconf.ZeroConfServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserInterface.java Create a window for WiThrottle information, advertise
 * service, and create a thread for it to run in.
 *
 *	listen() has to run in a separate thread.
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
 */
public class UserInterface extends JmriJFrame implements DeviceListener, DeviceManager, ZeroConfServiceListener {

    private final static Logger log = LoggerFactory.getLogger(UserInterface.class.getName());
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel portLabel = new JLabel(rb.getString("LabelPending"));
    JLabel manualPortLabel = new JLabel();
    JLabel numConnected;
    JScrollPane scrollTable;
    JTable withrottlesList;
    WiThrottlesListModel withrottlesListModel;
    UserPreferencesManager userPreferences = InstanceManager.getDefault(UserPreferencesManager.class);
    String rosterGroupSelectorPreferencesName = this.getClass().getName() + ".rosterGroupSelector";
    RosterGroupComboBox rosterGroupSelector = new RosterGroupComboBox(userPreferences.getComboBoxLastSelection(rosterGroupSelectorPreferencesName));

    // Server iVars
    int port;
    ZeroConfService service;
    boolean isListen = true;
    ServerSocket socket = null;
    ArrayList<DeviceServer> deviceList;

    UserInterface() {
        super(false, false);
        if (deviceList == null) {
            deviceList = new ArrayList<DeviceServer>(1);
        }

        createWindow();

        setShutDownTask();
        createServerThread();
    }	//	End of constructor

    public void createServerThread() {
        ServerThread s = new ServerThread(this);
        s.setName("WiThrottleGUIServer"); // NOI18N
        s.start();
    }

    protected void createWindow() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(MessageFormat.format(rb.getString("LabelAdvertising"), new Object[]{DeviceServer.getWiTVersion()}));
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

        numConnected = new JLabel(rb.getString("LabelClients") + " " + deviceList.size());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 2;
        con.ipadx = 5;
        con.gridwidth = 1;
        panel.add(numConnected, con);

        JPanel rgsPanel = new JPanel();
        rgsPanel.add(new JLabel(rb.getString("RosterGroupLabel")));
        rgsPanel.add(rosterGroupSelector);
        rgsPanel.setToolTipText(rb.getString("RosterGroupToolTip"));
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
        con.gridwidth = 3;
        panel.add(scrollTable, con);

//  Create the menu to use with WiThrottle window. Has to be before pack() for Windows.
        buildMenu();

//  Set window size & location
        this.setTitle("WiThrottle");
        this.pack();

        this.setResizable(false);
        Rectangle screenRect = new Rectangle(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());

//  Centers on top edge of screen
        this.setLocation((screenRect.width / 2) - (this.getWidth() / 2), 0);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);

        rosterGroupSelector.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                userPreferences.addComboBoxLastSelection(rosterGroupSelectorPreferencesName, (String) ((JComboBox<String>) e.getSource()).getSelectedItem());
//              Send new selected roster group to all devices
                for (DeviceServer device : deviceList) {
                    device.sendPacketToDevice(device.sendRoster());
                }
            }
        });
    }

    protected void buildMenu() {
        this.setJMenuBar(new JMenuBar());

        JMenu menu = new JMenu(rb.getString("MenuMenu"));
        serverOnOff = new JMenuItem(rb.getString("MenuMenuStop"));
        serverOnOff.addActionListener(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 8264877902074382783L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (isListen) {	//	Stop server
                    disableServer();
                    serverOnOff.setText(rb.getString("MenuMenuStart"));
                    portLabel.setText(rb.getString("LabelNone"));
                    manualPortLabel.setText(null);
                } else {	//	Restart server
                    serverOnOff.setText(rb.getString("MenuMenuStop"));
                    isListen = true;

                    createServerThread();
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

    public void listen() {
        int socketPort = WiThrottleManager.withrottlePreferencesInstance().getPort();

        try {	//Create socket on available port
            socket = new ServerSocket(socketPort);
        } catch (IOException e1) {
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        if (log.isDebugEnabled()) {
            log.debug("WiThrottle listening on TCP port: " + port);
        }

        service = ZeroConfService.create("_withrottle._tcp.local.", port);
        service.addEventListener(this);
        service.publish();

        while (isListen) { //Create DeviceServer threads
            DeviceServer device;
            try {
                log.info("Creating new WiThrottle DeviceServer(socket) on port " + port + ", waiting for incoming connection...");
                device = new DeviceServer(socket.accept(), this);  //blocks here until a connection is made

                Thread t = new Thread(device);
                device.addDeviceListener(this);
                t.setName("WiThrottleUIDeviceServer"); // NOI18N
                log.debug("Starting WiThrottleUIDeviceServer thread");
                t.start();
            } catch (IOException e3) {
                if (isListen) {
                    log.error("Listen Failed on port " + port);
                }
                return;
            }

        }

    }

    @Override
    public void notifyDeviceConnected(DeviceServer device) {

        deviceList.add(device);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
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

        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
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
     *
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

//	Clear out the deviceList array and close each device thread
    private void stopDevices() {
        DeviceServer device;
        int cnt = 0;
        if (deviceList.size() > 0) {
            do {
                device = deviceList.get(0);
                if (device != null) {
                    device.closeThrottles(); //Tell device to stop its throttles, 
                    device.closeSocket();   //close its sockets
                    //close() will throw read error and it will be caught
                    //and drop the thread.
                    cnt++;
                    if (cnt > 200) {
                        break;
                    }
                }
            } while (!deviceList.isEmpty());
        }
        deviceList.clear();
        withrottlesListModel.updateDeviceList(deviceList);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());

    }

    private jmri.implementation.AbstractShutDownTask task = null;

    @Override
    protected void setShutDownTask() {
        if (jmri.InstanceManager.getOptionalDefault(jmri.ShutDownManager.class) != null) {
            task = new jmri.implementation.AbstractShutDownTask(getTitle()) {
                @Override
                public boolean execute() {
                    disableServer();
                    return true;
                }
            };
            jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(task);
        }
    }

    private void disableServer() {
        isListen = false;
        stopDevices();
        try {
            socket.close();
            log.debug("UI socket in ServerThread just closed");
            service.stop();
        } catch (IOException ex) {
            log.error("socket in ServerThread won't close");
        }
    }

    @Override
    public String getSelectedRosterGroup() {
        return rosterGroupSelector.getSelectedRosterGroup();
    }

    @Override
    public void serviceQueued(ZeroConfServiceEvent se) {
        this.portLabel.setText(rb.getString("LabelPending"));
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
            this.portLabel.setText(rb.getString("LabelUnknown")); // NOI18N
            this.manualPortLabel.setText(null);
        }
    }

    @Override
    public void serviceUnpublished(ZeroConfServiceEvent se) {
        this.portLabel.setText(rb.getString("LabelNone"));
        this.manualPortLabel.setText(null);
    }

    //  listen() has to run in a separate thread.
    static class ServerThread extends Thread {

        UserInterface UI;

        ServerThread(UserInterface _UI) {
            UI = _UI;
        }

        @Override
        public void run() {
            UI.listen();
            log.debug("Leaving serverThread.run()");
        }

    private final static Logger log = LoggerFactory.getLogger(ServerThread.class.getName());
    }
}

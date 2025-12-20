package jmri.jmrit.z21server;

import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * User interface.
 * Create a window with a menu bar and some controls:
 * - IP name, address and port number of the running Z21 Server
 * - a power on/off button
 * - an all locos off button
 * - menu entry for Z21 Server start/stop
 * - menu entry for opening a mapping table window for turnout numbers
 * - a table showing connected clients and their last used loco number
 * 
 * @author Jean-Yves Roda (C) 2023
 * @author Eckart Meyer (C) 2025 (enhancements, WlanMaus support)
 */

public class UserInterface extends JmriJFrame implements PropertyChangeListener {


    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel portLabel = new JLabel(Bundle.getMessage("LabelPending"));
    JLabel manualPortLabel = new JLabel();
    JLabel numConnected;
    JScrollPane scrollTable;
    JTable z21ClientsList;
    Z21ClientsListModel z21ClientsListModel;

    //keep a reference to the actual server
    private FacelessServer facelessServer;

    // Server iVars
    boolean isListen;

    /**
     * Save the last known size and the last known location since 4.15.4.
     */
    UserInterface() {
        super(true, true);

        isListen = true;
        facelessServer = FacelessServer.getInstance();
        
        ClientManager.getInstance().setClientListener(this);
        
        //show all IPv4 addresses in window, for use by manual connections
        addIPAddressesToUI();

        createWindow();

    } // End of constructor

    private void addIPAddressesToUI() {
        //get port# directly from prefs
        //int port = InstanceManager.getDefault(xxxPreferences.class).getPort();
        int port = MainServer.port;// currently fixed
        //list IPv4 addresses on the UI, for manual connections
        StringBuilder as = new StringBuilder(); //build multiline string of valid addresses
        try {
            // This code based on ReportContext.addNetworkInfo()
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress = inetAddress.getHostAddress();
                    if (!hostAddress.equals("0.0.0.0") && !hostAddress.regionMatches(0, "127", 0, 3) && !hostAddress.contains(":")) {
                        this.portLabel.setText(inetAddress.getHostName());
                        as.append(hostAddress).append(":").append(port).append("<br/>");
                    }
                }
            }
            this.manualPortLabel.setText("<html>" + as + "</html>"); // NOI18N

        } catch (SocketException ex) {
            log.warn("Unable to enumerate Network Interfaces: {}", ex.getMessage());
        }

    }


    protected void createWindow() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(Bundle.getMessage("LabelListening"));
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

        numConnected = new JLabel(Bundle.getMessage("LabelClients") + " " + ClientManager.getInstance().getRegisteredClients().size());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 2;
        con.ipadx = 5;
        con.gridwidth = 1;
        panel.add(numConnected, con);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(new StopAllButton());
        toolBar.add(new LargePowerManagerButton());
        con.weightx = 0.5;
        con.ipadx = 0;
        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 2;
        panel.add(toolBar, con);

        JLabel icon;
        java.net.URL imageURL = FileUtil.findURL("resources/z21appIcon.png");

        if (imageURL != null) {
            ImageIcon image = new ImageIcon(imageURL);
            image.setImage(image.getImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)); 
            icon = new JLabel(image);
            con.weightx = 0.5;
            con.gridx = 2;
            con.gridy = 0;
            con.ipady = 5;
            con.gridheight = 2;
            panel.add(icon, con);
        }

//  Add a list of connected devices and the address they are set to.
        z21ClientsListModel = new Z21ClientsListModel();
        z21ClientsList = new JTable(z21ClientsListModel);
        z21ClientsList.setPreferredScrollableViewportSize(new Dimension(300, 80));

        z21ClientsList.setRowHeight(20);
        scrollTable = new JScrollPane(z21ClientsList);

        con.gridx = 0;
        con.gridy = 4;
        con.weighty = 1.0;
        con.ipadx = 10;
        con.ipady = 10;
        con.gridheight = 3;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.BOTH;
        panel.add(scrollTable, con);

        
        //  Create the menu to use with the window. Has to be before pack() for Windows.
        buildMenu();

        //  Set window size & location
        this.setTitle("Z21 App Server");
        this.pack();

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);
        setMinimumSize(new Dimension(400, 100));

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
                    manualPortLabel.setText(null);
                } else { // Restart server
                    enableServer();
                    serverOnOff.setText(Bundle.getMessage("MenuMenuStop"));
                    String host = "";
                    try {
                        host = InetAddress.getLocalHost().getHostAddress();
                    } catch (Exception e) { host = "unknown ip"; }
                    manualPortLabel.setText("<html>" + host + "</html>");
                }
            }
        });

        menu.add(serverOnOff);

        menu.add(new NumberMapAction());

//        Action prefsAction = InstanceManager.getDefault(JmriPreferencesActionFactory.class).getCategorizedAction(
//                Bundle.getMessage("MenuMenuPrefs"),
//                "Z21 App Server");
//
//        menu.add(prefsAction);

        this.getJMenuBar().add(menu);

        // add help menu
        addHelpMenu("package.jmri.jmrit.z21server.z21server", true);
    }

    /**
     * Provide public access to the throttle list model for Jython
     */
    public Z21ClientsListModel getThrottleList() {
        return z21ClientsListModel;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        numConnected.setText(Bundle.getMessage("LabelClients") + " " + ClientManager.getInstance().getRegisteredClients().size());
        if (isListen) {
            addIPAddressesToUI();
        }
        else {
            portLabel.setText(Bundle.getMessage("LabelNone"));
            manualPortLabel.setText("");
        }
        z21ClientsListModel.updateClientList();
    }

    void disableServer() {
        facelessServer.stop();
        isListen = false;
        propertyChange(null);
        ClientManager.getInstance().handleExpiredClients(true);
    }

    private void enableServer() {
        facelessServer.start();
        isListen = true;
        propertyChange(null);
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserInterface.class);

}

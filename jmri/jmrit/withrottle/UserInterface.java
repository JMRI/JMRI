
package jmri.jmrit.withrottle;

//  JmriPlugin
//
//  WiThrottle

/**
 *	UserInterface.java
 *	Create a window for WiThrottle information, advertise service, and create a thread for it to run in.
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@version $Revision: 1.7 $
 */

import java.awt.event.*;
import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.jmdns.*;

import jmri.DccLocoAddress;
import jmri.util.JmriJFrame;
//import jmri.util.WindowMenu;


public class UserInterface extends JmriJFrame implements ActionListener, DeviceListener{

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserInterface.class.getName());
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    JMenuBar menuBar;
    JPanel panel;
    JButton button;
    JLabel portLabel = new JLabel(rb.getString("LabelPending"));
    JLabel numConnected;
    JScrollPane scrollTable;
    JTable withrottlesList;
    WiThrottlesListModel withrottlesListModel;

//	Server iVars
    int port;
    JmDNS jmdns;
    ServiceInfo serviceInfo;
    boolean isListen = true;
    ServerSocket socket = null;
    ArrayList<DeviceServer> deviceList;


    UserInterface(){
        if (deviceList == null) deviceList = new ArrayList<DeviceServer>(1);

        createWindow();

        try{
                jmdns = JmDNS.create();
        }catch (IOException e){
                log.error("JmDNS creation failed.");
        }

        createServerThread();
    }	//	End of constructor


    public void createServerThread(){
        ServerThread s = new ServerThread(this);
        s.start();
    }



    private void createWindow(){
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(rb.getString("LabelAdvertising"));
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 2;
        panel.add(label, con);

        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 2;
        panel.add(portLabel, con);

        numConnected = new JLabel(rb.getString("LabelClients") + " " + deviceList.size());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 2;
        con.ipadx = 5;
        con.gridwidth = 1;
        panel.add(numConnected, con);

        button = new JButton(rb.getString("ButtonStopServer"));
        con.weightx = 0.5;
        con.ipadx = 0;
        con.gridx = 1;
        con.gridy = 2;
        con.gridwidth = 1;
        panel.add(button, con);


        JLabel icon;
        java.net.URL imageURL = ClassLoader.getSystemResource("resources/IconForWiThrottle.gif");

        if (imageURL != null) {
            ImageIcon image = new ImageIcon(imageURL);
            icon = new JLabel(image);
            con.gridx = 2;
            con.gridy = 0;
            con.ipady = 5;
            con.gridheight = 2;
            panel.add(icon,con);
        }



        button.addActionListener(this);


//  Add a list of connected devices and the address they are set to.

        withrottlesListModel = new WiThrottlesListModel(deviceList);
        withrottlesList = new JTable(withrottlesListModel);
        withrottlesList.setPreferredScrollableViewportSize(new Dimension(300, 80));

        withrottlesList.setRowHeight(20);
        scrollTable = new JScrollPane(withrottlesList);


        con.gridx = 0;
        con.gridy = 3;
        con.weighty = 1.0;
        con.ipadx = 10;
        con.ipady = 10;
        con.gridheight = 3;
        con.gridwidth = 3;
        panel.add(scrollTable, con);

		
//  Create the menu to use with WiThrottle window. Has to be before pack() for Windows.

        this.setJMenuBar(new JMenuBar());

        // add help menu
        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
                
//  Set window size & location
        this.setTitle("WiThrottle");
        this.pack();

        this.setResizable(false);
        Rectangle screenRect = new Rectangle(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());

//  Centers on top edge of screen
        this.setLocation((screenRect.width/2) - (this.getWidth()/2), 0);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);

    }

    public void listen(){
        try{	//Create socket on available port
            socket = new ServerSocket(0);
        } catch(IOException e){
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        try{	//Start advertising Network Info
            String serverName;
            String sentName;
            try{
                serverName = java.net.InetAddress.getLocalHost().getHostName();
            }catch (IOException e) {
                serverName = "WiThrottle";
            }

//	Name string of ServiceInfo cannot have a '.' in it.
            int dotIndex = serverName.indexOf('.');
            if (dotIndex == -1) {	//	Has no dot
                sentName = serverName;
            }else if (dotIndex > 0) {	//	Has a dot, name will be up to dot
                sentName = serverName.substring(0, dotIndex);
            }else {	//	Give up, assign generic name
                log.warn("Setting default name for service discovery");
                sentName = "WiThrottle";
            }


            serviceInfo = ServiceInfo.create("_withrottle._tcp.local.",
                                            sentName,
                                            port,
                                            "path=index.html");

            jmdns.registerService(serviceInfo);
            portLabel.setText(serviceInfo.getName());
        } catch (IOException e){
            log.error("JmDNS Failure");
            return;
        }


        while (isListen){ //Create DeviceServer threads
            DeviceServer device;
            try{
                device = new DeviceServer(socket.accept());

                Thread t = new Thread(device);
                device.addDeviceListener(this);
                t.start();
            } catch (IOException e){
                if (isListen)log.error("Listen Failed on port " + port);
                return;
            }

        }


    }


    public void notifyDeviceConnected(DeviceServer device){

        deviceList.add(device);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        pack();
    }

    public void notifyDeviceDisconnected(DeviceServer device){
        if (deviceList.size()<1) return;
        if (!deviceList.remove(device)) return;

        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        device.removeDeviceListener(this);
        pack();
    }

    public void notifyDeviceAddressChanged(DccLocoAddress currentAddress){
        withrottlesListModel.updateDeviceList(deviceList);
    }



//	Button in WiThrottle window
    public void actionPerformed(ActionEvent event){
        Object eventSource = event.getSource();
        if ((eventSource == button)){
            if (isListen){	//	Stop server
                isListen = false;
                stopDevices();
                try{
                        socket.close();
                        log.debug("UI socket just closed");
                        jmdns.unregisterService(serviceInfo);
                } catch (IOException e){
                        log.error("socket in ThreadedServer won't close");
                        return;
                }

                button.setText(rb.getString("ButtonStartServer"));

                portLabel.setText(rb.getString("LabelNone"));
            }else{	//	Restart server
                button.setText(rb.getString("ButtonStopServer"));
                isListen = true;

                createServerThread();
            }
        }
    }

//	Clear out the deviceList array and close each device thread
    private void stopDevices(){
        DeviceServer device;
        int cnt = 0;
        if (deviceList.size()>0) do{
            device = deviceList.get(0);
            if (device != null){
                device.closeSocket();   //Tell device to stop its throttles, close its sockets
                                        //close() will throw read error and it will be caught
                                        //and drop the thread.
                cnt++;
                if (cnt>200){
                    break;
                }
            }
        }while (!deviceList.isEmpty());
        deviceList.clear();
        withrottlesListModel.updateDeviceList(deviceList);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());

    }

}




//	listen() has to run in a separate thread.
class ServerThread extends Thread {
    UserInterface UI;

    ServerThread(UserInterface _UI){
        UI = _UI;
    }

    public void run() {
        UI.listen();
        log.debug("Leaving serverThread.run()");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ServerThread.class.getName());
}

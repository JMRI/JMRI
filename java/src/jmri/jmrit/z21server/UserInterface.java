package jmri.jmrit.z21server;

import jmri.InstanceManager;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.prefs.JmriPreferencesActionFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

public class UserInterface extends JmriJFrame {


    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel manualPortLabel = new JLabel();

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
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { host = "unknown ip"; }

        this.manualPortLabel.setText("<html>" + host + "</html>"); // NOI18N

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

        JLabel label = new JLabel(Bundle.getMessage("LabelListening"));
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 2;
        panel.add(label, con);

        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 2;

        con.gridy = 2;
        panel.add(manualPortLabel, con);


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
            icon = new JLabel(image);
            con.weightx = 0.5;
            con.gridx = 2;
            con.gridy = 0;
            con.ipady = 5;
            con.gridheight = 2;
            panel.add(icon, con);
        }

        con.gridx = 0;
        con.gridy = 4;
        con.weighty = 1.0;
        con.ipadx = 10;
        con.ipady = 10;
        con.gridheight = 3;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.BOTH;

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

        //menu.add(new ControllerFilterAction());

        Action prefsAction = InstanceManager.getDefault(JmriPreferencesActionFactory.class).getCategorizedAction(
                Bundle.getMessage("MenuMenuPrefs"),
                "Z21 App Server");

        menu.add(prefsAction);

        this.getJMenuBar().add(menu);

        // add help menu
        addHelpMenu("package.jmri.jmrit.z21server.UserInterface", true);
    }


    void disableServer() {
        facelessServer.stop();
        isListen = false;
    }

    private void enableServer() {
        facelessServer.start();
        isListen = true;
    }

}

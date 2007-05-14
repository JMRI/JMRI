// ServerFrame.java

package jmri.jmrix.loconet.loconetovertcp;

import java.awt.event.*;
import javax.swing.*;
import jmri.util.JSpinnerUtil;
import jmri.util.SwingUtil;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Frame displaying and programming a LocoNet clock monitor.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003, 2004
 * @author      Alex Shepherd Copyright (C) 2006
 * @version	$Revision: 1.6 $
 */

public class ServerFrame extends jmri.util.JmriJFrame implements ServerListner {

  private ServerFrame() {
    super("LocoNetOverTcp Server");
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    portNumber = JSpinnerUtil.getJSpinner();
    if (portNumber != null) {
        JSpinnerUtil.setModelMaximum(portNumber, new Integer(65535));
        JSpinnerUtil.setModelMinimum(portNumber, new Integer(1024));
        JSpinnerUtil.setValue(portNumber, new Integer( 65535 ));
    } else {
        portNumber = new JTextField();
    }
    SwingUtil.setFocusable(portNumber,false);
    
    // add GUI items
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(autoStartCheckBox);
    panel.add( portNumberLabel ) ;
    panel.add(portNumber);
    getContentPane().add(panel);

    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(startButton);
    panel.add(stopButton);
    panel.add(saveButton);
    getContentPane().add(panel);

    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(serverStatus);
    panel.add(clientStatus);
    getContentPane().add(panel);

    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent a) {
        Server.getInstance().enable();
      }
    });

    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent a) {
        Server.getInstance().disable();
      }
    });

    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent a) {
        Server.getInstance().setAutoStart( autoStartCheckBox.isSelected() );
        Server.getInstance().setPortNumber( ((Integer)JSpinnerUtil.getValue( portNumber )).intValue() );
        Server.getInstance().saveSettings();
      }
    });

    autoStartCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent a) {
        saveButton.setEnabled( true );
      }
    });

    if(portNumber !=null)
      JSpinnerUtil.addChangeListener(portNumber, new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          saveButton.setEnabled( true );
        }
      });

    pack();
  }

  public void windowClosing(java.awt.event.WindowEvent e) {
    setVisible(false);
    self = null ;
    Server.getInstance().setStateListner(null);
    dispose();
    super.windowClosing(e);
  }

  public void dispose() {
    super.dispose();
  }

  static public synchronized ServerFrame getInstance(){
    if( self == null ) {
      self = new ServerFrame() ;
      Server server = Server.getInstance() ;
      server.setStateListner(self);
      server.updateServerStateListener();
      server.updateClinetStateListener();
    }

    return self ;
  }

  private void updateServerStatus(){
    Server server = Server.getInstance() ;
    autoStartCheckBox.setSelected( server.getAutoStart() );
    autoStartCheckBox.setEnabled( !server.isEnabled() );
    if (portNumber!=null) JSpinnerUtil.setValue( portNumber, new Integer( server.getPortNumber() ) ) ;
    portNumber.setEnabled( !server.isEnabled() );
    portNumberLabel.setEnabled( !server.isEnabled() );
    startButton.setEnabled( !server.isEnabled() );
    stopButton.setEnabled( server.isEnabled() );
    saveButton.setEnabled( server.isSettingChanged() );
    serverStatus.setText( "Server Status: " + (server.isEnabled() ? "Enabled" : "Disabled" ) );
  }


  private void updateClientStatus(){
    clientStatus.setText( "   Client Count: " + Integer.toString( Server.getInstance().getClientCount() ) );
  }

  public void notifyServerStateChanged( Server s ){
    javax.swing.SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          updateServerStatus() ;
        }}) ;
  }

  public void notifyClientStateChanged( Server s ) {
    javax.swing.SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          updateClientStatus() ;
        }}) ;
  }

  JComponent portNumber ;
  JLabel portNumberLabel = new JLabel("  Port Number: ");
  JLabel serverStatus = new JLabel("Server Status:         " );
  JLabel clientStatus = new JLabel("   Client Count:  " );

  JCheckBox autoStartCheckBox = new JCheckBox(
    "Start Server at Application Startup");
  JButton startButton = new JButton("Start Server");
  JButton stopButton = new JButton("Stop Server");
  JButton saveButton = new JButton("Save Settings");

  static ServerFrame self ;

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( ServerFrame.class.getName());
}

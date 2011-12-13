// NetworkPortFrame.java

package jmri.jmrix;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Abstract base Frame to open and configure a NetworkPortAdapeter 
 * @author			Paul Bender Copyright (C) 2009,
 * @author                      Bob Jacobsen Copyright (C) 2001-2008
 * @version			$Revision$
 */
abstract public class NetworkPortFrame extends jmri.util.JmriJFrame {

	protected javax.swing.JTextField hostField= new javax.swing.JTextField();
	protected javax.swing.JTextField portField = new javax.swing.JTextField();
	protected javax.swing.JComboBox opt1Box = new javax.swing.JComboBox();
	protected javax.swing.JComboBox opt2Box = new javax.swing.JComboBox();
	protected javax.swing.JButton openPortButton = new javax.swing.JButton();

	public NetworkPortFrame() {
		super();
	}
	public NetworkPortFrame(String name) {
		super(name);
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state
                   hostField.setText(adapter.getHostName());
		hostField.setVisible(true);
		hostField.setToolTipText("Enter host name or IP address");
		hostField.setAlignmentX(JLabel.LEFT_ALIGNMENT);

                hostField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adapter.setHostName(hostField.getText());
                    }
                });

                hostField.addKeyListener( new KeyListener() {
                    public void keyPressed(KeyEvent keyEvent) {
                    }
                    public void keyReleased(KeyEvent keyEvent) {
                       adapter.setHostName(hostField.getText());
                    }
                    public void keyTyped(KeyEvent keyEvent) {
                    }
                });

                portField.setText(""+adapter.getPort());
		portField.setVisible(true);
		portField.setToolTipText("Enter the network port to use");
		portField.setAlignmentX(JLabel.LEFT_ALIGNMENT);

                portField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       adapter.setPort(portField.getText());
                    }
                });

                portField.addKeyListener( new KeyListener() {
                    public void keyPressed(KeyEvent keyEvent) {
                    }
                    public void keyReleased(KeyEvent keyEvent) {
                       adapter.setPort(portField.getText());
                    }
                    public void keyTyped(KeyEvent keyEvent) {
                    }
                });

		String[] opt1List = adapter.validOption1();
		for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
		String[] opt2List = adapter.validOption2();
		for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

		openPortButton.setText("Open port");
		openPortButton.setToolTipText("Configure program to use selected port");
		openPortButton.setVisible(true);

		setLocation(new java.awt.Point(5, 40));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(hostField);
		getContentPane().add(portField);
		getContentPane().add(opt1Box);
		getContentPane().add(opt2Box);
		getContentPane().add(openPortButton);

		pack();

		openPortButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
	 				openPortButtonActionPerformed(evt);
				} catch (jmri.jmrix.NetworkConfigException ex) {
					log.error("Error while opening port.  Did you select the right one?\n"+ex);
				}
			  	catch (java.lang.UnsatisfiedLinkError ex) {
					log.error("Error while opening port.  Did you select the right one?\n"+ex);
				}
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});
	}

	/**
	 * Closing this window does nothing
	 */
	void thisWindowClosing(java.awt.event.WindowEvent e) {
	}

	/**
	 * This member does the protocol specific processing
	 */
	abstract public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.NetworkConfigException;

// Data members
	protected AbstractNetworkPortController adapter = null;

        static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkPortFrame.class.getName());

}

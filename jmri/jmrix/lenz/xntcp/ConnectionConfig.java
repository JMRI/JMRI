// ConnectionConfig.java

package jmri.jmrix.lenz.xntcp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrix.JmrixConfigPane;

/**
 * Handle configuring an XPressNet layout connection
 * via a XnTcp adapter.
 * <P>
 * This uses the {@link XnTcpAdapter} class to do the actual
 * connection.
 *
 * @author	Giorgio Terdina Copyright (C) 2008-2011, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version	$Revision: 1.15 $
 * GT - May 2008 - Added possibility of manually defining the IP address and the TCP port number
 * GT - May 2011 - Fixed problems arising from recent refactoring
 *
 * @see XnTcpAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {
	javax.swing.JComboBox choiceBox = new javax.swing.JComboBox();

	private boolean manualInput = false;
	private boolean init = false;
	private String oldName;


    /**
     * Local initialization of defaults, to be called from all constructors
     */
    private void initDefaults() {
		hostNameField = new JTextField(XnTcpAdapter.DEFAULT_IP_ADDRESS);
		portField = new JTextField(String.valueOf(XnTcpAdapter.DEFAULT_TCP_PORT));
    }

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        initDefaults();
        adapter = p;
		String h = adapter.getHostName();
		if(h != null && !h.equals(JmrixConfigPane.NONE)) hostNameField = new JTextField(h);
		String t = adapter.getCurrentPortName();
		if(!t.equals("0")) portField = new JTextField(t);
		oldName = adapter.getCurrentOption1Setting();
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        initDefaults();
    }

    public String name() { return "XnTcp"; }


		@Override
    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        choiceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				enableInput();
            }
        });
        hostNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(manualInput) adapter.setHostName(hostNameField.getText());
            }
        });
        hostNameField.addKeyListener( new KeyListener() {
             public void keyPressed(KeyEvent keyEvent) {
             }
             public void keyReleased(KeyEvent keyEvent) {
                if(manualInput) adapter.setHostName(hostNameField.getText());
             }
             public void keyTyped(KeyEvent keyEvent) {
             }
         });
		portField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if(manualInput) adapter.setPort(portField.getText());
             }
        });
        portField.addKeyListener( new KeyListener() {
             public void keyPressed(KeyEvent keyEvent) {
             }
             public void keyReleased(KeyEvent keyEvent) {
                if(manualInput) adapter.setPort(portField.getText());
             }
             public void keyTyped(KeyEvent keyEvent) {
             }
         });
		
        init = true;
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
		@Override
    protected void setInstance() { if(adapter==null) adapter = new XnTcpAdapter(); }

		@Override
    public String getInfo() {
        String t = (String)choiceBox.getSelectedItem();
        if (t==null) return JmrixConfigPane.NONE;
		if (!t.equals("Manual") || adapter == null ) return t;
		return adapter.getHostName();
		
    }

		@Override
    public void loadDetails(JPanel details) {
    	
        setInstance();

        Vector<String> v;
		v = ((XnTcpAdapter)adapter).getInterfaceNames();
		if (log.isDebugEnabled()) {
			log.debug("loadDetails called in class "+this.getClass().getName());
			log.debug("adapter class: "+adapter.getClass().getName());
			log.debug("loadDetails called for "+name());
			log.debug("Found "+v.size()+" ports");
		}
        choiceBox.removeAllItems();
		int indSel = -1;
		if(oldName == null) oldName = JmrixConfigPane.NONE;
        for (int i=0; i<v.size(); i++) {
			if(v.elementAt(i).equals(oldName)) indSel = i;
			choiceBox.addItem(v.elementAt(i));
        }
        choiceBox.addItem("Manual");
		if(indSel < 0) indSel = v.size();
		choiceBox.setSelectedIndex(indSel);

		details.setLayout(new GridLayout(3,2));
        details.add(new JLabel("XnTcp Interface: "));
        details.add(choiceBox);
        details.add(new JLabel("IP address: "));
		details.add(hostNameField);
		details.add(new JLabel("Port number: "));
		details.add(portField);
 
		enableInput();
		
        checkInitDone();
    }
	
	private void enableInput() {
		String choice = (String)choiceBox.getSelectedItem();
		manualInput = choice.equals("Manual");
		hostNameField.setEnabled(manualInput);
		portField.setEnabled(manualInput);
		adapter.configureOption1(choice);
		adapter.setHostName(hostNameField.getText());
		adapter.setPort(portField.getText());
	}

    String manufacturerName = jmri.jmrix.DCCManufacturerList.LENZ;

		@Override
    public String getManufacturer() { return manufacturerName; }
		@Override
    public void setManufacturer(String manu) { manufacturerName=manu; }



static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfig.class.getName());

}

// ConnectionConfig.java

package jmri.jmrix.lenz.xntcp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Handle configuring an XPressNet layout connection
 * via a XnTcp adapter.
 * <P>
 * This uses the {@link XnTcpAdapter} class to do the actual
 * connection.
 *
 * @author	Giorgio Terdina Copyright (C) 2008, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version	$Revision: 1.2 $
 * GT - May 2008 - Added possibility of manually defining the IP address and the TCP port number
 *
 * @see XnTcpAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {


    protected JComboBox portBox = new JComboBox();
    protected jmri.jmrix.SerialPortAdapter adapter = null;
	protected JTextField ipField = new JTextField(XnTcpAdapter.DEFAULT_IP_ADDRESS);
	protected JTextField portField = new JTextField(String.valueOf(XnTcpAdapter.DEFAULT_TCP_PORT));
	protected boolean manualInput = false;
	private boolean init = false;

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
		String h = adapter.getCurrentOption1Setting();
		if(h != null && !h.equals("")) ipField = new JTextField(h);
		String t = adapter.getCurrentOption2Setting();
		if(t != null && !t.equals("")) portField = new JTextField(t);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
    }

    public String name() { return "XnTcp"; }


    void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        portBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				enableInput();
            }
        });
        ipField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(manualInput) adapter.configureOption1(ipField.getText());
            }
        });
		portField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if(manualInput) adapter.configureOption2(portField.getText());
             }
        });
		
        init = true;
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
    protected void setInstance() { adapter = XnTcpAdapter.instance(); }

    public String getInfo() {
        String t = (String)portBox.getSelectedItem();
        if (t!=null) return t;
        else return "(none)";
    }

    public void loadDetails(JPanel details) {
    	
        setInstance();

        Vector v;
        try {
            v = adapter.getPortNames();
    	    if (log.isDebugEnabled()) {
    		    log.debug("loadDetails called in class "+this.getClass().getName());
    		    log.debug("adapter class: "+adapter.getClass().getName());
    		    log.debug("loadDetails called for "+name());
        	    log.debug("Found "+v.size()+" ports");
            }
        } catch (java.lang.UnsatisfiedLinkError e1) {
            log.error("UnsatisfiedLinkError - the javax.comm library has not been installed properly");
            log.error("java.library.path="+System.getProperty("java.library.path","<unknown>"));
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to load comm library.\nYou have to fix that before setting preferences.");
            return;
        }
        portBox.removeAllItems();
		String oldName = adapter.getCurrentPortName();
		int indSel = -1;
		if(oldName == null) oldName = new String("(none)");
        for (int i=0; i<v.size(); i++) {
			if(((String)v.elementAt(i)).equals(oldName)) indSel = i;
			portBox.addItem(v.elementAt(i));
        }
        portBox.addItem("Manual");
		if(indSel < 0) indSel = v.size();
		portBox.setSelectedIndex(indSel);

		details.setLayout(new GridLayout(3,2));
        details.add(new JLabel("XnTcp Interface: "));
        details.add(portBox);
        details.add(new JLabel("IP address: "));
		details.add(ipField);
		details.add(new JLabel("Port number: "));
		details.add(portField);
 
		enableInput();
		
        checkInitDone();
    }
	
	private void enableInput() {
		String choice = (String)portBox.getSelectedItem();
		manualInput = choice.equals("Manual");
		ipField.setEnabled(manualInput);
		portField.setEnabled(manualInput);
		adapter.setPort(choice);
		if(manualInput) {
			adapter.configureOption1(ipField.getText());
			adapter.configureOption2(portField.getText());
		} else {
			adapter.configureOption1("");
			adapter.configureOption2("");
		}
	}
}

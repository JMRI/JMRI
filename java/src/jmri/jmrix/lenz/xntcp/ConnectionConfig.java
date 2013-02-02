// ConnectionConfig.java

package jmri.jmrix.lenz.xntcp;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import jmri.jmrix.JmrixConfigPane;

/**
 * Handle configuring an XPressNet layout connection
 * via a XnTcp adapter.
 * <P>
 * This uses the {@link XnTcpAdapter} class to do the actual
 * connection.
 *
 * @author	Giorgio Terdina Copyright (C) 2008-2011, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version	$Revision$
 * GT - May 2008 - Added possibility of manually defining the IP address and the TCP port number
 * GT - May 2011 - Fixed problems arising from recent refactoring
 * GT - Dec 2011 - Fixed problems in 2.14 arising from changes introduced since May
 *
 * @see XnTcpAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

	private boolean manualInput = false;
	@SuppressWarnings("unused")
	private String oldName;

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);

		String h = adapter.getHostName();
		if(h != null && !h.equals(JmrixConfigPane.NONE)) hostNameField = new JTextField(h);
		String t = ""+adapter.getPort();
		if(!t.equals("0")) portField = new JTextField(t);
		oldName = adapter.getHostName();
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
	super();
    }

    public String name() { return "XnTcp"; }


    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
		@Override
    protected void setInstance() { if(adapter==null) adapter = new XnTcpAdapter(); }

		@Override
    public String getInfo() {
// GT 2.14 retrieving adapter name from CurrentOption1Setting, since Opt1Box now returns null
		String x = adapter.getOptionState("XnTcpInterface");
        if (x==null)  return JmrixConfigPane.NONE;
		if (x.equals("Manual")) {
			x = "";
		} else {
			x+= ":";
		}
        String t = adapter.getHostName();
        int p = adapter.getPort();
        if (t != null && !t.equals("")) {
            if (p!=0){
                return x+t+":"+p;
            }
            return x+t;
        }
        else return JmrixConfigPane.NONE;
    }

		@Override
    public void loadDetails(final JPanel d) {
		super.loadDetails(d);

        if(options.get("XnTcpInterface").getComponent() instanceof JComboBox){
            ((JComboBox)options.get("XnTcpInterface").getComponent()).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    enableInput();
                }
            });
        }
	}
    protected void showAdvancedItems(){
        super.showAdvancedItems();
        enableInput();
        _details.repaint();
    }
	
	private void enableInput() {
		String choice = options.get("XnTcpInterface").getItem();
//GT 2.14 - Added test for null, now returned by opt1Box at startup (somewhere the initialization is missing)
		if(choice != null) {
			manualInput = choice.equals("Manual");
		} else {
			manualInput = false;
		}
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


    public boolean isHostNameAdvanced(){ return true;}


static Logger log = Logger.getLogger(ConnectionConfig.class.getName());

}

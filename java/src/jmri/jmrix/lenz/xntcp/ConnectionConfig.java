// ConnectionConfig.java

package jmri.jmrix.lenz.xntcp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener;

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
		String x = adapter.getCurrentOption1Setting();
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
		opt1Box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableInput();
			}
		});
	}

    protected void showAdvancedItems(){
        _details.removeAll();
        int stdrows = 1;
        boolean incAdvancedOptions=true;
        if(!isPortAdvanced()) stdrows++;
        if(!isHostNameAdvanced()) stdrows++;
        if ((!isOptList1Advanced())&&(opt1List.length>=1)) stdrows++;
        if ((!isOptList2Advanced())&&(opt2List.length>1)) stdrows++;
        if(adapter.getSystemConnectionMemo()!=null) stdrows=stdrows+2;
        if (stdrows == NUMOPTIONS){
            incAdvancedOptions=false;
        } else{
            stdrows++;
        }
        if (showAdvanced.isSelected()) {
            int advrows = stdrows;
            if(isPortAdvanced()) advrows++;
            if(isHostNameAdvanced()) advrows++;
            if ((isOptList1Advanced())&&(opt1List.length>=1)) advrows++;
            if ((isOptList2Advanced())&&(opt2List.length>1)) advrows++;
            _details.setLayout(new GridLayout(advrows,2));
            addStandardDetails(incAdvancedOptions);

            if(isHostNameAdvanced()){
                _details.add(hostNameFieldLabel);
                _details.add(hostNameField);
            }

            if(isPortAdvanced()){
                _details.add(portFieldLabel);
                _details.add(portField);
            }
            if ((isOptList1Advanced())&&(opt1List.length>=1)) {
                _details.add(opt1BoxLabel = new JLabel(adapter.option1Name()));
                _details.add(opt1Box);
            }
            if ((isOptList2Advanced())&&(opt2List.length>1)) {
                _details.add(opt2BoxLabel = new JLabel(adapter.option2Name()));
                _details.add(opt2Box);
            }
        } else {
            _details.setLayout(new GridLayout(stdrows,2));
            addStandardDetails(incAdvancedOptions);
        }
        _details.validate();
        if (_details.getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).pack();
        }
        enableInput();
        _details.repaint();
    }
	
    @Override
    protected void addStandardDetails(boolean incAdvanced){
        if(!isHostNameAdvanced()){
            _details.add(hostNameFieldLabel);
            _details.add(hostNameField);
        }

        if(!isPortAdvanced()){
            _details.add(portFieldLabel);
            _details.add(portField);
        }

        if ((!isOptList1Advanced())&&(opt1List.length>=1)){
            _details.add(opt1BoxLabel = new JLabel(adapter.option1Name()));
            _details.add(opt1Box);
        }

        if ((!isOptList2Advanced())&&(opt2List.length>1)) {
            _details.add(opt2BoxLabel);
            _details.add(opt2Box);
        }
        if(adapter.getSystemConnectionMemo()!=null){
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
        }

        if (incAdvanced){
            _details.add(new JLabel(" "));
            _details.add(showAdvanced);
        }
    }


	private void enableInput() {
		String choice = (String)opt1Box.getSelectedItem();
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


static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfig.class.getName());

}

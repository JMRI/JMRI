package jmri.jmrix.bidib.netbidib;

import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrix.PortAdapter;
import jmri.jmrix.bidib.BiDiBConstants;
import jmri.util.ThreadingUtil;
import java.util.Map.Entry;
        
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.netbidib.client.NetBidibClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring a netBiDiB layout
 * connection via a NetBiDiBAdapter object.
 *
 * @author Eckart Meyer Copyright (C) 2024
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig implements ActionListener {

    public final static String NAME = "netBiDiB"; //text to show in connection type ComboBox
    
    private final JComboBox<String> deviceListField = new JComboBox<>();
    private final JLabel deviceListFieldLabel = new JLabel(Bundle.getMessage("NetBiDiBConnectionAvailableDevices"));
    private final JButton deviceListRefreshButton = new JButton("Refresh");
    private final JButton pairingButton = new JButton("Pairing");
    private final JButton logoffButton = new JButton("Logoff");

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     * @param p network port adapter.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
        log.info("NetworkPortAdapter opening.");
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetBiDiBAdapter();
            adapter.setPort(NetBidibClient.NET_BIDIB_PORT_NUMBER);
            adapter.setHostName(BiDiBConstants.BIDIB_OVER_TCP_DEFAULT_HOST);
        }
    }
    
    /**
     * fill the device list combo box if autoconfig is enabled
     */
    private void getDeviceListData() {
        if (adapter.getMdnsConfigure()) {
            adapter.autoConfigure(); //get new data
        }
        else {
            //((NetBiDiBAdapter)adapter).deviceListAddFromPairingStore(); //get at least data from pairing store
        }
        // get the data from netBiDiB adapter
        Map<Long, String> devlist =  ((NetBiDiBAdapter)adapter).getDeviceListEntries();
        deviceListField.setEnabled(false); //signal the combo box action listener to do nothing while filling
        deviceListField.removeAllItems();
        for ( Entry<Long, String> entry: devlist.entrySet()) { //don't use keySet - CI Tests doesn't like it :-(
            Long uid = entry.getKey();
            log.trace("get device list entry for uid {}: [{}]", ByteUtils.getUniqueIdAsString(uid), devlist.get(uid));
            deviceListField.addItem(devlist.get(uid));
        }
        deviceListField.setEnabled(true); //re-enable the combo box
        // get the current unique id if available
        String item = devlist.get(((NetBiDiBAdapter)adapter).getUniqueId());
        if (item != null) {
            // it is available - select the correspondent entry in the combo box
            // this will trigger the selection event, which in turn fills other fields
            deviceListField.setSelectedItem(item);
        }
        else {
            // no current uid available - just select the first entry (if there are entries at all)
            // this will trigger the selection event, which in turn fills other fields
            if (devlist.size() > 0) {
                deviceListField.setSelectedIndex(0);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        log.trace("load Details");

        super.loadDetails(details);
        
        // add a listener to the traffic controller so we will be informed if something has changed
        ((NetBiDiBAdapter)adapter).addConnectionChangedListener(this);
        
//        ((NetBiDiBAdapter)adapter).addConnectionChangedListener((ActionEvent e) -> {
//            log.debug("Update connection panel {}", e.paramString());
//            ThreadingUtil.runOnGUIEventually(() -> {
//                // We are probably called from an event thread.
//                // Be sure to update the GUI on the GUI thread.
//                log.trace("update GUI");
//                getDeviceListData(); //update data
//                showAdvancedItems(); //and refresh the panel
//            });
//        });
        
        // change label for port
        portFieldLabel.setText("TCP Port");
        
        // remove output delay since it is not used by BiDiB
        outputIntervalLabel.setVisible(false);
        outputIntervalSpinner.setVisible(false);
        outputIntervalReset.setVisible(false);
        
        getDeviceListData(); //get current data

        // add listeners to our extra components\

        // Device list Combo-Box
        deviceListField.addActionListener((ActionEvent e) -> {
            log.trace("devlist selection event {}", e.paramString());
            
            if (deviceListField.isEnabled()  &&  deviceListField.getSelectedIndex() >= 0) {
                log.debug("device list item selected: [{}] {}", deviceListField.getSelectedIndex(), deviceListField.getSelectedItem());
                ((NetBiDiBAdapter)adapter).selectDeviceListItem(deviceListField.getSelectedIndex());

                hostNameField.setText(adapter.getHostName());
                portField.setText(String.valueOf(adapter.getPort()));
                log.debug("selected Unique UID: {},adName field: {}", adapter.getAdvertisementName(), adNameField.getText());
                adNameField.setText(adapter.getAdvertisementName());
            }
        });
        
        // Device list refresh button
        deviceListRefreshButton.addActionListener((ActionEvent e) -> {
            log.trace("devlist refresh event {}", e.paramString());
            getDeviceListData(); //update data
            showAdvancedItems(); //and refresh the panel
        });
        
        // Pairing button
        pairingButton.addActionListener((ActionEvent e) -> {
            log.trace("pairing button event {}", e.paramString());
            NetBiDiBAdapter a = (NetBiDiBAdapter)adapter;
            a.setPaired(!a.isConnectionReady(), (ActionEvent pe) -> {
                log.trace("pairing action finished event {}", pe.paramString());
                getDeviceListData(); //update data
                showAdvancedItems(); //and refresh the panel
            });
        });
        
        // Logon/Logoff button
        logoffButton.addActionListener((ActionEvent e) -> {
            log.trace("logoff button event {}", e.paramString());
            NetBiDiBAdapter a = (NetBiDiBAdapter)adapter;
            a.setLogon(a.isDetached());
            showAdvancedItems(); //and refresh the panel
        });
        
    }
    
    /**
     * connection changed action event
     * 
     * @param e - Action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Update connection panel {}", e.paramString());
        ThreadingUtil.runOnGUIEventually(() -> {
            // We are probably called from an event thread.
            // Be sure to update the GUI on the GUI thread.
            log.trace("update GUI");
            getDeviceListData(); //update data
            showAdvancedItems(); //and refresh the panel
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",justification = "Cast safe by design") //parameter adapter always is a NetBiDiBAdapter here
    public int addStandardDetails(PortAdapter adapter, boolean incAdvanced, int i) {

        log.trace("add Details to JPanel");
        
        if (showAutoConfig.isSelected()) {
            // the device list combo box should appear first, this is before super.addStandardDetails()
            cR.gridy = i;
            cL.gridy = i;
            JPanel deviceListPanel = new JPanel();
            deviceListPanel.add(deviceListField);
            deviceListPanel.add(deviceListRefreshButton);
            gbLayout.setConstraints(deviceListFieldLabel, cL);
            gbLayout.setConstraints(deviceListPanel, cR);
            _details.add(deviceListFieldLabel);
            _details.add(deviceListPanel);
            i++;
        }

        i = super.addStandardDetails(adapter, incAdvanced, i);

        //boolean connectionIsReady = ((NetBiDiBAdapter)adapter).isConnectionReady();
        boolean connectionIsOpened = ((NetBiDiBAdapter)adapter).isOpened();
        if (showAdvanced.isSelected()  ||  !connectionIsOpened) {
            if (connectionIsOpened) {
                pairingButton.setText(Bundle.getMessage("netBiDiBPairingButtonUnpair"));
            }
            else {
                pairingButton.setText(Bundle.getMessage("netBiDiBPairingButtonPair"));
            }
            cR.gridy = i;
            cL.gridy = i;
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(pairingButton);
            buttonPanel.add(logoffButton);
            gbLayout.setConstraints(buttonPanel, cR);
            _details.add(buttonPanel);
            i++;
        }

        logoffButton.setEnabled(connectionIsOpened);
        if (((NetBiDiBAdapter)adapter).isDetached()) {
            logoffButton.setText(Bundle.getMessage("netBiDiBLogoffButtonLogon"));
        }
        else {
            logoffButton.setText(Bundle.getMessage("netBiDiBLogoffButtonLogoff"));
        }
        
        return i;
    }


    /**
     * Actions to be done if network autoconfig has changed.
     * 
     * If autoconfig is set, disable input to the hostname field - it is then filled
     * from the device list combo box
     */
    @Override
    public void setAutoNetworkConfig() {
        log.trace("setAutoNetworkConfig: {}", showAutoConfig.isSelected());
        super.setAutoNetworkConfig();
        hostNameField.setEnabled(!showAutoConfig.isSelected());
        hostNameFieldLabel.setEnabled(!showAutoConfig.isSelected());
        getDeviceListData(); //update data
        showAdvancedItems(); //and refresh the panel
    }

    @Override
    public boolean isHostNameAdvanced() {
        return showAutoConfig.isSelected();
    }

    @Override
    public boolean isAutoConfigPossible() {
        return true;
    }
    
    @Override
    public void dispose() {
        ((NetBiDiBAdapter)adapter).removeConnectionChangedListener(this); //just in case - I think this will never happen
        super.dispose();
    }
    

    private static final Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

}

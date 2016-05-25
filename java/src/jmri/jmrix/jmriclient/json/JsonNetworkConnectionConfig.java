package jmri.jmrix.jmriclient.json;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import javax.jmdns.ServiceInfo;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import jmri.jmris.json.JSON;
import jmri.jmrix.AbstractNetworkConnectionConfig;
import jmri.jmrix.NetworkPortAdapter;
import jmri.util.zeroconf.ZeroConfClient;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkConnectionConfig extends AbstractNetworkConnectionConfig {

    protected ZeroConfClient zeroConfClient = new ZeroConfClient();
    protected JTextField transmitPrefixField = new JTextField(15);
    protected JLabel transmitPrefixLabel = new JLabel(Bundle.getMessage("ServerConnectionPrefix")); // NOI18N
    protected JLabel nodeIdentityField = new JLabel();
    protected JLabel nodeIdentityLabel = new JLabel(Bundle.getMessage("NodeIdentityLabel")); // NOI18N

    /**
     * Constructor for an object being created while loading existing
     * preferences.
     *
     * @param portAdapter
     */
    public JsonNetworkConnectionConfig(NetworkPortAdapter portAdapter) {
        super(portAdapter);
        this.zeroConfClient.startServiceListener(JSON.ZEROCONF_SERVICE_TYPE);
        this.systemPrefixField.setColumns(15);
    }

    /**
     * Constructor for a functional Swing object with no preexisting adapter.
     */
    public JsonNetworkConnectionConfig() {
        super();
        this.zeroConfClient.startServiceListener(JSON.ZEROCONF_SERVICE_TYPE);
        this.systemPrefixField.setColumns(15);
    }

    @Override
    protected void setInstance() {
        if (this.adapter == null) {
            this.adapter = new JsonNetworkPortController();
        }
    }

    @Override
    public String name() {
        return Bundle.getMessage("JsonNetworkConnection"); // NOI18N
    }

    @Override
    public boolean isPortAdvanced() {
        return true;
    }

    @Override
    public boolean isAutoConfigPossible() {
        return false; // temporarilly disable
    }

    @Override
    protected void checkInitDone() {
        super.checkInitDone();
        // temporarilly disable -- the lookup triggered by getServices causes an unacceptable delay in openning the preferences window
        //JComboBox services = new JComboBox(this.getServices());
        //options.put(Bundle.getMessage("DiscoveredServices"), new Option(Bundle.getMessage("DiscoveredServices"), services, false));
        if (this.adapter.getSystemConnectionMemo() != null) {
            final JsonClientSystemConnectionMemo memo = (JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo();
            this.transmitPrefixField.setText(memo.getTransmitPrefix());
            this.transmitPrefixField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    memo.setTransmitPrefix(transmitPrefixField.getText());
                    transmitPrefixField.setText(memo.getTransmitPrefix());
                }
            });
            this.transmitPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    memo.setTransmitPrefix(transmitPrefixField.getText());
                    transmitPrefixField.setText(memo.getTransmitPrefix());
                }

                @Override
                public void focusGained(FocusEvent e) {
                    // nothing to do
                }
            });
            this.nodeIdentityField.setText(memo.getNodeIdentity());
            this.nodeIdentityField.setToolTipText(Bundle.getMessage("NodeIdentityToolTip"));
        }
    }

    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems(); // we're adding to the normal advanced items.
        if (this.adapter.getSystemConnectionMemo() != null
                && showAdvanced.isSelected()) {
            this.cR.gridy += 2;
            this.cL.gridy += 2;
            this.gbLayout.setConstraints(transmitPrefixLabel, cL);
            this.gbLayout.setConstraints(transmitPrefixField, cR);
            this._details.add(transmitPrefixLabel);
            this._details.add(transmitPrefixField);
            this.cR.gridy += 2;
            this.cL.gridy += 2;
            this.gbLayout.setConstraints(nodeIdentityLabel, cL);
            this.gbLayout.setConstraints(nodeIdentityField, cR);
            this._details.add(nodeIdentityLabel);
            this._details.add(nodeIdentityField);
        }
        if (this._details.getParent() != null && this._details.getParent() instanceof JViewport) {
            JViewport vp = (JViewport) _details.getParent();
            vp.revalidate();
            vp.repaint();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter(); // we're adding more details to the connection.
        if (this.adapter.getSystemConnectionMemo() != null) {
            final JsonClientSystemConnectionMemo memo = (JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo();
            memo.setTransmitPrefix(this.transmitPrefixField.getText());
        }
    }

    @SuppressWarnings("unchecked")
    public Object[] getServices() {
        ArrayList<String> services = new ArrayList<String>();
        for (ServiceInfo service : this.zeroConfClient.getServices(JSON.ZEROCONF_SERVICE_TYPE)) {
            services.add(String.format(Bundle.getMessage("ZeroConfServiceDescription"), service.getQualifiedName(), service.getPropertyString(JSON.NODE)));
        }
        return services.toArray();
    }
}

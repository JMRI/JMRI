package jmri.jmrix.jmriclient.json;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import jmri.jmrix.AbstractNetworkConnectionConfig;
import jmri.jmrix.NetworkPortAdapter;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkConnectionConfig extends AbstractNetworkConnectionConfig {

    protected JTextField transmitPrefixField = new JTextField(10);
    protected JLabel transmitPrefixLabel = new JLabel(Bundle.getMessage("ServerConnectionPrefix")); // NOI18N

    /**
     * Constructor for an object being created while loading existing
     * preferences.
     *
     * @param portAdapter
     */
    public JsonNetworkConnectionConfig(NetworkPortAdapter portAdapter) {
        super(portAdapter);
    }

    /**
     * Constructor for a functional Swing object with no preexisting adapter.
     */
    public JsonNetworkConnectionConfig() {
        super();
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
    protected void checkInitDone() {
        super.checkInitDone();
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
        }
    }

    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems(); // we're adding to the normal advanced items.
        if (this.adapter.getSystemConnectionMemo() != null) {
            this.cR.gridy += 2;
            this.cL.gridy += 2;
            this.gbLayout.setConstraints(transmitPrefixLabel, cL);
            this.gbLayout.setConstraints(transmitPrefixField, cR);
            this._details.add(transmitPrefixLabel);
            this._details.add(transmitPrefixField);
        }
        if (this._details.getParent() != null && this._details.getParent() instanceof JViewport) {
            JViewport vp = (JViewport) _details.getParent();
            vp.validate();
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

}

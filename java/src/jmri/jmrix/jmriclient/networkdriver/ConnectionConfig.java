package jmri.jmrix.jmriclient.networkdriver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;

/**
 * Definition of objects to handle configuring a connection to a remote JMRI
 * instance via the JMRI Network Protocol.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    protected JLabel transmitPrefixLabel = new JLabel("Server Connection Prefix");
    protected JTextField transmitPrefixField = new JTextField(10);

    /**
     * Constructor for an object being created during load process; Swing init
     * is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Simple Network Connection";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

    @Override
    public boolean isPortAdvanced() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        super.checkInitDone();
        if (adapter.getSystemConnectionMemo() != null) {
            transmitPrefixField.setText(((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTransmitPrefix());
            transmitPrefixField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
                    transmitPrefixField.setText(((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTransmitPrefix());
                }
            });
            transmitPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    ((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
                    transmitPrefixField.setText(((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTransmitPrefix());
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
    }

    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems(); // we're adding to the normal advanced items.
        if (adapter.getSystemConnectionMemo() != null) {
            cR.gridy += 2;
            cL.gridy += 2;
            gbLayout.setConstraints(transmitPrefixLabel, cL);
            gbLayout.setConstraints(transmitPrefixField, cR);
            _details.add(transmitPrefixLabel);
            _details.add(transmitPrefixField);
        }
        if (_details.getParent() != null) {
            _details.getParent().revalidate();
            _details.getParent().repaint();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter(); // we're adding more details to the connection.
        if (adapter.getSystemConnectionMemo() != null) {
            ((JMRIClientSystemConnectionMemo) adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
        }
    }

    @Override
    public boolean isHostNameAdvanced() {
        return showAutoConfig.isSelected();
    }

    @Override
    public boolean isAutoConfigPossible() {
        return true;
    }

}

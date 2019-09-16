package jmri.jmrix.pi;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring a Raspberry Pi layout connection.
 * <p>
 * This uses the {@link RaspberryPiAdapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2015
 *
 * @see RaspberryPiAdapter
 */
public class RaspberryPiConnectionConfig extends jmri.jmrix.AbstractConnectionConfig {

    private boolean disabled = false;
    private RaspberryPiAdapter adapter = null;
    private Date GPIOMessageShown = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public RaspberryPiConnectionConfig(RaspberryPiAdapter p) {
        super();
        adapter = p;
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public RaspberryPiConnectionConfig() {
        super();
        adapter = new RaspberryPiAdapter();
    }

    protected boolean init = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
        if (init) {
            return;
        }
        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.addActionListener((ActionEvent e) -> {
                if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) { // not normalized
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionPrefixDialog", systemPrefixField.getText()));
                    systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
                }
            });
            systemPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) { // not normalized
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionPrefixDialog", systemPrefixField.getText()));
                        systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            connectionNameField.addActionListener((ActionEvent e) -> {
                if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionNameDialog", connectionNameField.getText()));
                    connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                }
            });
            connectionNameField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionNameDialog", connectionNameField.getText()));
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });

        }
        init = true;
    }

    @Override
    public void updateAdapter() {
        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    @Override
    protected void showAdvancedItems() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final javax.swing.JPanel details) {
        _details = details;
        setInstance();
        if (!init) {
            if (adapter.getSystemConnectionMemo() != null) {
                systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
                connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                NUMOPTIONS = NUMOPTIONS + 2;
            }
            addStandardDetails(adapter, false, NUMOPTIONS);
            init = false;
            checkInitDone();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new RaspberryPiAdapter();
        }
        if (adapter.getGPIOController() == null) {
            // don't show more than once every 30 seconds
            if (!GraphicsEnvironment.isHeadless()
                    && (this.GPIOMessageShown == null || ((new Date().getTime() - this.GPIOMessageShown.getTime()) / 1000 % 60) > 30)) {
                JOptionPane.showMessageDialog(this._details,
                        Bundle.getMessage("NoGpioControllerMessage"),
                        Bundle.getMessage("NoGpioControllerTitle"),
                        JOptionPane.ERROR_MESSAGE);
                this.GPIOMessageShown = new Date();
            }
        }
    }

    @Override
    public RaspberryPiAdapter getAdapter() {
        return adapter;
    }

    @Override
    public String getInfo() {
        return "GPIO";
    }

    String manuf = RaspberryPiConnectionTypeList.PI;

    @Override
    public String getManufacturer() {
        return manuf;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        manuf = manufacturer;
    }

    @Override
    public String name() {
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        return "Raspberry Pi GPIO"; // NOI18N
    }

    @Override
    public boolean getDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        this.disabled = disable;
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiConnectionConfig.class);

}

package jmri.jmrix.anyma;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import javax.swing.JOptionPane;
import jmri.jmrix.AbstractUSBConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring a Anyma DMX layout connection.
 * <P>
 * This uses the {@link AnymaDMX_Adapter} class to do the actual connection.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 *
 * @see AnymaDMX_Adapter
 */
public class AnymaDMX_ConnectionConfig extends AbstractUSBConnectionConfig {

    private boolean disabled = false;
    private AnymaDMX_Adapter adapter = null;
    private Date DMXMessageShown = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public AnymaDMX_ConnectionConfig(AnymaDMX_Adapter p) {
        super();
        log.info("*	AnymaDMX_ConnectionConfig constructor called.");
        adapter = p;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public AnymaDMX_ConnectionConfig() {
        super();
        log.info("*	AnymaDMX_ConnectionConfig constructor called.");
        adapter = new AnymaDMX_Adapter();
    }

    protected boolean init = false;

    @Override
    protected void checkInitDone() {
        log.info("*	AnymaDMX_ConnectionConfig.checkInitDone() called.");
        if (!init) {
            if (adapter.getSystemConnectionMemo() != null) {
                systemPrefixField.addActionListener((ActionEvent e) -> {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                });
                systemPrefixField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                            JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }
                });
                connectionNameField.addActionListener((ActionEvent e) -> {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                });
                connectionNameField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                            JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }
                });
            }   // if (adapter.getSystemConnectionMemo() != null)
            init = true;
        }   // if (!init)
    }   // checkInitDone()

    @Override
    public void updateAdapter() {
        log.info("*	AnymaDMX_ConnectionConfig.updateAdapter() called.");
        if ((adapter.getSystemConnectionMemo() != null)
                && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }

    }

    @Override
    protected void showAdvancedItems() {
        log.info("*	AnymaDMX_ConnectionConfig.showAdvancedItems() called.");
    }

    @Override
    public void loadDetails(final javax.swing.JPanel details) {
        log.info("*	AnymaDMX_ConnectionConfig.loadDetails() called.");
        _details = details;
        setInstance();
        if (!init) {
            if (adapter.getSystemConnectionMemo() != null) {
                systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                NUMOPTIONS = NUMOPTIONS + 2;
            }
            addStandardDetails(adapter, false, NUMOPTIONS);
            init = false;
            checkInitDone();
        }
    }

    @Override
    protected void setInstance() {
        log.info("*	AnymaDMX_ConnectionConfig.setInstance() called.");
        if (adapter == null) {
            adapter = new AnymaDMX_Adapter();

        }
//        if (adapter.getDMXController() == null) {
//            // don't show more than once every 30 seconds
//            if (!GraphicsEnvironment.isHeadless()
//                    && (this.DMXMessageShown == null || ((new Date().getTime() - this.DMXMessageShown.getTime()) / 1000 % 60) > 30)) {
//                JOptionPane.showMessageDialog(this._details,
//                        Bundle.getMessage("NoDMXControllerMessage"),
//                        Bundle.getMessage("NoDMXControllerTitle"),
//                        JOptionPane.ERROR_MESSAGE);
//                this.DMXMessageShown = new Date();
//            }
//        }
    }

    @Override
    public AnymaDMX_Adapter getAdapter() {
        log.info("*	AnymaDMX_ConnectionConfig.getAdapter() called.");
        return adapter;
    }

    @Override
    public String getInfo() {
        log.info("*	AnymaDMX_ConnectionConfig.getInfo() called.");
        return "DMX";
    }

    @Override
    public String getManufacturer() {
        log.info("*	AnymaDMX_ConnectionConfig.getManufacturer() called.");
        return AnymaDMX_ConnectionTypeList.ANYMA_DMX;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        log.info("*	AnymaDMX_ConnectionConfig.setManufacturer() called.");
    }

    @Override
    public String name() {
        log.info("*	AnymaDMX_ConnectionConfig.name() called.");
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        log.info("*	AnymaDMX_ConnectionConfig.getConnectionName() called.");
        return "Anyma DMX";
    }

    @Override
    public boolean getDisabled() {
        log.info("*	AnymaDMX_ConnectionConfig.getDisabled() called.");
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        log.info("*	AnymaDMX_ConnectionConfig.setDisabled() called.");
        this.disabled = disable;
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_ConnectionConfig.class);
}

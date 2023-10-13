package jmri.jmrix.bidib.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring a BiDiB layout connection via a BiDiBSimulator
 * adapter.
 * <p>
 * This uses the {@link BiDiBSimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Eckart Meyer Copyright (C) 2019
  *
 * @see BiDiBSimulatorAdapter
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    protected JLabel simulationFileLabel = new JLabel("Simulation File:");
    protected JTextField simulationFileField = new JTextField(10);

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p PortAdapter to present port
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
        log.debug("ConnectionConfig, p: {}", p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
        log.debug("ConnectionConfig");
    }

    @Override
    public String name() {
        log.debug("get name");
        return "BiDiB Simulator";
    }

    String manufacturerName = jmri.jmrix.bidib.BiDiBConnectionTypeList.BIDIB;

    @Override
    public String getManufacturer() {
        //log.debug("get manufacturer: {}", manufacturerName);
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        log.debug("set manufacturer: {}", manu);
        manufacturerName = manu;
    }

    @Override
    protected void setInstance() {
        log.debug("BiDiB Simulator ConnectionConfig.setInstance: {}", adapter);
        if (adapter == null) {
            adapter = new BiDiBSimulatorAdapter();
            log.debug("-- adapter created: {}", adapter);
        }
    }

//    @Override
//    public boolean isDirty() {
//        log.debug("isDirty");
//        if (super.isDirty()) {
//            return true;
//        }
//        return ( (()) )
//    }

//    @Override
//    public boolean isRestartRequired() {
//        log.debug("isRestartRequired");
//        return super.isRestartRequired();
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        log.debug("loadDetails");
        super.loadDetails(details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        super.checkInitDone();
        log.debug("checkInitDone");
        if (adapter.getSystemConnectionMemo() != null) {
            simulationFileField.setText(((BiDiBSimulatorAdapter)adapter).getSimulationFile());
            simulationFileField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((BiDiBSimulatorAdapter)adapter).setSimulationFile(simulationFileField.getText());
                    simulationFileField.setText(((BiDiBSimulatorAdapter)adapter).getSimulationFile());
                }
            });
            simulationFileField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    ((BiDiBSimulatorAdapter)adapter).setSimulationFile(simulationFileField.getText());
                    simulationFileField.setText(((BiDiBSimulatorAdapter)adapter).getSimulationFile());
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
        log.debug("showAdvancedItems");
        if (adapter.getSystemConnectionMemo() != null) {
            cR.gridy += 2;
            cL.gridy += 2;
            gbLayout.setConstraints(simulationFileLabel, cL);
            gbLayout.setConstraints(simulationFileField, cR);
            _details.add(simulationFileLabel);
            _details.add(simulationFileField);
        }
        if (_details.getParent() != null) {
            _details.getParent().revalidate();
            _details.getParent().repaint();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter(); // we're adding more details to the connection.
        log.debug("updateAdapter");
        if (adapter.getSystemConnectionMemo() != null) {
            ((BiDiBSimulatorAdapter)adapter).setSimulationFile(simulationFileField.getText());
        }
    }

//    @Override
//    public boolean isHostNameAdvanced() {
//        return showAutoConfig.isSelected();
//    }
//
//    @Override
//    public boolean isAutoConfigPossible() {
//        return true;
//    }


    
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);
}

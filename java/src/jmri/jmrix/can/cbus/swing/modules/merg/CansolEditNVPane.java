package jmri.jmrix.can.cbus.swing.modules.merg;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon.*;
import static jmri.jmrix.can.cbus.swing.modules.merg.CansolPaneProvider.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CansolEditNVPane extends AbstractEditNVPane {
    
    // Number of outputs
    public static final int OUTPUTS = 8;
    
    // Conversion between NV and display values
    public static final int TIME_STEP_SIZE = 10;
    
    private final UpdateNV pulseUpdateFn = new UpdatePulse();
    private final UpdateNV rechargeUpdateFn = new UpdateRecharge();
    private final UpdateNV fireDelayUpdateFn = new UpdateFireDelay();
    private final UpdateNV enableDelayUpdateFn = new UpdateEnableDelay();

    private TitledSpinner pulse[] = new TitledSpinner[OUTPUTS + 1];
    private TitledSpinner rechargeSpinner;
    private TitledSpinner fireDelaySpinner;
    private TitledSpinner enableDelaySpinner;
    
    protected CansolEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getContent() {
       
        JPanel newPane = new JPanel(new BorderLayout());
        
        JPanel gridPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridy = 0;
        
        // Four columns for the outputs
        for (int y = 0; y < OUTPUTS/2; y++) {
            c.gridx = 0;
            for (int x = 0; x < 2; x++) {
                int index = y*2 + x + 1;            // NVs indexed from 1
                pulse[index] = new TitledSpinner(Bundle.getMessage("OutputX", index), index, pulseUpdateFn);
                pulse[index].setToolTip(Bundle.getMessage("CanSolOutputTt"));
                pulse[index].init(_nvArray[index]*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
                gridPane.add(pulse[index], c);
                c.gridx++;
            }
            c.gridy++;
        }

        c.gridx = 0;
        
        rechargeSpinner = new TitledSpinner(Bundle.getMessage("RechargeTime"), RECHARGE_TIME, rechargeUpdateFn);
        rechargeSpinner.setToolTip(Bundle.getMessage("RechargeTimeTt"));
        rechargeSpinner.init(_nvArray[RECHARGE_TIME]*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(rechargeSpinner, c);
        c.gridy++;

        fireDelaySpinner = new TitledSpinner(Bundle.getMessage("FireDelayTime"), FIRE_DELAY, fireDelayUpdateFn);
        fireDelaySpinner.setToolTip(Bundle.getMessage("RechargeTimeTt"));
        fireDelaySpinner.init(_nvArray[FIRE_DELAY]*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(fireDelaySpinner, c);
        c.gridy++;

        enableDelaySpinner = new TitledSpinner(Bundle.getMessage("RechargeTime"), ENABLE_DELAY, enableDelayUpdateFn);
        enableDelaySpinner.setToolTip(Bundle.getMessage("RechargeTimeTt"));
        enableDelaySpinner.init(_nvArray[ENABLE_DELAY]*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(enableDelaySpinner, c);
        c.gridy++;

        JScrollPane scroll = new JScrollPane(gridPane);
        
        newPane.add(scroll, BorderLayout.CENTER);
        newPane.validate();
        newPane.repaint();
        
        return newPane;
    }
    
    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int nv = row + 1;
            CbusNodeNVTableDataModel model = (CbusNodeNVTableDataModel)e.getSource();
            int value = (int)model.getValueAt(row, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _nvArray[nv] = value;
            if ((nv > 0) && (nv <= 8)) {
                log.debug("Update NV {} to {}", nv, value);
                pulse[nv].getModel().setValue(value*TIME_STEP_SIZE);
                log.debug("NV {} Now {}", nv, ((SpinnerNumberModel)pulse[nv].getModel()).getNumber().intValue());
            } else if (nv == 9) {
                log.debug("Update recharge delay to {}", value);
                rechargeSpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else if (nv == 10) {
                log.debug("Update fire delay", value);
                fireDelaySpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else if (nv == 11) {
                log.debug("Update fire delay", value);
                fireDelaySpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else if ((nv >= 12) && (nv <= 16)) {
                // Not used
                log.debug("Update unknow");
                
            } else {
                throw new IllegalArgumentException("Unexpected NV index");
            }
        }
    }
    
    /**
     * Update the NV controlling the output pulse width
     */
    protected class UpdatePulse implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int delay = ((SpinnerNumberModel)pulse[index].getModel()).getNumber().intValue()/TIME_STEP_SIZE;
            _nvArray[index] = delay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NV controlling the recharge delay
     */
    protected class UpdateRecharge implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int delay = ((SpinnerNumberModel)rechargeSpinner.getModel()).getNumber().intValue()/TIME_STEP_SIZE;
            _nvArray[index] = delay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NV controlling the recharge delay
     */
    protected class UpdateFireDelay implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int delay = ((SpinnerNumberModel)fireDelaySpinner.getModel()).getNumber().intValue()/TIME_STEP_SIZE;
            _nvArray[index] = delay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NV controlling the enable delay
     */
    protected class UpdateEnableDelay implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int delay = ((SpinnerNumberModel)enableDelaySpinner.getModel()).getNumber().intValue()/TIME_STEP_SIZE;
            _nvArray[index] = delay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CansolEditNVPane.class);

}

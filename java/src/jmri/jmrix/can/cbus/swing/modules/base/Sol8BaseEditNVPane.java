package jmri.jmrix.can.cbus.swing.modules.base;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon.*;
import static jmri.jmrix.can.cbus.swing.modules.merg.CansolPaneProvider.*;

/**
 * Node Variable edit frame for a basic 8-channel solenoid module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Sol8BaseEditNVPane extends AbstractEditNVPane {
    
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
    
    protected Sol8BaseEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
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
        for (int y = 0; y < OUTPUTS/4; y++) {
            c.gridx = 0;
            for (int x = 0; x < 4; x++) {
                int index = y*4 + x + 1;            // NVs indexed from 1
                pulse[index] = new TitledSpinner((Bundle.getMessage("OutputX", index)) + " " + Bundle.getMessage("PulseWidthUnits"), index, pulseUpdateFn);
                pulse[index].setToolTip(Bundle.getMessage("CanSolOutputTt"));
                pulse[index].init(getSelectValue(index)*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
                gridPane.add(pulse[index], c);
                c.gridx++;
            }
            c.gridy++;
        }

        c.gridx = 0;
        
        rechargeSpinner = new TitledSpinner(Bundle.getMessage("RechargeTimeUnits"), RECHARGE_TIME, rechargeUpdateFn);
        rechargeSpinner.setToolTip(Bundle.getMessage("RechargeTimeTt"));
        rechargeSpinner.init(getSelectValue(RECHARGE_TIME)*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(rechargeSpinner, c);
        c.gridx++;

        fireDelaySpinner = new TitledSpinner(Bundle.getMessage("FireDelayUnits"), FIRE_DELAY, fireDelayUpdateFn);
        fireDelaySpinner.setToolTip(Bundle.getMessage("FireDelayTt"));
        fireDelaySpinner.init(getSelectValue(FIRE_DELAY)*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(fireDelaySpinner, c);
        c.gridx++;

        enableDelaySpinner = new TitledSpinner(Bundle.getMessage("EnableDelayUnits"), ENABLE_DELAY, enableDelayUpdateFn);
        enableDelaySpinner.setToolTip(Bundle.getMessage("EnableDelayTt"));
        enableDelaySpinner.init(getSelectValue(ENABLE_DELAY)*TIME_STEP_SIZE, 0, TIME_STEP_SIZE*255, TIME_STEP_SIZE);
        
        gridPane.add(enableDelaySpinner, c);

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
            int value = getSelectValue(nv);
            if ((nv > 0) && (nv <= 8)) {
                //log.debug("Update NV {} to {}", nv, value);
                pulse[nv].getModel().setValue(value*TIME_STEP_SIZE);
            } else if (nv == 9) {
                //log.debug("Update recharge delay to {}", value);
                rechargeSpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else if (nv == 10) {
                //log.debug("Update fire delay to {}", value);
                fireDelaySpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else if (nv == 11) {
                //log.debug("Update enable delay to {}", value);
                enableDelaySpinner.getModel().setValue(value*TIME_STEP_SIZE);
            } else {
                // Not used, or row was -1
//                log.debug("Update unknown NV {}", nv);
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
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
//    private final static Logger log = LoggerFactory.getLogger(Sol8BaseEditNVPane.class);

}

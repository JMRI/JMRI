package jmri.jmrix.can.cbus.swing.modules.merg;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Canacc8EditNVPane extends AbstractEditNVPane {
    
    // Number of outputs
    public static final int OUTPUTS = 8;
    
    // Output type
    public static final int TYPE_CONTINUOUS = 0;
    public static final int TYPE_SINGLE = 1;
    public static final int TYPE_REPEAT = 2;
    
    // Startup action
    public static final int ACTION_OFF = 3;
    public static final int ACTION_SAVED = 1;
    public static final int ACTION_NONE = 0;
    
    // Conversion between NV and display values
    public static final int PULSE_WIDTH_STEP_SIZE = 20;
    public static final int PULSE_WIDTH_NUM_STEPS = 127;
    public static final double FEEDBACK_DELAY_STEP_SIZE = 0.5;
    
    OutPane [] out = new OutPane[OUTPUTS+1];

    private final UpdateNV pulseUpdateFn = new UpdatePulse();
    private final UpdateNV startupUpdateFn = new UpdateStartup();
    private final UpdateNV feedbackUpdateFn = new UpdateFeedback();

    private TitledSpinner feedbackSpinner;
    
    protected Canacc8EditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public AbstractEditNVPane getContent() {
       
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
                out[index] = new OutPane(index);
                gridPane.add(out[index], c);
                c.gridx++;
            }
            c.gridy++;
        }

        c.gridx = 0;
        c.gridy = 3;
        feedbackSpinner = new TitledSpinner(Bundle.getMessage("FeedbackDelayUnits"), Canacc8PaneProvider.FEEDBACK_DELAY, feedbackUpdateFn);
        feedbackSpinner.setToolTip(Bundle.getMessage("FeedbackDelayTt"));
        feedbackSpinner.init(getSelectValue(Canacc8PaneProvider.FEEDBACK_DELAY)*FEEDBACK_DELAY_STEP_SIZE, 0, 
                FEEDBACK_DELAY_STEP_SIZE*255, FEEDBACK_DELAY_STEP_SIZE);
        
        gridPane.add(feedbackSpinner, c);

        JScrollPane scroll = new JScrollPane(gridPane);
        add(scroll);
        
        return this;
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
                int oldSpinnerValue = out[nv].pulseSpinner.getIntegerValue()/PULSE_WIDTH_STEP_SIZE;
                out[nv].setButtons(value, oldSpinnerValue);
                out[nv].pulseSpinner.setValue((value & 0x7f)*PULSE_WIDTH_STEP_SIZE);
                log.debug("NV {} Now {}", nv, (out[nv].pulseSpinner.getIntegerValue()));
            } else if (nv == 9) {
                //log.debug("Update feedback delay to {}", value);
                feedbackSpinner.setValue(value*FEEDBACK_DELAY_STEP_SIZE);
            } else if ((nv == 10) || (nv == 11)) {
                //log.debug("Update startup action", value);
                for (int i = 1; i <= 8; i++) {
                    out[i].action.setButtons();
                }
            } else {
                // Not used, or row was -1
//                log.debug("Update unknown NV {}", nv);
            }
        }
    }
    
    /**
     * Update the NVs controlling the pulse width and type
     */
    protected class UpdatePulse implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int pulseWidth = out[index].pulseSpinner.getIntegerValue();
            pulseWidth /= PULSE_WIDTH_STEP_SIZE;
            if (out[index].cont.isSelected()) {
                pulseWidth = 0;
            }          
            if (out[index].repeat.isSelected()) {
                pulseWidth |= 0x80;
            }
            // Preserve continuous (bit 7) from old value unless we selected single button
            if ((getSelectValue(index) >= 0x80) && !(out[index].buttonFlag && out[index].single.isSelected())) {
                pulseWidth |= 0x80;
            }
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(pulseWidth, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NVs controlling the startup action
     */
    protected class UpdateStartup implements UpdateNV {
        
        @Override
        public void setNewVal(int index) {
            int newNV10 = getSelectValue(Canacc8PaneProvider.STARTUP_POSITION) & (~(1<<(index-1)));
            int newNV11 = getSelectValue(Canacc8PaneProvider.STARTUP_MOVE) & (~(1<<(index-1)));
            
            // Startup action is in NV10 and NV11, 1 bit per output 
            if (out[index].action.off.isSelected()) {
                // 11
                newNV10 |= (1<<(index-1));
                newNV11 |= (1<<(index-1));
            } else if (out[index].action.saved.isSelected()) {
                // 01
                newNV11 |= (1<<(index-1));
            }
            
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(newNV10, Canacc8PaneProvider.STARTUP_POSITION-1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(newNV11, Canacc8PaneProvider.STARTUP_MOVE-1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NV controlling the feedback delay
     */
    protected class UpdateFeedback implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            double delay = feedbackSpinner.getDoubleValue();
            int newInt = (int)(delay/FEEDBACK_DELAY_STEP_SIZE);
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(newInt, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Construct pane to allow configuration of the module outputs
     */
    private class OutPane extends JPanel {
        
        int _index;
        
        protected JRadioButton cont;
        protected JRadioButton single;
        protected JRadioButton repeat;
        protected TitledSpinner pulseSpinner;
        protected StartupActionPane action;
        protected boolean buttonFlag = false;

        public OutPane(int index) {
            super();
            _index = index;
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder title = BorderFactory.createTitledBorder(border, Bundle.getMessage("OutputX", _index));
            setBorder(title);

            cont = new JRadioButton(Bundle.getMessage("Continuous"));
            cont.setToolTipText(Bundle.getMessage("ContinuousTt"));
            single = new JRadioButton(Bundle.getMessage("Single"));
            single.setToolTipText(Bundle.getMessage("SingleTt"));
            repeat = new JRadioButton(Bundle.getMessage("Repeat"));
            repeat.setToolTipText(Bundle.getMessage("RepeatTt"));

            cont.addActionListener((ActionEvent e) -> {
                typeActionListener();
            });
            single.addActionListener((ActionEvent e) -> {
                typeActionListener();
            });
            repeat.addActionListener((ActionEvent e) -> {
                typeActionListener();
            });
            
            ButtonGroup buttons = new ButtonGroup();
            buttons.add(cont);
            buttons.add(single);
            buttons.add(repeat);

            pulseSpinner = new TitledSpinner(Bundle.getMessage("PulseWidth"), _index, pulseUpdateFn);
            pulseSpinner.setToolTip(Bundle.getMessage("PulseWidthTt"));
            pulseSpinner.init(((getSelectValue(_index) & 0x7f)*PULSE_WIDTH_STEP_SIZE), 0, 
                    PULSE_WIDTH_NUM_STEPS*PULSE_WIDTH_STEP_SIZE, PULSE_WIDTH_STEP_SIZE);

            setButtonsInit(getSelectValue(index));

            gridPane.add(cont, c);
            c.gridy++;
            gridPane.add(single, c);
            c.gridy++;
            gridPane.add(repeat, c);
            c.gridy++;
            gridPane.add(pulseSpinner, c);
            
            c.gridx = 1;
            c.gridy = 0;
            c.gridheight = 4;
            action = new StartupActionPane(_index);
            gridPane.add(action, c);
            
            add(gridPane);
        }
        
        /**
         * Set Initial pulse type button states to reflect pulse width from initial NV value
         * 
         * @param pulseWidth 
         */
        protected void setButtonsInit(int pulseWidth) {
            if ((pulseWidth == 0) || (pulseWidth == 128)) {
                cont.setSelected(true);
                pulseSpinner.setEnabled(false);
            } else if (pulseWidth > 128) {
                repeat.setSelected(true);
            } else {
                single.setSelected(true);
            }                    
        }
        
        /**
         * Set pulse type button states to reflect new setting from change in
         * table model (which may result from changes in this gui).
         * 
         * Changes to table data model from this gui fire a data changed event 
         * back to us so we have a conflict between who is changing the raw 
         * value or who is changing button states, hence the slightly complex
         * logic.
         * 
         * @param pulseWidth from the table change event
         * @param oldPulseWidth from the spinner in this edit gui
         */
        protected void setButtons(int pulseWidth, int oldPulseWidth) {
            if (buttonFlag == true) {
                // User clicked a button
                if (cont.isSelected()) {
                    pulseSpinner.setEnabled(false);
                } else {
                    pulseSpinner.setEnabled(true);
                }
                buttonFlag = false;
            } else {
                // Change came from spinner or generic NV pane
                if (!pulseSpinner.isEnabled()) {
                    // Spinner disabled, change from generic NV pane
                    if ((pulseWidth != 0) && (pulseWidth != 128)) {
                        pulseSpinner.setEnabled(true);
                        if (pulseWidth >= 128) {
                            repeat.setSelected(true);
                        } else {
                            single.setSelected(true);
                        }
                    } else {
                        cont.setSelected(true);
                    }
                } else {
                    // Spinner enabled so was not continuous
                    if (pulseWidth != oldPulseWidth) {
                        // Change of value in generic NV pane
                        if ((pulseWidth & 0x7F) == 0) {
                            // Continuous
                            cont.setSelected(true);
                            pulseSpinner.setEnabled(false);
                        } else {
                            if (pulseWidth >= 128) {
                                repeat.setSelected(true);
                            } else {
                                single.setSelected(true);
                            }
                        }
                    } else if ((pulseWidth & 0x7F) == 0) {
                        // Change of spinner in this edit pane
                        cont.setSelected(true);
                        pulseSpinner.setEnabled(false);
                    }
                }
            }
        }
        
        /**
         * Call the callback to update from radio button selection state.
         */
        protected void typeActionListener() {
            buttonFlag = true;
            pulseUpdateFn.setNewVal(_index);
        }
    }
    
    /**
     * Construct pane to allow configuration of the output startup action
     */
    private class StartupActionPane extends JPanel {
        
        int _index;
        
        JRadioButton off;
        JRadioButton none;
        JRadioButton saved;
    
        public StartupActionPane(int index) {
            super();
            _index = index;
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder title = BorderFactory.createTitledBorder(border, Bundle.getMessage("StartupAction"));
            setBorder(title);

            off = new JRadioButton(Bundle.getMessage("Off"));
            off.setToolTipText(Bundle.getMessage("OffTt"));
            none = new JRadioButton(Bundle.getMessage("None"));
            none.setToolTipText(Bundle.getMessage("NoneTt"));
            saved = new JRadioButton(Bundle.getMessage("SavedAction"));
            saved.setToolTipText(Bundle.getMessage("SavedActionTt"));
            
            off.addActionListener((ActionEvent e) -> {
                startupActionListener();
            });
            none.addActionListener((ActionEvent e) -> {
                startupActionListener();
            });
            saved.addActionListener((ActionEvent e) -> {
                startupActionListener();
            });

            ButtonGroup buttons = new ButtonGroup();
            buttons.add(off);
            buttons.add(none);
            buttons.add(saved);
            setButtons();
            // Startup action is in NV10 and NV11, 1 bit per output 
            if ((getSelectValue(Canacc8PaneProvider.STARTUP_POSITION) & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((getSelectValue(Canacc8PaneProvider.STARTUP_MOVE) & (1<<(_index-1)))>0) {
                // 01
                saved.setSelected(true);
            } else {
                // 00
                none.setSelected(true);
            }

            gridPane.add(off, c);
            c.gridy++;
            gridPane.add(none, c);
            c.gridy++;
            gridPane.add(saved, c);
            
            add(gridPane);
        }
        
        /**
         * Set startup action button states
         */
        public void setButtons() {
            // Startup action is in NV10 and NV11, 1 bit per output 
            if ((getSelectValue(Canacc8PaneProvider.STARTUP_POSITION) & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((getSelectValue(Canacc8PaneProvider.STARTUP_MOVE) & (1<<(_index-1)))>0) {
                // 01
                saved.setSelected(true);
            } else {
                // 00
                none.setSelected(true);
            }
        }
        
        /**
         * Call the callback to update from radio button selection state.
         */
        protected void startupActionListener() {
            startupUpdateFn.setNewVal(_index);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(Canacc8EditNVPane.class);

}

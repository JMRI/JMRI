package jmri.jmrix.can.cbus.swing.modules.base;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a basic 8 channel servo module
 * 
 * SBUS Servo modules behave differently in that they need to be in learn mode to
 * write NVs. NVs are written in "real time" as the user interacts withthe GUI.
 * This allows the servo positions to be observed during setup. The NVs will be
 * stored by the module when it is taken out of learn mode. The entry/exit to/from
 * learn mode is handled in CbusNodeNVEditGuiPane when changing the displayed
 * node.
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Servo8BaseEditNVPane extends AbstractEditNVPane {
    
    // Number of outputs
    public static final int OUTPUTS = 8;
    
    // Startup action
    public static final int ACTION_OFF = 3;
    public static final int ACTION_SAVED = 1;
    public static final int ACTION_NONE = 0;
    
    private ServoPane[] servo = new ServoPane[OUTPUTS+1];

    private final UpdateNV onPosUpdateFn = new UpdateOnPos();
    private final UpdateNV offPosUpdateFn = new UpdateOffPos();
    private final UpdateNV onSpdUpdateFn = new UpdateOnSpd();
    private final UpdateNV offSpdUpdateFn = new UpdateOffSpd();
    private final UpdateNV startupUpdateFn = new UpdateStartup();

    protected JButton save;
        
    protected Servo8BaseEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
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
        c.gridx = 0;
        c.gridy = 0;
        
        // Two columns for the outputs
        for (int y = 0; y < OUTPUTS/2; y++) {
            c.gridx = 0;
            for (int x = 0; x < 2; x++) {
                int index = y*2 + x + 1;            // NVs indexed from 1
                servo[index] = new ServoPane(index);
                gridPane.add(servo[index], c);
                c.gridx++;
            }
            c.gridy++;
        }

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
            int sv = (nv - Servo8BasePaneProvider.OUT1_ON)/4 + 1;   // Outout channel number for NV 5 - 36
            CbusNodeNVTableDataModel model = (CbusNodeNVTableDataModel)e.getSource();
            int value = (int)model.getValueAt(row, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            int oldVal = _nvArray[nv];
            if (oldVal != value) {
                // Only do something if the value has changed
                // JSpinner is very trigger happy with state change updates and setting a new value
                // will trigger another round of updates
                _nvArray[nv] = value;
                if (nv == Servo8BasePaneProvider.CUTOFF) {
                    //log.debug("Update cutoff to {}", value);
                    for (int i = 1; i <= OUTPUTS; i++) {
                        servo[i].cutoff.setSelected((value & (1<<(i-1))) > 0);
                    }
                } else if ((nv == Servo8BasePaneProvider.STARTUP_POS) || (nv == Servo8BasePaneProvider.STARTUP_MOVE)) {
                    //log.debug("Update startup action {}", value);
                    for (int i = 1; i <= OUTPUTS; i++) {
                        servo[i].action.setButtons();
                    }
                } else if (nv == Servo8BasePaneProvider.SEQUENCE) {
                    //log.debug("Update sequential to {}", value);
                    for (int i = 1; i <= OUTPUTS; i++) {
                        servo[i].seq.setSelected((value & (1<<(i-1))) > 0);
                    }
                } else if (nv > Servo8BasePaneProvider.OUT8_OFF_SPD) {
                    // Not used
                    log.debug("Update unknown NV {}", nv);
                } else {
                    // Four NVs per output
                    if (((nv - Servo8BasePaneProvider.OUT1_ON) % 4) == 0) {
                        // ON position
                        //log.debug("Update ON pos NV {} output {} to {}", nv, sv, value);
                        servo[sv].onPosSlider.setValue(value);
                    } else if (((nv - Servo8BasePaneProvider.OUT1_OFF) % 4) == 0) {
                        // OFF position
                        //log.debug("Update OFF pos NV {} output {} to {}", nv, sv, value);
                        servo[sv].offPosSlider.setValue(value);
                    } else if (((nv - Servo8BasePaneProvider.OUT1_ON_SPD) % 4) == 0) {
                        // ON speed, this will trigger the spinner change listener to call updateOnSpd
                        //log.debug("Update ON spd NV {} output {} to {}", nv, sv, value);
                        servo[sv].onSpdSpinner.getModel().setValue(value & 7);
                    } else {
                        // OFF speed, this will trigger the spinner change listener to call updateOffSpd
                        //log.debug("Update OFF spd NV {} output {} to {}", nv, sv, value);
                        servo[sv].offSpdSpinner.getModel().setValue(value & 7);
                    }
                }
            }
        }
    }

    /**
     * Update the NV controlling the ON position
     * 
     * index is the output number 1 - 8
     */
    protected class UpdateOnPos implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int pos = servo[index].onPosSlider.getValue();
            // Four NVs per output
            int nv_index = (index - 1)*4 + Servo8BasePaneProvider.OUT1_ON;
            _nvArray[nv_index] = pos;
            //log.debug("UpdateOnPos() index {} nv {} pos {}", index, nv_index, pos);
            _dataModel.setValueAt(pos, nv_index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), nv_index, pos);
        }
    }
    
    /**
     * Update the NV controlling the OFF position
     * 
     * index is the output number 1 - 8
     */
    protected class UpdateOffPos implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int pos = servo[index].offPosSlider.getValue();
            // Four NVs per output
            int nv_index = (index - 1)*4 + Servo8BasePaneProvider.OUT1_OFF;
            _nvArray[nv_index] = pos;
            //log.debug("UpdateOffPos() index {} nv {} pos {}", index, nv_index, pos);
            _dataModel.setValueAt(pos, nv_index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), nv_index, pos);
        }
    }
    
    /**
     * Update the NV controlling the ON speed
     * 
     * index is the output number 1 - 8
     */
    protected class UpdateOnSpd implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int spd = ((SpinnerNumberModel)servo[index].onSpdSpinner.getModel()).getNumber().intValue();
            // Four NVs per output
            int nv_index = (index - 1)*4 + Servo8BasePaneProvider.OUT1_ON_SPD;
            _nvArray[nv_index] = spd;
            //log.debug("UpdateOnSpeed() index {} nv {} spd {}", index, nv_index, spd);
            // Note that changing the data model will result in tableChanged() being called
            _dataModel.setValueAt(spd, nv_index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), nv_index, spd);
        }
    }
    
    /**
     * Update the NV controlling the OFF speed
     * 
     * index is the output number 1 - 8
     */
    protected class UpdateOffSpd implements UpdateNV {

        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int spd = ((SpinnerNumberModel)servo[index].offSpdSpinner.getModel()).getNumber().intValue();
            // Four NVs per output
            int nv_index = (index - 1)*4 + Servo8BasePaneProvider.OUT1_OFF_SPD;
            _nvArray[nv_index] = spd;
            //log.debug("UpdateOffSpeed index {} nv {} spd {}", index, nv_index, spd);
            // Note that changing the data model will result in tableChanged() being called
            _dataModel.setValueAt(spd, nv_index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), nv_index, spd);
        }
    }
    
    /**
     * Update the NVs controlling the startup action
     */
    protected class UpdateStartup implements UpdateNV {
        
        @Override
        public void setNewVal(int index) {
            int newPos = _nvArray[Servo8BasePaneProvider.STARTUP_POS] & (~(1<<(index-1)));
            int newMove = _nvArray[Servo8BasePaneProvider.STARTUP_MOVE] & (~(1<<(index-1)));
            
            // Startup action is in NV2 and NV3, 1 bit per output 
            if (servo[index].action.off.isSelected()) {
                // 11
                newPos |= (1<<(index-1));
                newMove |= (1<<(index-1));
            } else if (servo[index].action.saved.isSelected()) {
                // 01
                newMove |= (1<<(index-1));
            }
            
            _nvArray[Servo8BasePaneProvider.STARTUP_POS] = newPos;
            _nvArray[Servo8BasePaneProvider.STARTUP_MOVE] = newMove;
            _dataModel.setValueAt(newPos, Servo8BasePaneProvider.STARTUP_POS - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(newMove, Servo8BasePaneProvider.STARTUP_MOVE - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), Servo8BasePaneProvider.STARTUP_POS, newPos);
            _node.send.nVSET(_node.getNodeNumber(), Servo8BasePaneProvider.STARTUP_MOVE, newMove);
        }
    }
    
    /**
     * Construct pane to allow configuration of the module outputs
     */
    private class ServoPane extends JPanel {
        
        int _index;
        
        protected JButton testOn;
        protected JButton testOff;
        protected JRadioButton cutoff;
        protected JRadioButton seq;
        protected TitledSlider onPosSlider;
        protected TitledSlider offPosSlider;
        protected TitledSpinner onSpdSpinner;
        protected TitledSpinner offSpdSpinner;
        protected StartupActionPane action;

        public ServoPane(int index) {
            super();
            _index = index;
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder title = BorderFactory.createTitledBorder(border, Bundle.getMessage("OutputX", _index));
            setBorder(title);

            testOn = new JButton(Bundle.getMessage("TestOn"));
            testOff = new JButton(Bundle.getMessage("TestOff"));
            cutoff = new JRadioButton(Bundle.getMessage("Cutoff"));
            seq = new JRadioButton(Bundle.getMessage("SequentialOp"));
            
            testOn.setToolTipText(Bundle.getMessage("TestOnTt"));
            testOff.setToolTipText(Bundle.getMessage("TestOffTt"));
            cutoff.setToolTipText(Bundle.getMessage("CutoffTt"));
            seq.setToolTipText(Bundle.getMessage("SequentialOpTt"));

            testOn.addActionListener((ActionEvent e) -> {
                testActionListener(e);
            });
            testOff.addActionListener((ActionEvent e) -> {
                testActionListener(e);
            });
            cutoff.addActionListener((ActionEvent e) -> {
                cutoffActionListener();
            });
            seq.addActionListener((ActionEvent e) -> {
                seqActionListener();
            });
            
            onPosSlider = new TitledSlider(Bundle.getMessage("OnPos"), _index, onPosUpdateFn);
            onPosSlider.setToolTip(Bundle.getMessage("OnPosTt"));
            onPosSlider.init(0, 255, 127);
            
            offPosSlider = new TitledSlider(Bundle.getMessage("OffPos"), _index, offPosUpdateFn);
            offPosSlider.setToolTip(Bundle.getMessage("OffPosTt"));
            offPosSlider.init(0, 255, 127);
            
            onSpdSpinner = new TitledSpinner(Bundle.getMessage("OnSpd"), _index, onSpdUpdateFn);
            onSpdSpinner.setToolTip(Bundle.getMessage("OnSpdTt"));
            onSpdSpinner.init(0, 0, 7, 1);

            offSpdSpinner = new TitledSpinner(Bundle.getMessage("OffSpd"), _index, offSpdUpdateFn);
            offSpdSpinner.setToolTip(Bundle.getMessage("OffSpdTt"));
            offSpdSpinner.init(0, 0, 7, 1);

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 3;
            c.weighty = 1;
            gridPane.add(onPosSlider, c);
            c.gridy++;
            gridPane.add(offPosSlider, c);
            c.gridy++;
            c.gridwidth = 1;
            gridPane.add(testOn, c);
            c.gridx++;
            gridPane.add(testOff, c);
            c.gridx++;
            gridPane.add(cutoff, c);
            
            c.gridx = 3;
            c.gridy = 0;
            gridPane.add(onSpdSpinner, c);
            c.gridy++;
            gridPane.add(offSpdSpinner, c);
            c.gridy++;
            gridPane.add(seq, c);
            
            c.gridx = 4;
            c.gridy = 0;
            c.gridheight = 3;
            action = new StartupActionPane(_index);
            gridPane.add(action, c);
            
            add(gridPane);
        }
        
        /**
         * Callback for test buttons.
         * 
         * Writes output number to NV37, adding 128 for ON event
         */
        protected void testActionListener(ActionEvent e) {
            int val;
            for (int i = 1; i <= OUTPUTS; i++) {
                val = 0;
                if (e.getSource() == servo[i].testOn) {
                    log.debug("Servo {} test ON", i);
                    val = 128 + i;
                } else if (e.getSource() == servo[i].testOff) {
                    log.debug("Servo {} test OFF", i);
                    val = i;
                }
                if (val > 0) {
                    // Send to module immediately
                    _node.send.nVSET(_node.getNodeNumber(), Servo8BasePaneProvider.LAST, val);
                }
            }
        }
        
        /**
         * Callback for cut off buttons.
         */
        protected void cutoffActionListener() {
            int newCutoff = 0;
            for (int i = OUTPUTS; i > 0; i--) {
                newCutoff = (newCutoff << 1) + ((servo[i].cutoff.isSelected()) ? 1 : 0);
            }
            log.debug("Cutoff Action now {}", newCutoff);
            _dataModel.setValueAt(newCutoff, Servo8BasePaneProvider.CUTOFF - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), Servo8BasePaneProvider.CUTOFF, newCutoff);
        }
        
        /**
         * Callback for sequential move button.
         */
        protected void seqActionListener() {
            int newSeq = 0;
            for (int i = OUTPUTS; i > 0; i--) {
                newSeq = (newSeq << 1) + ((servo[i].seq.isSelected()) ? 1 : 0);
            }
            log.debug("Sequential Action now {}", newSeq);
            _dataModel.setValueAt(newSeq, Servo8BasePaneProvider.SEQUENCE - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            // Send to module immediately
            _node.send.nVSET(_node.getNodeNumber(), Servo8BasePaneProvider.SEQUENCE, newSeq);
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
            // Startup action is in NV2 and NV3, 1 bit per output 
            if ((_nvArray[Servo8BasePaneProvider.STARTUP_POS] & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((_nvArray[Servo8BasePaneProvider.STARTUP_MOVE] & (1<<(_index-1)))>0) {
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
            // Startup action is in NV2 and NV3, 1 bit per output 
            if ((_nvArray[Servo8BasePaneProvider.STARTUP_POS] & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((_nvArray[Servo8BasePaneProvider.STARTUP_MOVE] & (1<<(_index-1)))>0) {
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
    
    private final static Logger log = LoggerFactory.getLogger(Servo8BaseEditNVPane.class);

}

package jmri.jmrix.can.cbus.swing.modules.merg;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;

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
    UpdatePulse pulseUpdateFn = new UpdatePulse();
    UpdateStartup startupUpdateFn = new UpdateStartup();

    TitledSpinner feedbackSpinner;
    
    protected Canacc8EditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
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
                int index = y*4+x+1;
                out[index] = new OutPane(index);
                gridPane.add(out[index], c);
                c.gridx++;
            }
            c.gridy++;
        }

        c.gridx = 0;
        c.gridy = 3;
        feedbackSpinner = new TitledSpinner("Feedback Delay (ms)", 9, new UpdateFeedback());    // NV9 for feedback delay 
        feedbackSpinner.init((double)_nvArray[9]*FEEDBACK_DELAY_STEP_SIZE, 0, 
                FEEDBACK_DELAY_STEP_SIZE*255, FEEDBACK_DELAY_STEP_SIZE);
        
        gridPane.add(feedbackSpinner, c);

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
                out[nv].setButtons(value);  // Do this first to ensure correct state when buttons are tested as value is updated
                out[nv].pulseSpinner.tSpin.getModel().setValue((value & 0x7f)*PULSE_WIDTH_STEP_SIZE);
                log.debug("NV {} Now {}", nv, ((SpinnerNumberModel)out[nv].pulseSpinner.tSpin.getModel()).getNumber().intValue());
            } else if (nv == 9) {
                log.debug("Update feedback delay to {}", value);
                feedbackSpinner.tSpin.getModel().setValue(value*FEEDBACK_DELAY_STEP_SIZE);
            } else if ((nv == 10) || (nv == 11)) {
                log.debug("Update startup action", value);
                for (int i = 1; i <= 8; i++) {
                    out[i].action.setButtons();
                }
            } else if (nv == 12) {
                // Not used
                log.debug("Update unknow");
                
            } else {
                throw new IllegalArgumentException("Unexpected NV index");
            }
        }
    }
    
    /**
     * Interface for callback function(s) used to update the NVs
     */
    public interface UpdateNV {
        
        /**
         * Build a new NV value from contents of gui elements
         * 
         * @param index of the NV
         */
        void setNewVal(int index);
    }
    
    /**
     * Update the NVs controlling the pulse width and type
     * 
     * {@inheritDoc}
     */
    public class UpdatePulse implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int pulseWidth = 0;
            if (out[index].single.isSelected() || out[index].repeat.isSelected()) {
                pulseWidth = ((SpinnerNumberModel)out[index].pulseSpinner.tSpin.getModel()).getNumber().intValue();
                pulseWidth /= PULSE_WIDTH_STEP_SIZE;
            }            
            if (out[index].repeat.isSelected()) {
                pulseWidth |= 0x80;
            }
            _nvArray[index] = pulseWidth;
            _dataModel.setValueAt(pulseWidth, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NVs controlling the startup action
     * 
     * {@inheritDoc}
     */
    public class UpdateStartup implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int newNV10 = _nvArray[10] & (~(1<<(index-1)));
            int newNV11 = _nvArray[11] & (~(1<<(index-1)));
            
            // Startup action is in NV10 and NV11, 1 bit per output 
            if (out[index].action.off.isSelected()) {
                // 11
                newNV10 |= (1<<(index-1));
                newNV11 |= (1<<(index-1));
            } else if (out[index].action.saved.isSelected()) {
                // 01
                newNV11 |= (1<<(index-1));
            }
            
            _nvArray[10] = newNV10;
            _nvArray[11] = newNV11;
            _dataModel.setValueAt(newNV10, 9, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(newNV11, 10, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the NVs controlling the feedback delay
     * 
     * {@inheritDoc}
     */
    public class UpdateFeedback implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            double delay = ((SpinnerNumberModel)feedbackSpinner.tSpin.getModel()).getNumber().doubleValue();
            int newInt = (int)((double)delay/FEEDBACK_DELAY_STEP_SIZE);
            _nvArray[index] = newInt;
            _dataModel.setValueAt(newInt, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Construct pane to allow configuration of the module outputs
     */
    private final class OutPane extends JPanel {
        
        int _index;
        
        protected JRadioButton cont;
        protected JRadioButton single;
        protected JRadioButton repeat;
        protected TitledSpinner pulseSpinner;
        protected StartupActionPane action;

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
            TitledBorder title = BorderFactory.createTitledBorder(border, "Output"+_index);
            setBorder(title);

            cont = new JRadioButton("Continuous");
            single = new JRadioButton("Single Pulse");
            repeat = new JRadioButton("Repeat Pulse");

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
            setButtons(_nvArray[index]);

            pulseSpinner = new TitledSpinner("Pulse width (ms)", _index, pulseUpdateFn);
            pulseSpinner.init((double)((_nvArray[_index] & 0x7f)*PULSE_WIDTH_STEP_SIZE), 0, 
                    PULSE_WIDTH_NUM_STEPS*PULSE_WIDTH_STEP_SIZE, PULSE_WIDTH_STEP_SIZE);

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
         * Set pulse type button states to reflect pulse width
         * 
         * @param pulseWidth 
         */
        protected void setButtons(int pulseWidth) {
            if (pulseWidth == 0) {
                cont.setSelected(true);
            } else if (pulseWidth > 127) {
                repeat.setSelected(true);
            } else {
                single.setSelected(true);
            }
        }
        
        /**
         * Call the callback to update from radio button selection state.
         */
        protected void typeActionListener() {
            pulseUpdateFn.setNewVal(_index);
        }
    }
    
    /**
     * Construct pane to allow configuration of the oputput startup action
     */
    private final class StartupActionPane extends JPanel {
        
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
            TitledBorder title = BorderFactory.createTitledBorder(border, "Startup Action");
            setBorder(title);

            off = new JRadioButton("Off");
            none = new JRadioButton("None");
            saved = new JRadioButton("Saved Action");
            
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
            if ((_nvArray[10] & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((_nvArray[11] & (1<<(_index-1)))>0) {
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
         * 
         * @param nv10 startup position
         * @param nv11 move on startup
         */
        public void setButtons() {
            // Startup action is in NV10 and NV11, 1 bit per output 
            if ((_nvArray[10] & (1<<(_index-1)))>0) {
                // 1x
                off.setSelected(true);
            } else if ((_nvArray[11] & (1<<(_index-1)))>0) {
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
    
    /**
     * Spinner with titled border
     */
    private final class TitledSpinner extends JPanel implements ChangeListener {
        
        protected JSpinner tSpin;
        protected int _index;
        protected String _title;
        protected double _step;
        protected UpdateNV _update;
        
        /**
         * Construct a new titledSpinner
         * 
         * @param title to be displayed
         * @param index of the associated NV 
         * @param update callback funtion to apply new value
         */
        public TitledSpinner(String title, int index, UpdateNV update) {
            super();
            _title = title;
            _index = index;
            _update = update;
        }
        
        protected void init(double init, double min, double max, double step) {
            _step = step;
            GridLayout grid = new GridLayout(1, 1);
            setLayout(grid);

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder titled = BorderFactory.createTitledBorder(border, _title);
            setBorder(titled);

            SpinnerNumberModel spinModel = new SpinnerNumberModel(init, min, max, _step);
            tSpin = new JSpinner(spinModel);
            tSpin.addChangeListener(this);

            add(tSpin);
        }
        
        /**
         * Call back with updated value
         * 
         * @param e the spinner change event
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner spin = (JSpinner)(e.getSource());
            SpinnerNumberModel spinModel = (SpinnerNumberModel)(spin.getModel());
            int newVal = spinModel.getNumber().intValue();
            _update.setNewVal(_index);
        }

        /**
         ** The preferred width on the panel must consider the width of the text
         ** used on the TitledBorder
         * 
         * from <a href=https://stackoverflow.com/questions/43425939/how-to-get-the-titledborders-title-to-display-properly-in-the-gui>
         */
        @Override
        public Dimension getPreferredSize() {
            
            Dimension preferredSize = super.getPreferredSize();

            Border border = getBorder();
            int borderWidth = 0;

            if (border instanceof TitledBorder) {
                Insets insets = getInsets();
                TitledBorder titledBorder = (TitledBorder)border;
                borderWidth = titledBorder.getMinimumSize(this).width + insets.left + insets.right;
            }

            int preferredWidth = Math.max(preferredSize.width, borderWidth);

            return new Dimension(preferredWidth, preferredSize.height);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Canacc8EditNVPane.class);

}

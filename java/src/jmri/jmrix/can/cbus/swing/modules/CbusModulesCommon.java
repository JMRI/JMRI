package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane._nvArray;
import jmri.jmrix.can.cbus.swing.modules.sprogdcc.Sprog3PlusPaneProvider;

/**
 * Returns configuration objects for a MERG CANACC8
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusModulesCommon {
    
    /**
     * Interface for callback function(s) used to update the NVs
     */
    public interface UpdateNV {
        
        /**
         * Build a new NV value from contents of gui elements
         * 
         * @param index Index of something. e.g., an NV, or an output bit, etc
         */
        void setNewVal(int index);
    }
    
    /**
     * Spinner with titled border
     */
    public static class TitledSpinner extends JPanel implements ChangeListener {
        
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
            tSpin = new JSpinner();
        }

        /**
         * Initialise the spinner
         * 
         * @param init  Initial value
         * @param min   Minimum value
         * @param max   Maximum value
         * @param step  Step
         */
        public void init(double init, double min, double max, double step) {
            _step = step;
            GridLayout grid = new GridLayout(1, 1);
            setLayout(grid);

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder titled = BorderFactory.createTitledBorder(border, _title);
            setBorder(titled);

            SpinnerNumberModel spinModel = new SpinnerNumberModel(init, min, max, _step);
            tSpin.setModel(spinModel);
            tSpin.addChangeListener(this);

            add(tSpin);
        }
        
        /**
         * Set the tool tip
         * 
         * @param tt tooltip text
         */
        public void setToolTip(String tt) {
            tSpin.setToolTipText(tt);
        }
        
        /**
         * Enable the spinner
         * 
         * @param b boolean to enable (true) or disable (false)
         */
        @Override
        public void setEnabled(boolean b) {
            tSpin.setEnabled(b);
        }
        
        /**
         * Is the spinner enabled
         * 
         * @return true or false 
         */
        @Override
        public boolean isEnabled() {
            return tSpin.isEnabled();
        }
        
        /**
         * Get the spinner model
         * 
         * @return spinner model
         */
        public SpinnerModel getModel() {
            return tSpin.getModel();
        }
        
        /**
         * Call back with updated value
         * 
         * @param e the spinner change event
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            _update.setNewVal(_index);
        }

        /**
         ** The preferred width on the panel must consider the width of the text
         ** used on the TitledBorder
         * 
         * from <a href=https://stackoverflow.com/questions/43425939/how-to-get-the-titledborders-title-to-display-properly-in-the-gui></a>
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

    /**
     * Slider with titled border
     */
    public static class TitledSlider extends JPanel implements ChangeListener {
        
        protected JSlider tSlide;
        protected int _index;
        protected String _title;
        protected UpdateNV _update;
        
        /**
         * Construct a new titledSpinner
         * 
         * @param title to be displayed
         * @param index of the associated NV 
         * @param update callback funtion to apply new value
         */
        public TitledSlider(String title, int index, UpdateNV update) {
            super();
            _title = title;
            _index = index;
            _update = update;
            tSlide = new JSlider();
        }

        /**
         * Initialise the spinner
         * 
         * @param init  Initial value
         * @param min   Minimum value
         * @param max   Maximum value
         * @param step  Step
         */
        public void init(int min, int max, int init) {
            GridLayout grid = new GridLayout(1, 1);
            setLayout(grid);

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder titled = BorderFactory.createTitledBorder(border, _title);
            setBorder(titled);

            tSlide.setMinimum(min);
            tSlide.setMaximum(max);
            tSlide.setValue(init);
            tSlide.addChangeListener(this);

            add(tSlide);
        }
        
        /**
         * Set slider value
         * 
         * @param v  value
         */
        public void setValue(int v) {
            tSlide.setValue(v);
        }
        
        /**
         * Get slider value
         * 
         * @return slider value
         */
        public int getValue() {
            return tSlide.getValue();
        }
        
        /**
         * Set the tool tip
         * 
         * @param tt tooltip text
         */
        public void setToolTip(String tt) {
            tSlide.setToolTipText(tt);
        }
        
        /**
         * Call back with updated value
         * 
         * @param e the spinner change event
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                _update.setNewVal(_index);
            }
        }

        /**
         ** The preferred width on the panel must consider the width of the text
         ** used on the TitledBorder
         * 
         * from <a href=https://stackoverflow.com/questions/43425939/how-to-get-the-titledborders-title-to-display-properly-in-the-gui></a>
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

    /**
     * Class to display CBUS command station flag settings
     * 
     * Each flags object is a set of 8 bits that form an NV
     */
    public static class CmdStaFlags extends JPanel {
        
        protected int _index;
        protected String _title;
        protected int flags;
        protected JRadioButton [] buttons;
        protected UpdateNV _flagUpdateFn;

        /**
         * 
         * @param index of the flags, not the NV array index which may be offset
         * @param title of the flags object
         * @param flagStrings array of strings to name each flag bit
         * @param flagTtStrings array of tooltip strings for each flag bit
         * @param update the callback function to update the table data model
         */
        public CmdStaFlags(int index, String title, String [] flagStrings, String [] flagTtStrings, UpdateNV update) {
            super();
            
            _index = index;
            _title = title;
            _flagUpdateFn = update;
            
            flags = _nvArray[Sprog3PlusPaneProvider.USER_FLAGS + _index];
            buttons = new JRadioButton[8];
            for (int i = 0; i < 8; i++) {
                buttons[i] = new JRadioButton(flagStrings[i]);
                buttons[i].setToolTipText(flagTtStrings[i]);
                buttons[i].addActionListener((ActionEvent e) -> {
                    flagActionListener();
                });
            }
        }
        
        /**
         * Get the panel to display the flags
         * 
         * @return JPanel displaying the flags
         */
        public JPanel getContents() {
            
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder t = BorderFactory.createTitledBorder(border, _title);
            gridPane.setBorder(t);

            for (int i = 0; i < 8; i++) {
                gridPane.add(buttons[i], c);
                c.gridy++;
            }
            setButtons();
            
            return gridPane;
        }
        
        /**
         * Call the callback to update from flags state.
         */
        protected void flagActionListener() {
            int value = buttons[7].isSelected() ? 1 : 0;
            for (int i = 6; i >= 0; i--) {
                value = (value << 1) + (buttons[i].isSelected() ? 1 : 0);
            }
            setFlags(value);
            _flagUpdateFn.setNewVal(_index);
        }

        /**
         * Update the flags settings
         * 
         * @param value settings
         */
        public void setFlags(int value) {
            flags = value;
            setButtons();
        }
        
        /**
         * Get the flags settings
         * 
         * @return flags as an int
         */
        public int getFlags() {
            return flags;
        }
        
        /**
         * Set the buttons to the state of the flags
         */
        protected void setButtons() {
            for (int i = 0; i < 8; i++) {
                if ((flags & (1<<i)) > 0) {
                    buttons[i].setSelected(true);
                } else {
                    buttons[i].setSelected(false);
                }
            }
        }
    }

}

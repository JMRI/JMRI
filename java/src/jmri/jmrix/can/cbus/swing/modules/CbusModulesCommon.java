package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmri.jmrix.can.cbus.swing.modules.merg.Canacc8EditNVPane;
import jmri.jmrix.can.cbus.swing.modules.sprogdcc.Sprog3PlusEditNVPane;

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
         * @param index of the NV
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

}

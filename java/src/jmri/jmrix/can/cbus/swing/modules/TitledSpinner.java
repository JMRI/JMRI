package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSpinner with titled border
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class TitledSpinner extends JPanel implements ChangeListener {
        
    protected JSpinner tSpin;
    protected int _index;
    protected String _title;
    protected UpdateNV _update;
    protected Object lastValue;


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
     * Initialise with float values
     * 
     * @param init  Initial value
     * @param min   Minimum value
     * @param max   Maximum value
     * @param step  Step
     */
    public void init(double init, double min, double max, double step) {
        SpinnerNumberModel spinModel = new SpinnerNumberModel(init, min, max, step);
        init(spinModel);
    }

    /**
     * Initialise with int values
     * 
     * @param init Initial value for spinner
     * @param min  Minimum value for spinner
     * @param max  Maximum value fro spinner
     * @param step Step size for spinner adjustments
     */
    public void init(int init, int min, int max, int step) {
        SpinnerNumberModel spinModel = new SpinnerNumberModel(init, min, max, step);
        init(spinModel);
    }

    /**
     * Initialise from the spin model
     * 
     * @param spinModel 
     */
    private void init(SpinnerNumberModel spinModel) {
        GridLayout grid = new GridLayout(1, 1);
        setLayout(grid);

        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder titled = BorderFactory.createTitledBorder(border, _title);
        setBorder(titled);

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
     * Set the current spinner value
     * 
     * Check that the supplied value is the correct type for the current spinner,
     * otherwise we seem to get more stateChange events that can update the gui
     * that change the spinner again, ... leading to an endless code loop
     * 
     * @param val Number which should contain a Double or an Integer
     */
    public void setValue(Number val) {
        if (val.getClass() == (((SpinnerNumberModel)tSpin.getModel()).getValue()).getClass()) {
            tSpin.getModel().setValue(val);
        } else {
            log.error("Expected {} given {}", (((SpinnerNumberModel)tSpin.getModel()).getValue()).getClass(), val.getClass());
        }
    }

    /**
     * Get the Double representation of the spinner value
     * 
     * @return Spinner value as Double
     */
    public Double getDoubleValue() {
        return ((SpinnerNumberModel)tSpin.getModel()).getNumber().doubleValue();
    }

    /**
     * Get the int representation of the spinner value
     * 
     * @return Spinner values as int
     */
    public int getIntegerValue() {
        return ((SpinnerNumberModel)tSpin.getModel()).getNumber().intValue();
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

    private final static Logger log = LoggerFactory.getLogger(TitledSpinner.class);

}

package jmri.jmrit.symbolicprog;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Extends a JSlider so that its color & value are consistent with
 * an underlying variable; we return one of these in DecValVariable.getNewRep.
 *
 * @author   Bob Jacobsen   Copyright (C) 2001
 */
public class DecVarSlider extends JSlider implements ChangeListener {

    DecVarSlider(DecVariableValue var, int min, int max) {
        super(new DefaultBoundedRangeModel(min, 0, min, max));
        _var = var;
        // get the original color right
        setBackground(_var.getColor());
        if (_var.getColor() == _var.getDefaultColor()) {
            setOpaque(false);
        } else {
            setOpaque(true);
        }
        // set the original value
        setValue(Integer.parseInt(_var.getValueString()));
        // listen for changes here
        addChangeListener(this);
        // listen for changes to associated variable
        _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                originalPropertyChanged(e);
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // called for new values of a slider - set the variable value as needed
        // e.getSource() points to the JSlider object - find it in the list
        JSlider j = (JSlider) e.getSource();
        BoundedRangeModel r = j.getModel();

        _var.setIntValue(r.getValue());
        _var.setState(AbstractValue.EDITED);
    }

    DecVariableValue _var;

    void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("VarSlider saw property change: " + e);
        }
        // update this color from original state
        if (e.getPropertyName().equals("State")) {
            setBackground(_var.getColor());
            if (_var.getColor() == _var.getDefaultColor()) {
                setOpaque(false);
            } else {
                setOpaque(true);
            }
        }
        if (e.getPropertyName().equals("Value")) {
            int newValue = Integer.parseInt(((JTextField) _var.getCommonRep()).getText());
            setValue(newValue);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DecVarSlider.class);

}

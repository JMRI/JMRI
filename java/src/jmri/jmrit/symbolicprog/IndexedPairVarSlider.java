// IndexedPairVarSlider.java
package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/* Extends a JSlider so that its color & value are consistent with
 * an underlying variable; we return one of these in IndexedVariableValue.getNewRep.
 *
 * @author    Howard G. Penny   Copyright (C) 2005
 * @author    Bob Jacobsen   Copyright (C) 2013
 * @version   $Revision$
 * @deprecated // since 3.7.1
 */
@Deprecated // since 3.7.1
public class IndexedPairVarSlider extends JSlider implements ChangeListener {

    IndexedPairVariableValue _iVar;

    IndexedPairVarSlider(IndexedPairVariableValue iVar, int min, int max) {
        super(new DefaultBoundedRangeModel(min, 0, min, max));
        _iVar = iVar;
        // get the original color right
        setBackground(_iVar.getColor());
        // set the original value
        setValue(Integer.valueOf(_iVar.getValueString()).intValue());
        // listen for changes here
        addChangeListener(this);
        // listen for changes to associated variable
        _iVar.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                originalPropertyChanged(e);
            }
        });
    }

    public void stateChanged(ChangeEvent e) {
        // called for new values of a slider - set the variable value as needed
        // e.getSource() points to the JSlider object - find it in the list
        JSlider j = (JSlider) e.getSource();
        BoundedRangeModel r = j.getModel();

        _iVar.setIntValue(r.getValue());
        _iVar.setState(AbstractValue.EDITED);
    }

    void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("IndexedVarSlider saw property change: "+e);
        // update this color from original state
        if (e.getPropertyName().equals("State")) {
            setBackground(_iVar.getColor());
        }
        if (e.getPropertyName().equals("Value")) {
            int newValue = Integer.valueOf(((JTextField)_iVar.getCommonRep()).getText()).intValue();
            setValue(newValue);
        }
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(IndexedPairVarSlider.class.getName());
}

package jmri.jmrit.voltmeter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.Meter;
import jmri.swing.NamedBeanComboBox;

/**
 * Action to select a meter
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class SelectMeterAction extends AbstractAction {
    
    private final Class<? extends Meter> _clazz;
    private final SetMeterFunction _setMeterFunction;
    
    public SelectMeterAction(String actionName, Class<? extends Meter> clazz, SetMeterFunction setMeterFunction) {
        super(actionName);
        this._clazz = clazz;
        this._setMeterFunction = setMeterFunction;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
    
    
    public interface SetMeterFunction {
        
        public void setMeter(Meter m);
    }
    
}

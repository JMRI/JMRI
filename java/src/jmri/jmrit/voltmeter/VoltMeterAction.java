package jmri.jmrit.voltmeter;

import java.awt.event.ActionEvent;
import java.util.SortedSet;

import javax.swing.AbstractAction;

import jmri.*;

/**
 * Swing action to create and register a AmpMeterFrame object.
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 *
 * This was a direct steal form the LCDClock code by Ken Cameron,
 * which was a direct steal from the Nixie clock code, ver 1.5. 
 * Thank you Bob Jacobsen and Ken Cameron.
 */
public class VoltMeterAction extends AbstractAction {

    public VoltMeterAction() {
        this(Bundle.getMessage("MenuItemVoltMeter"));
    }

    public VoltMeterAction(String s) {
        super(s);
        
        // disable ourself if no MeterGroup available
        if (!hasVoltageMeter()) {
            setEnabled(false);
        }
    }
    
    private boolean hasVoltageMeter() {
        MeterGroupManager m = InstanceManager.getNullableDefault(MeterGroupManager.class);
        if (m == null) return false;
        
        SortedSet<MeterGroup> set = m.getNamedBeanSet();
        if (set.isEmpty()) return false;
        
        return set.first().getMeterByName(MeterGroup.VoltageMeter) != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        VoltMeterFrame f = new VoltMeterFrame();
        f.initComponents();
        f.setVisible(true);
    }

}

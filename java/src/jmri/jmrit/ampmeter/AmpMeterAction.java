package jmri.jmrit.ampmeter;

import java.awt.event.ActionEvent;
import java.util.SortedSet;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.Meter;
import jmri.MeterGroup;
import jmri.MeterGroupManager;

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
public class AmpMeterAction extends AbstractAction {

    public AmpMeterAction() {
        this(Bundle.getMessage("MenuItemAmpMeter"));
    }

    public AmpMeterAction(String s) {
        super(s);
        
        // disable ourself if no MeterGroup available
        if (!hasCurrentMeter()) {
            setEnabled(false);
        }
    }
    
    private boolean hasCurrentMeter() {
        MeterGroupManager m = InstanceManager.getNullableDefault(MeterGroupManager.class);
        if (m == null) return false;
        
        SortedSet<MeterGroup> set = m.getNamedBeanSet();
        if (set.isEmpty()) return false;
        
        return set.first().getMeterByName(MeterGroup.CurrentMeter) != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        AmpMeterFrame f = new AmpMeterFrame();
        f.initComponents();
        f.setVisible(true);
    }

}

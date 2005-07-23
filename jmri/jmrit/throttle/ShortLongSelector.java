package jmri.jmrit.throttle;

import javax.swing.JComboBox;
import jmri.*;

/**
 * Tool for selecting short/long address for DCC throttles.
 *
 * This is made more complex because we want it to appear easier.
 * Some DCC systems allow addresses like 112 to be either long (extended)
 * or short; others default to one or the other.
 *
 * @author     Bob Jacobsen   Copyright (C) 2005
 * @version    $Revision: 1.1 $
 */
public class ShortLongSelector extends JComboBox
{

    public ShortLongSelector() {
        super(new String[]{"---", "Short", "Long"});
    }
    
    public boolean isLong(int address) {
        ThrottleManager tf = InstanceManager.throttleManagerInstance();

        // if it has to be long, handle that
        if (tf.canBeLongAddress(address) && !tf.canBeShortAddress(address)) {
            setSelectedIndex(2);
            return true;
        }
        
        // if it has to be short, handle that
        if (!tf.canBeLongAddress(address) && tf.canBeShortAddress(address)) {
            setSelectedIndex(1);
            return false;
        }
        
        // now we're in the "could be either" place; use selection
        switch (getSelectedIndex()) {
            case 0:
                { // well, now we've got a problem; no clue, so guess short
                    setSelectedIndex(1);
                    return false;
                }
            case 1:
                return false;
            case 2:
                return true;
            default:
                return false;
        }
    }
        
}
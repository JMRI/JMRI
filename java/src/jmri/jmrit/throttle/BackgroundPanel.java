package jmri.jmrit.throttle;

import java.awt.Color;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.ResizableImagePanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to be used as background for JMRI throttle frames 
 * 
 * @author Lionel Jeanson - 2009-
 * 
 */

public class BackgroundPanel extends ResizableImagePanel implements AddressListener {

    AddressPanel addressPanel = null;

    public BackgroundPanel() {
        super();
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        initGUI();
        applyPreferences();
    }
    
    private void initGUI() {
        setBackground(Color.GRAY);
        setRespectAspectRatio(true);
    }
    
    public void applyPreferences() {
        setResizingContainer(InstanceManager.getDefault(ThrottlesPreferences.class).isResizingWindow());
    }

    public void setAddressPanel(AddressPanel addressPanel) {
        this.addressPanel = addressPanel;
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        RosterEntry rosterEntry = null;
        if (addressPanel != null) {
            rosterEntry = addressPanel.getRosterEntry();
        }
        if (rosterEntry != null) {
            setImagePath(rosterEntry.getImagePath());
        } else {
            if (t.getLocoAddress().toString().compareTo("3(S)") == 0) // default DCC address
            {
                setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.png"));
            }
            if (t.getLocoAddress().toString().compareTo("0(S)") == 0) // default DC address
            {
                setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.png"));
            }
        }
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        setImagePath(null);
        setVisible(false);
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
    }

    private final static Logger log = LoggerFactory.getLogger(BackgroundPanel.class);    
}

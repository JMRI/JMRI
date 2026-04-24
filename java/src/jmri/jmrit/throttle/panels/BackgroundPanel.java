package jmri.jmrit.throttle.panels;

import java.awt.Color;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.swing.ResizableImagePanel;

/**
 * A panel to be used as background for JMRI throttle frames 
 * 
 * @author Lionel Jeanson - 2009, 2026
 * 
 */

public class BackgroundPanel extends ResizableImagePanel implements AddressListener {

    AddressPanel addressPanel = null;

    public BackgroundPanel() {
        super();
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
        if (this.addressPanel != null) {
            this.addressPanel.removeAddressListener(this);
        }
        this.addressPanel = addressPanel;
        if (this.addressPanel != null) {
            this.addressPanel.addAddressListener(this);
        }    
    }

    private void updateImage(RosterEntry rosterEntry) {
        setImagePath(null);
        if (rosterEntry != null) {
            setImagePath(rosterEntry.getImagePath());
        } else if (addressPanel != null) {
            DccThrottle t = addressPanel.getThrottle();
            if (t != null) {               
                if (t.getLocoAddress().toString().compareTo("3(S)") == 0) { // default DCC address
                    setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.png"));
                }
                if (t.getLocoAddress().toString().compareTo("0(S)") == 0) { // default DC address
                    setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.png"));
                }
            }
        }
    }
    

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }       
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {
        updateImage(re);
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        setImagePath(null);
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress l) {
        updateImage(null);
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle t) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }
    }

    public void destroy() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackgroundPanel.class);    
}

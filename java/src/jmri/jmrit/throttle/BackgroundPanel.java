package jmri.jmrit.throttle;

import java.awt.Color;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.ResizableImagePanel;

public class BackgroundPanel extends ResizableImagePanel implements AddressListener {

    public BackgroundPanel() {
        super();
        setBackground(Color.GRAY);
        setRespectAspectRatio(true);
        if (InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isResizingWindow()) {
            setResizingContainer(true);
        }
    }

    AddressPanel addressPanel = null;

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
}

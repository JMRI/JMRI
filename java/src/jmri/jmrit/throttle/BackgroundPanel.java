package jmri.jmrit.throttle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.ResizableImagePanel;

public class BackgroundPanel extends ResizableImagePanel implements AddressListener {

	public BackgroundPanel() {
		super();
		setBackground(Color.GRAY);
		setRespectAspectRatio(true);
        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isResizingWindow() )
        	setResizingContainer(true);
	}
	
    AddressPanel addressPanel = null;
    public void setAddressPanel(AddressPanel addressPanel){
    	this.addressPanel = addressPanel; 
    }

	public void notifyAddressThrottleFound(DccThrottle t) {
		RosterEntry rosterEntry = null;
		if (addressPanel != null)
			rosterEntry = addressPanel.getRosterEntry();
		if ( rosterEntry != null ) {
			setImagePath(rosterEntry.getImagePath());
		}
		else {
			if ( t.getLocoAddress().toString().compareTo("3(S)") == 0 )  // default DCC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.png"));
			if ( t.getLocoAddress().toString().compareTo("0(S)") == 0 )  // default DC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.png"));
		}
	}

	public void notifyAddressReleased(LocoAddress la)  {
		setImagePath(null);
		setVisible(false);
	}
	
	public void notifyAddressChosen(LocoAddress l) {		
	}

        public void notifyConsistAddressChosen(int newAddress, boolean isLong) { 
        }
                
        public void notifyConsistAddressReleased(int address, boolean isLong) {
        }

        public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        }

    static Logger log = LoggerFactory.getLogger(BackgroundPanel.class.getName());
}

package jmri.jmrit.throttle;

import jmri.DccThrottle;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.DnDImagePanel;

public class BackgroundPanel extends DnDImagePanel {

	public BackgroundPanel() {
		super();
		setRespectAspectRatio(true);
        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isResizingWindow() )
        	setResizingContainer(true);
	}
	
    AddressPanel addressPanel = null;
    public void setAddressPanel(AddressPanel addressPanel){
    	this.addressPanel = addressPanel; 
    }
	
	public void notifyThrottleFound(DccThrottle t) {
		RosterEntry rosterEntry = null;
		if (addressPanel != null)
			rosterEntry = addressPanel.getRosterEntry();
		if ( rosterEntry != null ) {
			setImagePath(rosterEntry.getImagePath());
//			setDnd(true);
		}
		else {
			if ( t.getLocoAddress().toString().compareTo("3(S)") == 0 )  // default DCC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.jpg"));
			if ( t.getLocoAddress().toString().compareTo("0(S)") == 0 )  // default DC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.jpg"));
		}
	}

	public void notifyThrottleDisposed() {
		setDnd(false);
		unsetImage();
	}
	
	public void saveImageToRoster(RosterEntry rosterEntry) {
    	if (rosterEntry == null)
    		return;
    	rosterEntry.setImagePath(this.getImagePath());
    	Roster.writeRosterFile();
    	return;
	}

	public void notifyConsistThrottleFound(DccThrottle t) {	
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BackgroundPanel.class.getName());
}

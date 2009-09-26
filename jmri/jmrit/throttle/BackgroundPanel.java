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
    		setDnd(true);
        }
 /*       else {	
			if ( (DccLocoAddress)t.getLocoAddress().toString() == new DccLocoAddress(0, false) )
				setImagePath(jmri.util.FileUtil.getExternalFilename("program:/resources/throttles/DCImage.jpg"));
			if ( t.getLocoAddress() == 3 )
				setImagePath(jmri.util.FileUtil.getExternalFilename("program:/resources/throttles/DCCImage.jpg"));
		}*/
	}

	public void notifyThrottleDisposed() {
		setDnd(false);
		unsetImage();
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BackgroundPanel.class.getName());

	public void saveImageToRoster(RosterEntry rosterEntry) {
    	if (rosterEntry == null)
    		return;
    	rosterEntry.setImagePath(this.getImagePath());
    	Roster.writeRosterFile();
    	return;
	}

	public void notifyConsistThrottleFound(DccThrottle t) {
		// TODO Auto-generated method stub
		
	}

}

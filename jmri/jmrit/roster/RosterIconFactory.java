package jmri.jmrit.roster;

import java.util.HashMap;

import javax.swing.ImageIcon;

public class RosterIconFactory {
	private int iconHeight;
	HashMap<String,ImageIcon> icons = new HashMap<String,ImageIcon>();
	
	public RosterIconFactory(int h) {
		iconHeight = h;
	}
	
	public RosterIconFactory() {
		iconHeight = 19; // OS X, because of Apple look'n feel constraints, ComboBox cannot be higher than this 19pixels
	}
	
	public ImageIcon getIcon(String id) {	
		if (id == null) return null;
		RosterEntry re = Roster.instance().entryFromTitle(id);
		if (re==null) return null;
		return getIcon(re);
	}

	public ImageIcon getIcon(RosterEntry re) {
		if (re==null) return null;

		ImageIcon icon = icons.get(re.getIconPath());
		if (icon == null) {
			icon = new ImageIcon( re.getIconPath(), re.getId());
			icon.setImage( icon.getImage().getScaledInstance( -1, iconHeight, java.awt.Image.SCALE_FAST ));
			icons.put(re.getIconPath(), icon);
		}
		return icon;
	}
	
    public static RosterIconFactory instance() {
        if (_instance == null) {
            _instance = new RosterIconFactory();
        }
        return _instance;
    }

    private static RosterIconFactory _instance;
}

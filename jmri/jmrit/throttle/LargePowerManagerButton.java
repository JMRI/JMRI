package jmri.jmrit.throttle;

import jmri.jmrit.catalog.NamedIcon;

public class LargePowerManagerButton extends PowerManagerButton {
	void loadIcons() {
    	powerOnIcon = new NamedIcon("resources/icons/throttles/PowerGreen24.png", "resources/icons/throttles/PowerGreen24.png");
		powerOffIcon = new NamedIcon("resources/icons/throttles/PowerRed24.png", "resources/icons/throttles/PowerRed24.png");
		powerXIcon = new NamedIcon("resources/icons/throttles/PowerYellow24.png", "resources/icons/throttles/PowerYellow24.png");
	}

}

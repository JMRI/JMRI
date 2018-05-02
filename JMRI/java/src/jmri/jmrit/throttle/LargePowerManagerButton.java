package jmri.jmrit.throttle;

import jmri.jmrit.catalog.NamedIcon;

public class LargePowerManagerButton extends PowerManagerButton {

    public LargePowerManagerButton(Boolean fullText) {
        super(fullText);
    }

    public LargePowerManagerButton() {
        super();
    }

    @Override
    protected void loadIcons() {
        setPowerOnIcon(new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png"));
        setPowerOffIcon(new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png"));
        setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png"));
    }

}

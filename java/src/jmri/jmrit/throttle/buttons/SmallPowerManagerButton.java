package jmri.jmrit.throttle.buttons;

import jmri.jmrit.catalog.NamedIcon;

/**
 * 
 * A small button handling layout power
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 */

public class SmallPowerManagerButton extends PowerManagerButton {

    public SmallPowerManagerButton(Boolean fullText) {
        super(fullText);
    }

    public SmallPowerManagerButton() {
        super();
    }
    
    @Override
    protected void initComponents() {
        setBorderPainted(false);
    }

    @Override
    protected void loadIcons() {
        setPowerOnIcon(new NamedIcon("resources/icons/throttles/GreenPowerLED.gif", "resources/icons/throttles/GreenPowerLED.gif"));
        setPowerOffIcon(new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif"));
        setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
    }

}

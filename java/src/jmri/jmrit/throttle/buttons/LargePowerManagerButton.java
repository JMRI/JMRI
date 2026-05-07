package jmri.jmrit.throttle.buttons;

import jmri.jmrit.catalog.NamedIcon;

/**
 * 
 * A Large button handling layout power
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

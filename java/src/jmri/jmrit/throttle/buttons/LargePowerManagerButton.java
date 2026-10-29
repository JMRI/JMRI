package jmri.jmrit.throttle.buttons;

import javax.annotation.CheckForNull;

import jmri.PowerManager;
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

    /**
     * Create a Large Power Manager Button for a given Power Manager.
     * @param fullText true if displaying text and graphic, false for graphic only.
     * @param powerMgr The PowerManager to monitor and control.
     */
    public LargePowerManagerButton(boolean fullText, @CheckForNull PowerManager powerMgr) {
        super(fullText, powerMgr);
    }

    /**
     * Create a Large Power Manager Button for the default Power Manager.
     * @param fullText true if displaying text and graphic, false for graphic only.
     */
    public LargePowerManagerButton(Boolean fullText) {
        super(fullText);
    }

    /**
     * Create a Large Power Manager Button for the default Power Manager,
     * displaying text and graphic.
     */
    public LargePowerManagerButton() {
        super();
    }

    @Override
    protected void loadIcons() {
        setPowerOnIcon(new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png"));
        setPowerOffIcon(new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png"));
        setPowerIdleIcon(new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png"));
        setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png"));
    }

}

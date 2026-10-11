package jmri.jmrit.throttle.buttons;

import javax.annotation.CheckForNull;

import jmri.PowerManager;
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

    /**
     * Create a SmallPowerManagerButton for a given Power Manager.
     * @param fullText true if displaying text and graphic, false for graphic only.
     * @param powerMgr The PowerManager to monitor and control.
     */
    public SmallPowerManagerButton(boolean fullText, @CheckForNull PowerManager powerMgr) {
        super(fullText, powerMgr);
    }

    /**
     * Create a Small Power Manager Button for the default Power Manager.
     * @param fullText true if displaying text and graphic, false for graphic only.
     */
    public SmallPowerManagerButton(Boolean fullText) {
        super(fullText);
    }

    /**
     * Create a Small Power Manager Button for the default Power Manager,
     * displaying text and graphic.
     */
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
        setPowerIdleIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
        setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
    }

}

package jmri.jmrit.roster;

import java.util.HashMap;
import javax.swing.ImageIcon;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author	Lionel Jeanson   Copyright (C) 2009
 * @version	$$
 */
public class RosterIconFactory {

    private int iconHeight;
    HashMap<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

    public RosterIconFactory(int h) {
        iconHeight = h;
    }

    public RosterIconFactory() {
        iconHeight = 19; // OS X, because of Apple look'n feel constraints, ComboBox cannot be higher than this 19pixels
    }

    public ImageIcon getIcon(String id) {
        if (id == null) {
            return null;
        }
        RosterEntry re = Roster.instance().entryFromTitle(id);
        if (re == null) {
            return null;
        }
        return getIcon(re);
    }

    public ImageIcon getIcon(RosterEntry re) {
        if ((re == null) || (re.getIconPath() == null)) {
            return null;
        }

        ImageIcon icon = icons.get(re.getIconPath());
        if (icon == null) {
            icon = new ImageIcon(re.getIconPath(), re.getId());
            /* icon can not be null
             if (icon==null)
             return null;
             */
            icon.setImage(icon.getImage().getScaledInstance(-1, iconHeight, java.awt.Image.SCALE_FAST));
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

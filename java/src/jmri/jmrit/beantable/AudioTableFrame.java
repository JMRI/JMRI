package jmri.jmrit.beantable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jmri.Audio;

/**
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
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Matthew Harris copyright (c) 2009
 */
public class AudioTableFrame extends BeanTableFrame<Audio> {

    AudioTablePanel audioPanel;

    public AudioTableFrame(AudioTablePanel panel,
            String helpTarget) {

        super();

        audioPanel = panel;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());

        //fileMenu.add(panel.getPrintItem());
        setJMenuBar(menuBar);

        addHelpMenu(helpTarget, true);

        // install items in GUI
        getContentPane().add(audioPanel);
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        getContentPane().add(bottomBox);

        // add extras, if desired by subclass
        extras();

    }

    @Override
    public void dispose() {
        if (audioPanel != null) {
            audioPanel.dispose();
        }
        super.dispose();
    }

}

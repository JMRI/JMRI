package jmri.jmrit.beantable;

import java.awt.BorderLayout;

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

    private final AudioTablePanel audioPanel;

    public AudioTableFrame(AudioTablePanel panel, String helpTarget) {

        super();

        audioPanel = panel;

        // general GUI config
        getContentPane().setLayout(new BorderLayout());

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.StoreMenu());
        fileMenu.add(panel.getPrintItem());

        setJMenuBar(menuBar);

        addHelpMenu(helpTarget, true);

        // install items in GUI
        getContentPane().add(audioPanel, BorderLayout.CENTER);
        getContentPane().add(bottomBox, BorderLayout.SOUTH);

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

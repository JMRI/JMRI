package jmri.jmrit.throttle.implementation;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * A translucent JPanel (grey semitransparent background)
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
 * @author Lionel Jeanson 2007-2026
 * 
 */

public class TranslucentJPanel extends JPanel {

    private final Color TRANS_COL = new Color(100, 100, 100, 100);

    public TranslucentJPanel() {
        super();
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(TRANS_COL);
        g.fillRoundRect(0, 0, getSize().width, getSize().height, 10, 10);
        super.paintComponent(g);
    }
}

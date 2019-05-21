package jmri.util.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * Creates a simple "indeterminate" busy spinner dialog...
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */
public class BusyDialog extends JDialog {

    JFrame frame;
    JProgressBar pbar;

    public BusyDialog(JFrame frame, String title, boolean modal) {
        super(frame, title, modal);
        this.frame = frame;
        initComponents();
    }

    public void initComponents() {

        setLocationRelativeTo(frame);
        setPreferredSize(new Dimension(200, 100));
        setMinimumSize(new Dimension(200, 100));
        setLayout(new BorderLayout(10, 10));

        pbar = new JProgressBar();
        pbar.setIndeterminate(true);
        pbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        //pbar.setBorderPainted(true);
        this.add(pbar, BorderLayout.CENTER);
    }

    @SuppressWarnings("deprecation")
    public void start() {
        this.pack();
        this.setVisible(true);
        this.getContentPane().paintAll(pbar.getGraphics());
        this.show();
    }

    public void finish() {
        this.dispose();

    }

    // Unused, for now.  Commented out to avoid the compiler warning.
    //private static final Logger log = LoggerFactory.getLogger(VSDecoderPane.class);
}

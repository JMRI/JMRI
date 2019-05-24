package jmri.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a Swing component to show and/or edit a PhysicalLocation
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
 * @author			Mark Underwood Copyright (C) 2011
 */
public class PhysicalLocationPanel extends JPanel {

    TitledBorder tb;
    JSpinner xs, ys, zs;
    SpinnerNumberModel spinnerModel;

    static final Float min_spin = -1000.0f;
    static final Float max_spin = 1000.0f;
    static final Float spin_value = 0.0f;
    static final Float spin_inc = 0.1f;

    /**
     * Constructor
     */
    public PhysicalLocationPanel() {
        super();
        initComponents("");
    }

    /**
     * Constructor
     *
     * @param title (String) : Window title
     */
    public PhysicalLocationPanel(String title) {
        super();
        initComponents(title);
    }

    private GridBagConstraints setConstraints(int x, int y, boolean fill) {
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(2, 2, 2, 2);
        gbc1.gridx = x;
        //gbc1.gridx = GridBagConstraints.RELATIVE;
        gbc1.gridy = y;
        gbc1.weightx = 100.0;
        gbc1.weighty = 100.0;
        gbc1.gridwidth = 1;
        gbc1.anchor = GridBagConstraints.LINE_START;
        return (gbc1);
    }

    protected void initComponents(String title) {

        tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title);
        tb.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        this.setBorder(tb);

        this.setLayout(new GridBagLayout());

        xs = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));
        ys = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));
        zs = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));

        JLabel xl = new JLabel("X");
        JLabel yl = new JLabel("Y");
        JLabel zl = new JLabel("Z");

        this.add(xl, setConstraints(0, 0, false));
        this.add(xs, setConstraints(GridBagConstraints.RELATIVE, 0, true));
        this.add(yl, setConstraints(GridBagConstraints.RELATIVE, 0, false));
        this.add(ys, setConstraints(GridBagConstraints.RELATIVE, 0, true));
        this.add(zl, setConstraints(GridBagConstraints.RELATIVE, 0, false));
        this.add(zs, setConstraints(GridBagConstraints.RELATIVE, 0, true));

        this.setVisible(true);
        log.debug("initComponents() complete");
    }

    /**
     * Set the window tile
     *
     * @param t : new title
     */
    public void setTitle(String t) {
        tb.setTitle(t);
    }

    /**
     * Retrieve the window title
     *
     * @return (String) title
     */
    public String getTitle() {
        return (tb.getTitle());
    }

    /**
     * Set the value of the pane
     *
     * @param p (PhysicalLocation) : value to set
     */
    public void setValue(PhysicalLocation p) {
        xs.setValue(p.getX());
        ys.setValue(p.getY());
        zs.setValue(p.getZ());
    }

    /**
     * Set the value of the pane
     *
     * @param s (String) : value to set
     */
    public void setValue(String s) {
        PhysicalLocation p = PhysicalLocation.parse(s);
        if (p != null) {
            this.setValue(p);
        }
    }

    /**
     * Get the value of the pane
     *
     * @return PhysicalLocation : Current value of pane
     */
    public PhysicalLocation getValue() {
        Float x = (Float) ((SpinnerNumberModel) xs.getModel()).getNumber();
        Float y = (Float) ((SpinnerNumberModel) ys.getModel()).getNumber();
        Float z = (Float) ((SpinnerNumberModel) zs.getModel()).getNumber();
        return (new PhysicalLocation(x, y, z));

    }

    private static final Logger log = LoggerFactory.getLogger(PhysicalLocationPanel.class);

}

package jmri.jmrit.vsdecoder;

/*
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
 */
import java.awt.GridLayout;
import java.util.ArrayList;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSDSoundsPanel extends JmriPanel {

    String decoder_id;
    VSDecoderPane main_pane;

    public VSDSoundsPanel() {
        super();
    }

    public VSDSoundsPanel(String dec, VSDecoderPane dad) {
        super();
        decoder_id = dec;
        main_pane = dad;
        initComponents();

    }

    public void init() {
    }

    @Override
    public void initContext(Object context) {
        initComponents();
    }

    @Override
    public void initComponents() {

        this.setLayout(new GridLayout(0, 3));

        if (main_pane.getDecoder() == null) {
            log.debug("No decoder!");
            return;
        }

        ArrayList<SoundEvent> elist = new ArrayList<SoundEvent>(main_pane.getDecoder().getEventList());
        for (SoundEvent e : elist) {
            if (e.getButton() != null) {
                log.debug("adding button " + e.getButton().toString());
            }
            this.add(e.getButton());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(VSDSoundsPanel.class);

}

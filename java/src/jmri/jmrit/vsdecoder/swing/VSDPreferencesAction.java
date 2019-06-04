package jmri.jmrit.vsdecoder.swing;

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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.util.JmriJFrame;

public class VSDPreferencesAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public VSDPreferencesAction(String s) {
        super(s);
    }

    public VSDPreferencesAction() {
        this("VSDecoder preferences");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JmriJFrame f = new JmriJFrame(Bundle.getMessage("VSDecoderFileMenuPreferences"), false, false); // used here for the Frame Title
        VSDecoderPreferencesPane tpP = new VSDecoderPreferencesPane(VSDecoderManager.instance().getVSDecoderPreferences());
        f.add(tpP);
        tpP.setContainer(f);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        f.requestFocus();
    }
}

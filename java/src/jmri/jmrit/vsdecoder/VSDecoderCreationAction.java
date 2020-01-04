package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.awt.GraphicsEnvironment;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
public class VSDecoderCreationAction extends AbstractAction {

    Boolean _useNewGUI = false;

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public VSDecoderCreationAction(String s, Boolean ng) {
        super(s);
        _useNewGUI = ng;
        if (GraphicsEnvironment.isHeadless()) {
            log.info("GUI lookAndFeel: {}", UIManager.getLookAndFeel().getName());
        }
    }

    public VSDecoderCreationAction() {
        this("Virtual Sound Decoder", true);
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String fp = null, fn = null;
        JFrame tf = null;
        if (_useNewGUI == true) {
            tf = VSDecoderManager.instance().provideManagerFrame(); // headless will return null
        } else {
            tf = new VSDecoderFrame(); // old GUI
        }

        // Handle Auto-Load
        if (VSDecoderManager.instance().getVSDecoderPreferences().isAutoLoadingDefaultVSDFile() && !GraphicsEnvironment.isHeadless()) {
            // Force load of a VSD file
            fp = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
            fn = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFileName();
            if (fn.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Cannot auto-load VSD File - file name missing - check your VSD Preferences value",
                        Bundle.getMessage("VSDFileError"), JOptionPane.ERROR_MESSAGE);
            } else {
                LoadVSDFileAction.loadVSDFile(fp + File.separator + fn);
            }
        }

        if (tf != null) {
            tf.toFront();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(VSDecoderCreationAction.class);

}

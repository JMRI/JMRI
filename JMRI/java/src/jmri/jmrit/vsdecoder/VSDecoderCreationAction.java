package jmri.jmrit.vsdecoder;

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
 *
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new VSDecoder Pane.
 *
 * @author Mark Underwood
 */
@SuppressWarnings("serial")
public class VSDecoderCreationAction extends AbstractAction {

    Boolean _useNewGUI = false;
    //private static JFrame openFrame = null;

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public VSDecoderCreationAction(String s, Boolean ng) {
        super(s);
        _useNewGUI = ng;
    }

    public VSDecoderCreationAction() {
        //this(ThrottleBundle.bundle().getString("MenuItemNewThrottle"));
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
            tf = VSDecoderManager.instance().provideManagerFrame();
        } else {
            tf = new VSDecoderFrame();
        }
        if (VSDecoderManager.instance().getVSDecoderPreferences().isAutoLoadingDefaultVSDFile()) {
            // Force load of a VSD file
            fp = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
            fn = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFileName();
            log.debug("Loading VSD File: " + fp + File.separator + fn);
            LoadVSDFileAction.loadVSDFile(fp + File.separator + fn);
        }
        tf.toFront();
    }

    private final static Logger log = LoggerFactory.getLogger(VSDecoderCreationAction.class);
}

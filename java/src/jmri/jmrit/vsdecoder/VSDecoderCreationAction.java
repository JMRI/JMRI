package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

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

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public VSDecoderCreationAction(String s) {
        super(s);
    }

    public VSDecoderCreationAction() {
        this("Virtual Sound Decoder");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame tf;

        tf = VSDecoderManager.instance().provideManagerFrame(); // headless will return null

        if (tf != null) {
            tf.toFront();
        }
    }

    //private final static Logger log = LoggerFactory.getLogger(VSDecoderCreationAction.class);

}

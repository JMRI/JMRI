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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class VSDecoderPreferencesAction extends AbstractAction {
    /**
     * Constructor
     * @param s Name for the action.
     */
    public VSDecoderPreferencesAction(String s) {
        super(s);
    }
    
    public VSDecoderPreferencesAction() {
	  this("VSDecoder preferences");         
    }
    
	public void actionPerformed(ActionEvent e) {
		jmri.jmrit.vsdecoder.VSDecoderManager.instance().showVSDecoderPreferences();
	}
}

// SystemConsoleAction.java

package apps;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to display the JMRI System Console
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
 * @author Matthew Harris  copyright (c) 2010
 * @version $Revision$
 */
public class SystemConsoleAction extends jmri.util.swing.JmriAbstractAction {

    public SystemConsoleAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public SystemConsoleAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public SystemConsoleAction() {
        super(Bundle.getString("TitleConsole"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Show system console
        SystemConsole.getConsole().setVisible(true);
    }
    
    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked"); // NOI18N
    }

}

/* @(#)SystemConsoleAction.java */
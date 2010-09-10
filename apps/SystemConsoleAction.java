// SystemConsoleAction.java

package apps;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

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
 * @version $Revision: 1.1 $
 */
public class SystemConsoleAction extends AbstractAction {

    public SystemConsoleAction() {
        super();
    }

    public void actionPerformed(ActionEvent e) {

        // Show system console
        SystemConsole.getConsole().setVisible(true);

    }

}

/* @(#)SystemConsoleAction.java */
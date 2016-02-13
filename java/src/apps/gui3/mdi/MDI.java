// MDI.java
package apps.gui3.mdi;

import java.util.ResourceBundle;
import jmri.Application;
import jmri.util.swing.mdi.MdiMainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI application for developing the 3rd GUI
 * <P>
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author	Bob Jacobsen Copyright 2003, 2004, 2007, 2009, 2010
 * @version $Revision$
 */
public class MDI extends apps.gui3.Apps3 {

    public MDI(String[] args) {
        super("JMRI GUI3 Demo", null, args);
        this.start();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new MdiMainFrame(Application.getApplicationName(),
                "xml/config/apps/demo/Gui3LeftTree.xml",
                "xml/config/apps/demo/Gui3Menus.xml",
                "xml/config/apps/demo/Gui3MainToolBar.xml");
    }

    // Main entry point
    public static void main(String args[]) {
        new MDI(args);
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(MDI.class.getName());
}

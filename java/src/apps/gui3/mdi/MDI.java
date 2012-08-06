// MDI.java

package apps.gui3.mdi;

import jmri.util.swing.mdi.MdiMainFrame;
import java.io.File;
import java.util.ResourceBundle;
import jmri.Application;

/**
 * The JMRI application for developing the 3rd GUI
 * <P>
 *
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
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004, 2007, 2009, 2010
 * @version     $Revision$
 */
public class MDI extends apps.gui3.Apps3 {

    public MDI(String[] args) {
        super("JMRI GUI3 Demo", null, args);
        this.start();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                                                    justification="only one application at a time")
    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new MdiMainFrame(Application.getApplicationName(),
                new File("xml/config/apps/demo/Gui3LeftTree.xml"), 
    	        new File("xml/config/apps/demo/Gui3Menus.xml"), 
    	        new File("xml/config/apps/demo/Gui3MainToolBar.xml"));
    }
    
    // Main entry point
    public static void main(String args[]) {
        MDI app = new MDI(args);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return null;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MDI.class.getName());
}



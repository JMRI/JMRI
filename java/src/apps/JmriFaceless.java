// JmriFaceless.java

package apps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application for running JMRI server functions without a graphical interface.
 *   Goal is to run on very light hardware, such as a Raspberry Pi, without requiring the 
 *   overhead associated with display functions.  Needs an existing JMRI configuration file
 *   passed as parm to program, or it will use the default of JmriFacelessConfig3.xml 
 * Copied from apps.gui3.demo3.Demo3, then removed all ui-related elements
 * NOTE: JMRI "server" functions based on ui components (such as JmriJFrames)
 *   may need to be modified to check isHeadless() and adjust their 
 *   behavior as needed. 
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
 * @author	Steve Todd   Copyright 2012
 * @version     $Revision: 17977 $
 */
public class JmriFaceless extends apps.AppsBase {

	// insure that the headless property is true for downstream use
	static {           
		System.setProperty("java.awt.headless", "true");
	}

    public JmriFaceless(String[] args) {
        super("JmriFaceless", "JmriFacelessConfig3.xml", args);
        this.start();
      
        //start LocoNetOverTcp server if configured to autostart
        jmri.jmrix.loconet.loconetovertcp.Server.getInstance();  
    }

	// Main entry point
    public static void main(String args[]) {
        new JmriFaceless(args);
    }

    static Logger log = LoggerFactory.getLogger(JmriFaceless.class.getName());
}

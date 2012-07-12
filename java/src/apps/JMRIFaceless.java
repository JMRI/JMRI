// JMRIFaceless.java

package apps;

/**
 * Application for running JMRI server functions without a graphical interface.
 *   Goal is to run on very light hardware, such as a Raspberry Pi, without requiring the 
 *   overhead associated with display functions.  Needs an existing JMRI configuration file
 *   passed as parm to program, or it will use the default of JMRIFacelessConfig3.xml 
 * Copied from apps.gui3.demo3.Demo3, then removed all ui-related elements
 * NOTE: tested with NCE simulator, web server and WiThrottle (modified).  
 *   Other JMRI functions may need to be modified to check isHeadless() and adjust 
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
public class JMRIFaceless extends apps.AppsBase {

	static { /* insure that the headless property is true for downstream use */
		System.setProperty("java.awt.headless", "true");
	}

	// Main entry point
    public static void main(String args[]) {

        // do processing needed immediately, before
        // we attempt anything else
        preInit("JMRIFaceless");
        
        setConfigFilename("JMRIFacelessConfig3.xml", args);
        
        // create the program object
        JMRIFaceless app = new JMRIFaceless();
        
        // do final post initialization processing
        app.postInit();
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMRIFaceless.class.getName());
}



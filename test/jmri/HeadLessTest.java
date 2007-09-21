// HeadLessTest.java

package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the jmri package
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
 * <P>
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 * @version         $Revision: 1.6 $
 */
public class HeadLessTest extends TestCase {

    // from here down is testing infrastructure

    public HeadLessTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", HeadLessTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.JmriTest");  // no tests in this class itself
		suite.addTest(jmri.BlockTest.suite());
		suite.addTest(jmri.RouteTest.suite());
		suite.addTest(jmri.BlockManagerTest.suite());
		suite.addTest(jmri.BeanSettingTest.suite());
		suite.addTest(jmri.PathTest.suite());
        suite.addTest(jmri.DccLocoAddressTest.suite());
        suite.addTest(jmri.progdebugger.ProgDebuggerTest.suite());
        suite.addTest(jmri.NmraPacketTest.suite());
        suite.addTest(jmri.configurexml.ConfigXmlTest.suite());
        suite.addTest(jmri.util.UtilTest.suite());
        
        // suite.addTest(jmri.jmrix.JmrixTest.suite());
        // The single line above expands to all of these, 
        // cut & paste in here to allow adding one at a time.
		suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
		suite.addTest(jmri.jmrix.cmri.serial.SerialTest.suite());
		suite.addTest(jmri.jmrix.direct.DirectTest.suite());
		//suite.addTest(jmri.jmrix.easydcc.EasyDccTest.suite());
		suite.addTest(jmri.jmrix.lenz.XNetTest.suite());
		suite.addTest(jmri.jmrix.loconet.LocoNetTest.suite());
		//suite.addTest(jmri.jmrix.nce.NceTest.suite());
		//suite.addTest(jmri.jmrix.pricom.PricomTest.suite());
		//suite.addTest(jmri.jmrix.tmcc.SerialTest.suite());
		//suite.addTest(jmri.jmrix.oaktree.SerialTest.suite());
		//suite.addTest(jmri.jmrix.qsi.QsiTest.suite());

        suite.addTest(jmri.jmrit.JmritTest.suite());

        return suite;
    }

}

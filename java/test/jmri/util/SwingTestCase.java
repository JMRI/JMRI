package jmri.util;

import junit.extensions.jfcunit.*;


/**
 * Provide Swing context for JUnit test classes.
 *<p>
 * By default, JFCUnit closes all windows at the end
 * of each test. JMRI tests leave windows open, so that's
 * been bypassed for now.
 *
 * @author	Bob Jacobsen - Copyright 2009
 * @version	$Revision$
 * @since 2.5.3
 */
 
public class SwingTestCase extends JFCTestCase {

    public SwingTestCase(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        // Choose the test Helper
        setHelper( new JFCTestHelper( ) ); // Uses the AWT Event Queue.
        // setHelper( new RobotTestHelper( ) ); // Uses the OS Event Queue.
    }
    
    protected void leaveAllWindowsOpen() {
    	TestHelper.addSystemWindow(".");  // all windows left open
    }
    
    protected void tearDown() throws Exception {
        leaveAllWindowsOpen();
        TestHelper.cleanUp( this );
        super.tearDown( );
    }
    
}

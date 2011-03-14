// LEConnectivityTest.java

package jmri.jmrit.display.layoutEditor;

import javax.swing.*;

import jmri.Block;
import jmri.BlockManager;
import jmri.Turnout;

import java.util.*;

import junit.framework.*;
import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;

/**
 * Swing jfcUnit tests for the LayoutEditor 
 * @author			Dave Duchamp  Copyright 2011
 * @version         $Revision: 1.4 $
 */
public class LEConnectivityTest extends jmri.util.SwingTestCase {


    @SuppressWarnings("unchecked")
	public void testShowAndClose() throws Exception {
	    jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager(){
	    };
	    
	    // load and display test panel file
	    java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/LEConnectTest.xml");
        cm.load(f);
        sleep(100); // time for internal listeners to calm down
        
        // Find new window by name (should be more distinctive, comes from sample file)
        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("Connectivity Test");
        Assert.assertNotNull(le);
        
        // Panel is up, continue set up for tests.
		ConnectivityUtil cu = new ConnectivityUtil(le);
        Assert.assertNotNull(cu);
		BlockManager bm = jmri.InstanceManager.blockManagerInstance();
		Assert.assertNotNull(bm);
		
		// Test right-handed crossover connectivity turnout settings
		Block cBlock = bm.getBlock("4");
		Block pBlock = bm.getBlock("6");
		Block nBlock = bm.getBlock("5");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		ArrayList<Integer> tsList = cu.getTurnoutSettingList();
		int setting = tsList.get(0).intValue();
		Assert.assertEquals("6_4_5Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("5");
		nBlock = bm.getBlock("6");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("5_4_6Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("5");
		nBlock = bm.getBlock("2");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("5_4_2Connect",setting,Turnout.THROWN);
		cBlock = bm.getBlock("2");
		pBlock = bm.getBlock("1");
		nBlock = bm.getBlock("3");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("1_2_3Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("3");
		nBlock = bm.getBlock("1");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("3_2_1Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("1");
		nBlock = bm.getBlock("4");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("1_2_4Connect",setting,Turnout.THROWN);
		
		// Test left-handed crossover connectivity turnout settings
		cBlock = bm.getBlock("14");
		pBlock = bm.getBlock("13");
		nBlock = bm.getBlock("17");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("13_14_17Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("17");
		nBlock = bm.getBlock("13");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("17_14_13Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("17");
		nBlock = bm.getBlock("12");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("17_14_12Connect",setting,Turnout.THROWN);
		cBlock = bm.getBlock("12");
		pBlock = bm.getBlock("11");
		nBlock = bm.getBlock("15");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("11_12_15Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("15");
		nBlock = bm.getBlock("11");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("15_12_11Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("15");
		nBlock = bm.getBlock("14");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("15_12_14Connect",setting,Turnout.THROWN);
		
		// Test double crossover connectivity turnout settings
		cBlock = bm.getBlock("21");
		pBlock = bm.getBlock("20");
		nBlock = bm.getBlock("22");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("20_21_22Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("22");
		nBlock = bm.getBlock("20");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("22_21_20Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("20");
		nBlock = bm.getBlock("26");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("20_21_26Connect",setting,Turnout.THROWN);
		cBlock = bm.getBlock("22");
		pBlock = bm.getBlock("23");
		nBlock = bm.getBlock("21");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("23_22_21Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("21");
		nBlock = bm.getBlock("23");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("21_22_23Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("23");
		nBlock = bm.getBlock("25");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("23_22_25Connect",setting,Turnout.THROWN);
		cBlock = bm.getBlock("26");
		pBlock = bm.getBlock("27");
		nBlock = bm.getBlock("25");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("27_26_25Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("25");
		nBlock = bm.getBlock("27");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("25_26_27Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("27");
		nBlock = bm.getBlock("21");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("27_26_21Connect",setting,Turnout.THROWN);
		cBlock = bm.getBlock("25");
		pBlock = bm.getBlock("24");
		nBlock = bm.getBlock("26");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("24_25_26Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("26");
		nBlock = bm.getBlock("24");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("26_25_24Connect",setting,Turnout.CLOSED);
		pBlock = bm.getBlock("24");
		nBlock = bm.getBlock("22");
		cu.getTurnoutList(cBlock,pBlock,nBlock);
		tsList = cu.getTurnoutSettingList();
		setting = tsList.get(0).intValue();
		Assert.assertEquals("24_25_22Connect",setting,Turnout.THROWN);

        // Ask to close window
        TestHelper.disposeWindow(le, this);
        
        // Dialog has popped up, so handle that. First, locate it.
        List<JDialog> dialogList = new DialogFinder(null).findAll(le);
        JDialog d = dialogList.get(0);

        // Find the button that deletes the panel
        AbstractButtonFinder finder = new AbstractButtonFinder("Delete Panel" );
        JButton button = ( JButton ) finder.find( d, 0);
        Assert.assertNotNull(button);   
                
        // Click button to delete panel and close window
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );
        
        // another dialog has popped up, so handle that by finding the "Yes - Delete" button.
        dialogList = new DialogFinder(null).findAll(le);
        d = dialogList.get(0);
        finder = new AbstractButtonFinder("Yes - Delete" );
        button = ( JButton ) finder.find( d, 0);
        Assert.assertNotNull(button);   

        // Click to say yes, I really mean it.
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );
	}

    
	// from here down is testing infrastructure
	public LEConnectivityTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LEConnectivityTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LEConnectivityTest.class);  
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}

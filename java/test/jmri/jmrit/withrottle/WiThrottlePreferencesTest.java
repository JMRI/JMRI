package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of WiThrottlePreferences
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiThrottlePreferencesTest {
 
    private WiThrottlePreferences prefs;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", prefs );
    }

    @Test
    public void testGetDefaultPort() {
        Assert.assertEquals("default port", 12090, prefs.getPort() );
    }

    @Test
    public void testSetAndGetPort() {
        prefs.setPort(12345);
        Assert.assertEquals("port after set", 12345, prefs.getPort() );
    }

    @Test
    public void testGetDefaultEStopDelay() {
        Assert.assertEquals("default estop delay", 10, prefs.getEStopDelay() );
    }

    @Test
    public void testSetAndGetEStopDelay() {
        prefs.setEStopDelay(12345);
        Assert.assertEquals("port after set", 12345, prefs.getEStopDelay() );
    }

    @Test
    public void testIsRestartRequired(){
        Assert.assertFalse("restart required",prefs.isRestartRequired());
    }

    @Test
    public void testIsUseEStop(){
        Assert.assertTrue("Use EStop",prefs.isUseEStop());
    }

    @Test
    public void testSetAndGetUseEStop(){
        prefs.setUseEStop(false);
        Assert.assertFalse("Use EStop",prefs.isUseEStop());
    }

    @Test
    public void testIsUseMomF2(){
        Assert.assertTrue("Use Momentary F2",prefs.isUseMomF2());
    }

    @Test
    public void testSetAndGetUseMomF2(){
        prefs.setUseMomF2(false);
        Assert.assertFalse("Use Momentary F2",prefs.isUseMomF2());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        prefs = new WiThrottlePreferences();
    }

    @Test
    public void testIsAllowTrackPower(){
        Assert.assertTrue("Allow Track Power",prefs.isAllowTrackPower());
    }

    @Test
    public void testSetAndGetAllowTrackPower(){
        prefs.setAllowTrackPower(false);
        Assert.assertFalse("Allow Track Power",prefs.isAllowTrackPower());
    }

    @Test
    public void testIsAllowTurnout(){
        Assert.assertTrue("Allow Turnout Control",prefs.isAllowTurnout());
    }

    @Test
    public void testSetAndGetAllowTurnout(){
        prefs.setAllowTurnout(false);
        Assert.assertFalse("Allow Turnout",prefs.isAllowTurnout());
    }

    @Test
    public void testSetAndGetAllowTurnoutCreation(){
        prefs.setAllowTurnoutCreation(false);
        Assert.assertFalse("Allow Turnout Creation",prefs.isAllowTurnoutCreation());
    }

    @Test
    public void testIsAllowRoute(){
        Assert.assertTrue("Allow Route Control",prefs.isAllowRoute());
    }

    @Test
    public void testSetAndGetAllowRoute(){
        prefs.setAllowRoute(false);
        Assert.assertFalse("Allow Route",prefs.isAllowRoute());
    }

    @Test
    public void testIsAllowConsist(){
        Assert.assertTrue("Allow Consist Control",prefs.isAllowConsist());
    }

    @Test
    public void testSetAndGetAllowConsist(){
        prefs.setAllowConsist(false);
        Assert.assertFalse("Allow Consist",prefs.isAllowConsist());
    }

    @Test
    public void testIsUseWiFiConsist(){
        Assert.assertTrue("Use WiFi Consist",prefs.isUseWiFiConsist());
    }

    @Test
    public void testSetAndGetUseWiFiConsist(){
        prefs.setUseWiFiConsist(false);
        Assert.assertFalse("Use WiFiConsist",prefs.isUseWiFiConsist());
    }
    
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}

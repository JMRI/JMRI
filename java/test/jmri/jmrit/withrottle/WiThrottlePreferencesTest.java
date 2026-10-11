package jmri.jmrit.withrottle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiThrottlePreferences
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class WiThrottlePreferencesTest {
 
    private WiThrottlePreferences prefs;

    @Test
    public void testCtor() {
        assertNotNull( prefs, "exists");
    }

    @Test
    public void testGetDefaultPort() {
        assertEquals( 12090, prefs.getPort(), "default port");
    }

    @Test
    public void testSetAndGetPort() {
        prefs.setPort(12345);
        assertEquals( 12345, prefs.getPort(), "port after set");
    }

    @Test
    public void testGetDefaultEStopDelay() {
        assertEquals( 10, prefs.getEStopDelay(), "default estop delay");
    }

    @Test
    public void testSetAndGetEStopDelay() {
        prefs.setEStopDelay(12345);
        assertEquals( 12345, prefs.getEStopDelay(), "port after set");
    }

    @Test
    public void testIsRestartRequired(){
        assertFalse( prefs.isRestartRequired(), "restart required");
    }

    @Test
    public void testIsUseEStop(){
        assertTrue( prefs.isUseEStop(), "Use EStop");
    }

    @Test
    public void testSetAndGetUseEStop(){
        prefs.setUseEStop(false);
        assertFalse( prefs.isUseEStop(), "Use EStop");
    }

    @Test
    public void testIsUseMomF2(){
        assertTrue( prefs.isUseMomF2(), "Use Momentary F2");
    }

    @Test
    public void testSetAndGetUseMomF2(){
        prefs.setUseMomF2(false);
        assertFalse( prefs.isUseMomF2(), "Use Momentary F2");
    }

    @Test
    public void testIsAllowTrackPower(){
        assertTrue( prefs.isAllowTrackPower(), "Allow Track Power");
    }

    @Test
    public void testSetAndGetAllowTrackPower(){
        prefs.setAllowTrackPower(false);
        assertFalse( prefs.isAllowTrackPower(), "Allow Track Power");
    }

    @Test
    public void testIsAllowTurnout(){
        assertTrue( prefs.isAllowTurnout(), "Allow Turnout Control");
    }

    @Test
    public void testSetAndGetAllowTurnout(){
        prefs.setAllowTurnout(false);
        assertFalse( prefs.isAllowTurnout(), "Allow Turnout");
    }

    @Test
    public void testSetAndGetAllowTurnoutCreation(){
        prefs.setAllowTurnoutCreation(false);
        assertFalse( prefs.isAllowTurnoutCreation(), "Allow Turnout Creation");
    }

    @Test
    public void testIsAllowRoute(){
        assertTrue( prefs.isAllowRoute(), "Allow Route Control");
    }

    @Test
    public void testSetAndGetAllowRoute(){
        prefs.setAllowRoute(false);
        assertFalse( prefs.isAllowRoute(), "Allow Route");
    }

    @Test
    public void testIsAllowConsist(){
        assertTrue( prefs.isAllowConsist(), "Allow Consist Control");
    }

    @Test
    public void testSetAndGetAllowConsist(){
        prefs.setAllowConsist(false);
        assertFalse( prefs.isAllowConsist(), "Allow Consist");
    }

    @Test
    public void testIsUseWiFiConsist(){
        assertTrue( prefs.isUseWiFiConsist(), "Use WiFi Consist");
    }

    @Test
    public void testSetAndGetUseWiFiConsist(){
        prefs.setUseWiFiConsist(false);
        assertFalse( prefs.isUseWiFiConsist(), "Use WiFiConsist");
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        prefs = new WiThrottlePreferences();
    }

    @AfterEach
    public void tearDown() {
        prefs = null;
        JUnitUtil.tearDown();
    }

}

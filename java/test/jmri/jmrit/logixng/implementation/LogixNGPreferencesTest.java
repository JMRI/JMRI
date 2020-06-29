package jmri.jmrit.logixng.implementation;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNGPreferences
 * 
 * @author Daniel Bergqvist 2020
 */
public class LogixNGPreferencesTest {

    @Test
    public void testCompareValuesDifferent() {
        LogixNGPreferences prefsA = new LogixNGPreferences();
        LogixNGPreferences prefsB = new LogixNGPreferences();
        Assert.assertFalse("prefs are equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertFalse("prefs are equal", prefsB.compareValuesDifferent(prefsA));
        
        prefsB.setAllowDebugMode(false);
        prefsB.setAllowDebugMode(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setAllowDebugMode(false);
        
        prefsB.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setStartLogixNGOnStartup(false);
        
        prefsB.setUseGenericFemaleSockets(false);
        prefsB.setUseGenericFemaleSockets(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setUseGenericFemaleSockets(false);
    }
    
    @Test
    public void testSetAndGet() {
        LogixNGPreferences prefs = new LogixNGPreferences();
        
        prefs.setStartLogixNGOnStartup(true);
        Assert.assertTrue(prefs.getStartLogixNGOnStartup());
        
        prefs.setStartLogixNGOnStartup(false);
        Assert.assertFalse(prefs.getStartLogixNGOnStartup());
        
        prefs.setUseGenericFemaleSockets(true);
        Assert.assertTrue(prefs.getUseGenericFemaleSockets());
        
        prefs.setUseGenericFemaleSockets(false);
        Assert.assertFalse(prefs.getUseGenericFemaleSockets());
        
        prefs.setAllowDebugMode(true);
        Assert.assertTrue(prefs.getAllowDebugMode());
        
        prefs.setAllowDebugMode(false);
        Assert.assertFalse(prefs.getAllowDebugMode());
    }
    
    @Test
    public void testApply() {
        LogixNGPreferences prefsA = new LogixNGPreferences();
        LogixNGPreferences prefsB = new LogixNGPreferences();
        
        prefsB.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        Assert.assertFalse(prefsA.getStartLogixNGOnStartup());
        Assert.assertTrue(prefsB.getStartLogixNGOnStartup());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getStartLogixNGOnStartup());
        
        prefsB.setUseGenericFemaleSockets(false);
        prefsB.setUseGenericFemaleSockets(true);
        Assert.assertFalse(prefsA.getUseGenericFemaleSockets());
        Assert.assertTrue(prefsB.getUseGenericFemaleSockets());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getUseGenericFemaleSockets());
        
        prefsB.setAllowDebugMode(false);
        prefsB.setAllowDebugMode(true);
        Assert.assertFalse(prefsA.getAllowDebugMode());
        Assert.assertTrue(prefsB.getAllowDebugMode());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getAllowDebugMode());
    }
    
    @Test
    public void testSave() {
        LogixNGPreferences prefsA = new LogixNGPreferences();
        LogixNGPreferences prefsB;
        
        prefsA.setStartLogixNGOnStartup(false);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertFalse(prefsB.getStartLogixNGOnStartup());
        
        prefsA.setStartLogixNGOnStartup(true);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertTrue(prefsB.getStartLogixNGOnStartup());
        
        prefsA.setUseGenericFemaleSockets(false);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertFalse(prefsB.getUseGenericFemaleSockets());
        
        prefsA.setUseGenericFemaleSockets(true);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertTrue(prefsB.getUseGenericFemaleSockets());
        
        prefsA.setAllowDebugMode(false);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertFalse(prefsB.getAllowDebugMode());
        
        prefsA.setAllowDebugMode(true);
        prefsA.save();
        prefsB = new LogixNGPreferences();
        Assert.assertTrue(prefsB.getAllowDebugMode());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}

package jmri.jmrit.logixng.implementation;

import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test LogixNGPreferences
 * 
 * @author Daniel Bergqvist 2020
 */
public class LogixNGPreferencesTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    @Test
    public void testCompareValuesDifferent() {
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB = new DefaultLogixNGPreferences();
        Assert.assertFalse("prefs are equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertFalse("prefs are equal", prefsB.compareValuesDifferent(prefsA));
        
        prefsA.setAllowDebugMode(false);
        prefsB.setAllowDebugMode(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setAllowDebugMode(false);
        
        prefsA.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setStartLogixNGOnStartup(false);
        
        prefsA.setUseGenericFemaleSockets(false);
        prefsB.setUseGenericFemaleSockets(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setUseGenericFemaleSockets(false);
    }
    
    @Test
    public void testSetAndGet() {
        DefaultLogixNGPreferences prefs = new DefaultLogixNGPreferences();
        
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
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB = new DefaultLogixNGPreferences();
        
        prefsA.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        Assert.assertFalse(prefsA.getStartLogixNGOnStartup());
        Assert.assertTrue(prefsB.getStartLogixNGOnStartup());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getStartLogixNGOnStartup());
        
        prefsA.setStartLogixNGOnStartup(true);
        prefsB.setStartLogixNGOnStartup(false);
        Assert.assertTrue(prefsA.getStartLogixNGOnStartup());
        Assert.assertFalse(prefsB.getStartLogixNGOnStartup());
        prefsA.apply(prefsB);
        Assert.assertFalse(prefsA.getStartLogixNGOnStartup());
        
        prefsA.setUseGenericFemaleSockets(false);
        prefsB.setUseGenericFemaleSockets(true);
        Assert.assertFalse(prefsA.getUseGenericFemaleSockets());
        Assert.assertTrue(prefsB.getUseGenericFemaleSockets());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getUseGenericFemaleSockets());
        
        prefsA.setUseGenericFemaleSockets(true);
        prefsB.setUseGenericFemaleSockets(false);
        Assert.assertTrue(prefsA.getUseGenericFemaleSockets());
        Assert.assertFalse(prefsB.getUseGenericFemaleSockets());
        prefsA.apply(prefsB);
        Assert.assertFalse(prefsA.getUseGenericFemaleSockets());
        
        prefsA.setAllowDebugMode(false);
        prefsB.setAllowDebugMode(true);
        Assert.assertFalse(prefsA.getAllowDebugMode());
        Assert.assertTrue(prefsB.getAllowDebugMode());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getAllowDebugMode());
        
        prefsA.setAllowDebugMode(true);
        prefsB.setAllowDebugMode(false);
        Assert.assertTrue(prefsA.getAllowDebugMode());
        Assert.assertFalse(prefsB.getAllowDebugMode());
        prefsA.apply(prefsB);
        Assert.assertFalse(prefsA.getAllowDebugMode());
    }
    
    @Test
    public void testSave() {
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB;
        
        prefsA.setStartLogixNGOnStartup(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertFalse(prefsB.getStartLogixNGOnStartup());
        
        prefsA.setStartLogixNGOnStartup(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertTrue(prefsB.getStartLogixNGOnStartup());
        
        prefsA.setUseGenericFemaleSockets(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertFalse(prefsB.getUseGenericFemaleSockets());
        
        prefsA.setUseGenericFemaleSockets(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertTrue(prefsB.getUseGenericFemaleSockets());
        
        prefsA.setAllowDebugMode(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertFalse(prefsB.getAllowDebugMode());
        
        prefsA.setAllowDebugMode(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertTrue(prefsB.getAllowDebugMode());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}

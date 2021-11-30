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
        
        prefsA.setInstallDebugger(false);
        prefsB.setInstallDebugger(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setInstallDebugger(false);
        
        prefsA.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        Assert.assertTrue("prefs are not equal", prefsA.compareValuesDifferent(prefsB));
        Assert.assertTrue("prefs are not equal", prefsB.compareValuesDifferent(prefsA));
        prefsB.setStartLogixNGOnStartup(false);
    }
    
    @Test
    public void testSetAndGet() {
        DefaultLogixNGPreferences prefs = new DefaultLogixNGPreferences();
        
        prefs.setStartLogixNGOnStartup(true);
        Assert.assertTrue(prefs.getStartLogixNGOnStartup());
        
        prefs.setStartLogixNGOnStartup(false);
        Assert.assertFalse(prefs.getStartLogixNGOnStartup());
        
        prefs.setInstallDebugger(true);
        Assert.assertTrue(prefs.getInstallDebugger());
        
        prefs.setInstallDebugger(false);
        Assert.assertFalse(prefs.getInstallDebugger());
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
        
        prefsA.setInstallDebugger(false);
        prefsB.setInstallDebugger(true);
        Assert.assertFalse(prefsA.getInstallDebugger());
        Assert.assertTrue(prefsB.getInstallDebugger());
        prefsA.apply(prefsB);
        Assert.assertTrue(prefsA.getInstallDebugger());
        
        prefsA.setInstallDebugger(true);
        prefsB.setInstallDebugger(false);
        Assert.assertTrue(prefsA.getInstallDebugger());
        Assert.assertFalse(prefsB.getInstallDebugger());
        prefsA.apply(prefsB);
        Assert.assertFalse(prefsA.getInstallDebugger());
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
        
        prefsA.setInstallDebugger(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertFalse(prefsB.getInstallDebugger());
        
        prefsA.setInstallDebugger(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        Assert.assertTrue(prefsB.getInstallDebugger());
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}

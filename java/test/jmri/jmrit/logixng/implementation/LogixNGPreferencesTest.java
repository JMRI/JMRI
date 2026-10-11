package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test LogixNGPreferences
 *
 * @author Daniel Bergqvist 2020
 */
public class LogixNGPreferencesTest {

    @Test
    public void testCompareValuesDifferent() {
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB = new DefaultLogixNGPreferences();
        assertFalse( prefsA.compareValuesDifferent(prefsB), "prefs are equal");
        assertFalse( prefsB.compareValuesDifferent(prefsA), "prefs are equal");

        prefsA.setInstallDebugger(false);
        prefsB.setInstallDebugger(true);
        assertTrue( prefsA.compareValuesDifferent(prefsB), "prefs are not equal");
        assertTrue( prefsB.compareValuesDifferent(prefsA), "prefs are not equal");
        prefsB.setInstallDebugger(false);

        prefsA.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        assertTrue( prefsA.compareValuesDifferent(prefsB), "prefs are not equal");
        assertTrue( prefsB.compareValuesDifferent(prefsA), "prefs are not equal");
        prefsB.setStartLogixNGOnStartup(false);
    }

    @Test
    public void testSetAndGet() {
        DefaultLogixNGPreferences prefs = new DefaultLogixNGPreferences();

        prefs.setStartLogixNGOnStartup(true);
        assertTrue(prefs.getStartLogixNGOnStartup());

        prefs.setStartLogixNGOnStartup(false);
        assertFalse(prefs.getStartLogixNGOnStartup());

        prefs.setInstallDebugger(true);
        assertTrue(prefs.getInstallDebugger());

        prefs.setInstallDebugger(false);
        assertFalse(prefs.getInstallDebugger());
    }

    @Test
    public void testApply() {
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB = new DefaultLogixNGPreferences();

        prefsA.setStartLogixNGOnStartup(false);
        prefsB.setStartLogixNGOnStartup(true);
        assertFalse(prefsA.getStartLogixNGOnStartup());
        assertTrue(prefsB.getStartLogixNGOnStartup());
        prefsA.apply(prefsB);
        assertTrue(prefsA.getStartLogixNGOnStartup());

        prefsA.setStartLogixNGOnStartup(true);
        prefsB.setStartLogixNGOnStartup(false);
        assertTrue(prefsA.getStartLogixNGOnStartup());
        assertFalse(prefsB.getStartLogixNGOnStartup());
        prefsA.apply(prefsB);
        assertFalse(prefsA.getStartLogixNGOnStartup());

        prefsA.setInstallDebugger(false);
        prefsB.setInstallDebugger(true);
        assertFalse(prefsA.getInstallDebugger());
        assertTrue(prefsB.getInstallDebugger());
        prefsA.apply(prefsB);
        assertTrue(prefsA.getInstallDebugger());

        prefsA.setInstallDebugger(true);
        prefsB.setInstallDebugger(false);
        assertTrue(prefsA.getInstallDebugger());
        assertFalse(prefsB.getInstallDebugger());
        prefsA.apply(prefsB);
        assertFalse(prefsA.getInstallDebugger());
    }

    @Test
    public void testSave() {
        DefaultLogixNGPreferences prefsA = new DefaultLogixNGPreferences();
        DefaultLogixNGPreferences prefsB;

        prefsA.setStartLogixNGOnStartup(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertFalse(prefsB.getStartLogixNGOnStartup());

        prefsA.setStartLogixNGOnStartup(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertTrue(prefsB.getStartLogixNGOnStartup());

        prefsA.setInstallDebugger(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertFalse(prefsB.getInstallDebugger());

        prefsA.setInstallDebugger(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertTrue(prefsB.getInstallDebugger());

        prefsA.setStrictTypingGlobalVariables(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertFalse(prefsB.getStrictTypingGlobalVariables());

        prefsA.setStrictTypingGlobalVariables(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertTrue(prefsB.getStrictTypingGlobalVariables());

        prefsA.setStrictTypingLocalVariables(false);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertFalse(prefsB.getStrictTypingLocalVariables());

        prefsA.setStrictTypingLocalVariables(true);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertTrue(prefsB.getStrictTypingLocalVariables());

        prefsA.setIfThenElseExecuteTypeDefault(IfThenElse.ExecuteType.AlwaysExecute);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertEquals(IfThenElse.ExecuteType.AlwaysExecute, prefsB.getIfThenElseExecuteTypeDefault());

        prefsA.setIfThenElseExecuteTypeDefault(IfThenElse.ExecuteType.ExecuteOnChange);
        prefsA.save();
        prefsB = new DefaultLogixNGPreferences();
        assertEquals(IfThenElse.ExecuteType.ExecuteOnChange, prefsB.getIfThenElseExecuteTypeDefault());
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}

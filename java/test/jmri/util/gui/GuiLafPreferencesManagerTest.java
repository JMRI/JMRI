package jmri.util.gui;

import jmri.spi.PreferencesManager;
import jmri.util.JUnitUtil;
import jmri.util.gui.GuiLafPreferencesManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class GuiLafPreferencesManagerTest {

    Locale current;
    GuiLafPreferencesManager t;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testGetRequires() {
        Iterator<Class<? extends PreferencesManager>> i = t.getRequires().iterator();
        assertFalse("No requirements on other Preferences Managers", i.hasNext());
    }

    @Test
    public void testProvides() {
        ArrayList<Class<?>> a = new ArrayList<>();
        t.getProvides().forEach(c -> a.add(c));
        assertEquals("Only provides one item", 1, a.size());
        assertEquals("Only provides self", GuiLafPreferencesManager.class, a.get(0));
    }

    @Test
    public void testLocale() {
        Assume.assumeFalse("Default locale is not Italian", Locale.getDefault().equals(Locale.ITALIAN));
        JComponent.setDefaultLocale(Locale.getDefault());
        assertEquals("JComponent has current locale", Locale.getDefault(), JComponent.getDefaultLocale());
        assertEquals("Defaults to default locale", Locale.getDefault(), t.getLocale());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setLocale(Locale.getDefault());
        assertEquals("Locale is default", Locale.getDefault(), t.getLocale());
        assertEquals("JComponent has default locale", Locale.getDefault(), JComponent.getDefaultLocale());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setLocale(Locale.ITALIAN);
        assertEquals("Locale is Italian", Locale.ITALIAN, t.getLocale());
        assertNotEquals("Does not have default locale", Locale.getDefault(), t.getLocale());
        assertEquals("JComponent has default locale", Locale.getDefault(), JComponent.getDefaultLocale());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
        t.setLocale(Locale.getDefault());
        assertEquals("Locale is default", Locale.getDefault(), t.getLocale());
        assertEquals("JComponent has default locale", Locale.getDefault(), JComponent.getDefaultLocale());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
    }

    @Test
    public void testToolTipDismissDelay() {
        int dd = ToolTipManager.sharedInstance().getDismissDelay();
        int ld = 2 * dd;
        assertEquals("delay is default", dd, t.getToolTipDismissDelay());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setToolTipDismissDelay(dd);
        assertEquals("delay is default", dd, t.getToolTipDismissDelay());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setToolTipDismissDelay(ld);
        assertEquals("delay is long", ld, t.getToolTipDismissDelay());
        assertTrue("Changes made", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
    }

    @Test
    public void testNonstandardMouseEvent() {
        assertFalse("Standard events", t.isNonStandardMouseEvent());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setNonStandardMouseEvent(false);
        assertFalse("Standard events", t.isNonStandardMouseEvent());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setNonStandardMouseEvent(true);
        assertTrue("Non-standard events", t.isNonStandardMouseEvent());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
    }

    @Test
    public void testGraphicsTableState() {
        assertFalse("Use text", t.isGraphicTableState());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setGraphicTableState(false);
        assertFalse("Use text", t.isGraphicTableState());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setGraphicTableState(true);
        assertTrue("Use graphics", t.isGraphicTableState());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
    }

    @Test
    public void testEditorUseOldLocSize() {
        assertFalse("Use old location and size", t.isEditorUseOldLocSize());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setEditorUseOldLocSize(false);
        assertFalse("Use old location and size", t.isEditorUseOldLocSize());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        t.setEditorUseOldLocSize(true);
        assertTrue("Use old location and size", t.isEditorUseOldLocSize());
        assertTrue("Changes made", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        current = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        t = new GuiLafPreferencesManager();
    }

    @After
    public void tearDown() {
        Locale.setDefault(current);
        JUnitUtil.tearDown();
    }

}

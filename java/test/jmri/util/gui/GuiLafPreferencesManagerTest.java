package jmri.util.gui;

import jmri.spi.PreferencesManager;
import jmri.util.JUnitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2019
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

    // passing null to @Nonnull annotated method to test later use
    @SuppressWarnings({"null", "deprecation"})
    @Test
    public void testFont() {
        Font f1 = Font.decode(null).deriveFont(Font.PLAIN, 44);
        Font f2 = t.getDefaultFont();
        assertNull("Has no font", t.getFont());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        // change to setFont(String) when removing deprecated method
        t.setFontByName(f1.getFontName());
        // without calling setFontSize(int), font size == 0
        assertEquals("Font is 0 size", f1.deriveFont((float) 0).getSize(), t.getFont().getSize());
        assertNotEquals("Font is not Font default", Font.decode(null), t.getFont());
        assertNotEquals("Font is not derived font", f1, t.getFont());
        t.setFont(f2);
        assertEquals("Font is list default", f2, t.getFont());
        assertNotEquals("Font is not Font default", Font.decode(null), t.getFont());
        assertNotEquals("Font is not derived font", f1, t.getFont());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
        t.setFont(f1);
        assertNotEquals("Font is not list default", f2, t.getFont());
        assertEquals("Font is derived font", f1, t.getFont());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
        // setting the same font twice should not change anything
        t.setFont(f1);
        assertNotEquals("Font is not list default", f2, t.getFont());
        assertEquals("Font is derived font", f1, t.getFont());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
        // set to f2 to ensure setting to f1 by name is a change
        t.setFont(f2);
        assertEquals("Font is list default", f2, t.getFont());
        // change to setFont(String) when removing deprecated method
        t.setFontByName(f1.getFontName());
        // without calling setFontSize(int), font size == 0
        assertEquals("Font is 0 size", f1.deriveFont((float) 0).getSize(), t.getFont().getSize());
        assertNotEquals("Font is not Font default", Font.decode(null), t.getFont());
        assertNotEquals("Font is not derived font", f1, t.getFont());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
        // set to null to test unique code path
        t.setFont((Font) null);
        assertNull("No font set", t.getFont());
        // change to setFont(String) when removing deprecated method
        t.setFontByName(f1.getFontName());
        // without calling setFontSize(int), font size == 0
        assertEquals("Font is plain", f1.deriveFont(0).getStyle(), t.getFont().getStyle());
        assertNotEquals("Font is not Font default", Font.decode(null), t.getFont());
        assertNotEquals("Font is not derived font", f1, t.getFont());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
    }

    @Test
    public void testFontSize() {
        assertEquals("Font size == 0", 0, t.getFontSize());
        assertFalse("No changes", t.isDirty());
        assertFalse("No need to restart", t.isRestartRequired());
        int low = 1;
        int high = 50;
        assertTrue("low < MIN_FONT_SIZE", low < GuiLafPreferencesManager.MIN_FONT_SIZE);
        assertTrue("high > MIN_FONT_SIZE", high > GuiLafPreferencesManager.MAX_FONT_SIZE);
        for (int i = low; i < high; i++) {
            t.setFontSize(i);
            if (i < GuiLafPreferencesManager.MIN_FONT_SIZE) {
                assertEquals("Min font size", GuiLafPreferencesManager.MIN_FONT_SIZE, t.getFontSize());
            } else if (i > GuiLafPreferencesManager.MAX_FONT_SIZE) {
                assertEquals("Max font size", GuiLafPreferencesManager.MAX_FONT_SIZE, t.getFontSize());
            } else {
                assertEquals("font size", i, t.getFontSize());
            }
            assertTrue("Changes made", t.isDirty());
            assertTrue("Need to restart", t.isRestartRequired());
        }
        t.setFontSize(0);
        assertEquals("Font size == 0", 0, t.getFontSize());
        assertTrue("Changes made", t.isDirty());
        assertTrue("Need to restart", t.isRestartRequired());
    }

    @Test
    public void testDefaultFontNimbus() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        Font f = UIManager.getFont("List.font");
        assertEquals("Default font", f, t.getDefaultFont());
        assertEquals("Default font size", f.getSize(), t.getDefaultFontSize());
    }

    @Test
    public void testDefaultFontMetal() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        Font f = UIManager.getFont("List.font");
        assertEquals("Default font", f, t.getDefaultFont());
        assertEquals("Default font size", f.getSize(), t.getDefaultFontSize());
    }

    @Test
    public void testDefaultFontAqua() {
        try {
            UIManager.setLookAndFeel("apple.laf.AquaLookAndFeel");
        } catch (
                ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            // skip test if not on macOS
            Assume.assumeNoException("Not on macOS", e);
        }
        Font f = UIManager.getFont("List.font");
        assertEquals("Default font", f, t.getDefaultFont());
        assertEquals("Default font size", f.getSize(), t.getDefaultFontSize());
    }

    @Test
    public void testDefaultFontWindows() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (
                ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            // skip test if not on Microsoft Windows
            Assume.assumeNoException("Not on Windows", e);
        }
        Font f = UIManager.getFont("List.font");
        assertEquals("Default font", f, t.getDefaultFont());
        assertEquals("Default font size", f.getSize(), t.getDefaultFontSize());
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
